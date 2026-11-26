package it.nexus.legacy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import it.nexus.domain.CompanyPlaceholders;
import it.nexus.legacy.AccessReader.LegacyCall;
import it.nexus.legacy.AccessReader.LegacyCompany;
import it.nexus.legacy.AccessReader.LegacyData;

/**
 * Importazione dal database attuale (US-1309): aziende con sedi, recapiti, referenti, categoria e nota nel diario, più
 * l'archivio delle chiamate. Tutto in una transazione, con gli id fissi di {@link LegacyIds}: una seconda esecuzione non
 * duplica e non tocca le aziende che ci sono già. Non scrive dati personali nel log.
 */
@Service
public class LegacyImportService {

    static final String ALIASES = "legacy/municipality-aliases.csv";
    private static final ZoneId ROME = ZoneId.of("Europe/Rome");
    private static final int NOTE_LENGTH = 2000;
    private static final int MAX_NOTE_PARTS = 9;
    /** Ruolo 28 di Access: doppione di 27, «Legale Rappresentante» (V47). */
    private static final int DUPLICATE_ROLE = 28;
    private static final int LEGAL_REPRESENTATIVE_ROLE = 27;
    /** Possibile nome e cognome di un beneficiario nelle note di una chiamata: da far controllare al PO. */
    private static final Pattern NAME_IN_NOTES = Pattern.compile(
            "(?:[Uu]tente|[Cc]andidat[oa]|[Rr]agazz[oa])\\s+(?:\\S+\\s+)?\\p{Lu}[\\p{Ll}']{2,}\\s+\\p{Lu}[\\p{Ll}']{2,}");

    private static final String INSERT_COMPANY = """
            INSERT INTO company (id, name, vat_code, legal_address, legal_municipality_id, operating_address,
                                 operating_municipality_id, business_category_id, activity, phone, mobile, fax, email,
                                 secondary_email, reliability, project_interest, active, version, created_at,
                                 created_by, updated_at, updated_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, true, 0, ?, ?, ?, ?)
            ON CONFLICT (id) DO NOTHING""";
    private static final String INSERT_CONTACT = """
            INSERT INTO company_contact (id, company_id, position, name, role_id, created_at, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO NOTHING""";
    private static final String INSERT_NOTE = """
            INSERT INTO company_note (id, company_id, text, created_at, created_by)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (id) DO NOTHING""";
    private static final String INSERT_AUDIT = """
            INSERT INTO audit_event (id, entity_type, entity_id, action, changes, created_at, created_by)
            VALUES (?, 'COMPANY', ?, 'IMPORT', ?::jsonb, ?, ?)
            ON CONFLICT (id) DO NOTHING""";
    private static final String INSERT_CALL = """
            INSERT INTO legacy_call (id, legacy_number, company_id, legacy_company_number, called_at, subject, outcome,
                                     outcome_at, notes, handled, internship, stage, ist, work_grant, hiring, giovanisi,
                                     project, operator, legacy_updated_at, created_at, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO NOTHING""";

    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaction;

    public LegacyImportService(JdbcTemplate jdbc, PlatformTransactionManager transactionManager) {
        this.jdbc = jdbc;
        this.transaction = new TransactionTemplate(transactionManager);
    }

    /** Legge il file (in sola lettura) e importa; senza l'elenco dei comuni non scrive nulla. */
    public LegacyImportReport importFile(Path file) throws IOException {
        LegacyData data = AccessReader.read(file);
        return transaction.execute(status -> write(data, Instant.now()));
    }

    private LegacyImportReport write(LegacyData data, Instant now) {
        MunicipalityMatcher matcher = new MunicipalityMatcher(municipalities(), aliases());
        if (matcher.isEmpty()) {
            throw new LegacyImportException(
                    "Manca l'elenco dei comuni: caricalo dai Cataloghi (US-114) e ripeti l'importazione");
        }
        LegacyImportReport report = new LegacyImportReport(now);
        Set<Long> categories = ids("SELECT id FROM business_category");
        Set<Long> roles = ids("SELECT id FROM contact_role");
        Set<Long> existing = ids("SELECT id FROM company WHERE id BETWEEN %d AND %d"
                .formatted(LegacyIds.COMPANY, LegacyIds.CONTACT - 1));

        List<Object[]> companies = new ArrayList<>();
        List<Object[]> contacts = new ArrayList<>();
        List<Object[]> notes = new ArrayList<>();
        List<Object[]> audits = new ArrayList<>();
        Map<String, List<Integer>> byName = new LinkedHashMap<>();
        for (LegacyCompany c : data.companies()) {
            report.companiesRead++;
            long id = LegacyIds.company(c.number());
            boolean added = !existing.contains(id);
            // simboli del database attuale all'inizio del nome: stelle e pollici (US-514)
            LegacyCompanyMarks marks = LegacyCompanyMarks.of(LegacyText.clean(c.name()));
            report.count(marks);
            String name = marks.name();
            if (name == null) {
                report.unnamed++;
                report.unnamedNumbers.add(c.number());
                name = CompanyPlaceholders.name(c.number());
            } else {
                byName.computeIfAbsent(LegacyText.normalize(name), k -> new ArrayList<>()).add(c.number());
            }
            Long legal = municipality(matcher, c.legalMunicipality(), c.number(), report.legalByRule, report);
            Long operating = municipality(matcher, c.operatingMunicipality(), c.number(), report.operatingByRule,
                    report);
            Long category = category(c.category(), categories, report);
            ContactDetails details = ContactDetails.of(c);
            if (!details.originals().isEmpty()) {
                report.contactDetailNotes++;
            }
            Timestamp created = timestamp(c.registeredAt(), now);
            companies.add(new Object[] {id, name, CompanyPlaceholders.VAT_CODE, LegacyText.clean(c.legalAddress()),
                    legal, LegacyText.clean(c.operatingAddress()), operating, category, LegacyText.clean(c.activity()),
                    details.phone(), details.mobile(), details.fax(), details.email(), details.secondaryEmail(),
                    marks.reliability(), marks.projectInterest(), created, LegacyIds.AUTHOR, Timestamp.from(now),
                    LegacyIds.AUTHOR});

            List<Object[]> companyContacts = contacts(c, id, roles, created, report);
            String note = LegacyText.clean(c.note());
            if (note != null) {
                report.notesRead++;
            }
            if (!added) {
                // azienda già importata: referenti, note e audit sono quelli di allora, magari corretti a mano
                continue;
            }
            report.companiesAdded++;
            report.contactsAdded += companyContacts.size();
            contacts.addAll(companyContacts);
            if (note != null) {
                Timestamp when = timestamp(c.updatedAt() != null ? c.updatedAt() : c.registeredAt(), now);
                List<String> parts = parts(note);
                for (int i = 0; i < parts.size(); i++) {
                    notes.add(new Object[] {LegacyIds.note(c.number(), i == 0 ? 0 : i + 1), id, parts.get(i), when,
                            LegacyIds.AUTHOR});
                }
                report.notesAdded += parts.size();
            }
            if (!details.originals().isEmpty()) {
                notes.add(new Object[] {LegacyIds.note(c.number(), 1), id, details.noteText(), Timestamp.from(now),
                        LegacyIds.AUTHOR});
                report.notesAdded++;
            }
            audits.add(new Object[] {LegacyIds.audit(c.number()), id,
                    "{\"legacyNumber\": %d}".formatted(c.number()), Timestamp.from(now), LegacyIds.AUTHOR});
        }
        jdbc.batchUpdate(INSERT_COMPANY, companies);
        jdbc.batchUpdate(INSERT_CONTACT, contacts);
        jdbc.batchUpdate(INSERT_NOTE, notes);
        jdbc.batchUpdate(INSERT_AUDIT, audits);
        byName.values().stream().filter(numbers -> numbers.size() > 1).forEach(report.duplicates::add);

        writeCalls(data, now, report);
        return report;
    }

    private void writeCalls(LegacyData data, Instant now, LegacyImportReport report) {
        Set<Integer> companies = new HashSet<>();
        data.companies().forEach(c -> companies.add(c.number()));
        Set<Long> existing = ids("SELECT id FROM legacy_call");
        List<Object[]> calls = new ArrayList<>();
        for (LegacyCall c : data.calls()) {
            report.callsRead++;
            if (c.number() == null) {
                report.callsWithoutNumber++;
                continue;
            }
            int companyNumber = c.companyNumber() == null ? 0 : c.companyNumber();
            String notes = LegacyText.clean(c.notes());
            if (notes != null && NAME_IN_NOTES.matcher(notes).find()) {
                report.callsToCheck.add(c.number());
            }
            boolean linked = companies.contains(companyNumber);
            if (linked) {
                report.callsWithCompany++;
            }
            long id = LegacyIds.call(c.number());
            if (existing.contains(id)) {
                continue;
            }
            report.callsAdded++;
            calls.add(new Object[] {id, c.number(), linked ? LegacyIds.company(companyNumber) : null,
                    companyNumber, calledAt(c.date(), c.time()), LegacyText.clean(c.subject()),
                    label(data.outcomes(), c.outcome()), timestamp(c.outcomeAt(), null), notes, c.handled(),
                    c.internship(), c.stage(), c.ist(), c.workGrant(), c.hiring(), c.giovanisi(),
                    label(data.projects(), c.project()), label(data.operators(), c.operator()),
                    timestamp(c.updatedAt(), null), Timestamp.from(now), LegacyIds.AUTHOR});
        }
        report.callsToCheck.sort(null);
        jdbc.batchUpdate(INSERT_CALL, calls);
    }

    private static Long municipality(MunicipalityMatcher matcher, String text, int number,
            Map<MunicipalityMatcher.Rule, Integer> byRule, LegacyImportReport report) {
        String cleaned = LegacyText.clean(text);
        if (cleaned == null) {
            return null;
        }
        return matcher.match(cleaned).map(m -> {
            byRule.merge(m.rule(), 1, Integer::sum);
            return m.id();
        }).orElseGet(() -> {
            // nel campo del comune a volte c'è altro: le cifre lunghe (partite IVA, telefoni) restano fuori dal resoconto
            String shown = cleaned.toUpperCase(Locale.ROOT).replaceAll("\\d{5,}", "…");
            report.unmatched.computeIfAbsent(shown, k -> new ArrayList<>()).add(number);
            return null;
        });
    }

    private static Long category(Integer number, Set<Long> categories, LegacyImportReport report) {
        if (number == null || number == 0) {
            return null;
        }
        long id = LegacyIds.BUSINESS_CATEGORY + number;
        if (!categories.contains(id)) {
            report.unknownCategories++;
            return null;
        }
        report.categories++;
        return id;
    }

    /** Referenti nelle posizioni 0 e 1; senza nome e senza ruolo non c'è referente. */
    private static List<Object[]> contacts(LegacyCompany c, long companyId, Set<Long> roles, Timestamp created,
            LegacyImportReport report) {
        List<Object[]> rows = new ArrayList<>();
        String[] names = {LegacyText.clean(c.contact1()), LegacyText.clean(c.contact2())};
        Integer[] numbers = {c.role1(), c.role2()};
        for (int i = 0; i < 2; i++) {
            Long role = role(numbers[i], roles, report);
            if (names[i] == null && role == null) {
                continue;
            }
            report.contactsRead++;
            rows.add(new Object[] {LegacyIds.contact(c.number(), rows.size()), companyId, rows.size(), names[i],
                    role, created, LegacyIds.AUTHOR});
        }
        return rows;
    }

    private static Long role(Integer number, Set<Long> roles, LegacyImportReport report) {
        if (number == null || number == 0) {
            return null;
        }
        long id = LegacyIds.CONTACT_ROLE + (number == DUPLICATE_ROLE ? LEGAL_REPRESENTATIVE_ROLE : number);
        if (!roles.contains(id)) {
            report.unknownRoles++;
            return null;
        }
        return id;
    }

    /** Il diario tiene note di al massimo 2.000 caratteri: una nota più lunga va in più parti. */
    private static List<String> parts(String note) {
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < note.length() && parts.size() < MAX_NOTE_PARTS; i += NOTE_LENGTH) {
            parts.add(note.substring(i, Math.min(note.length(), i + NOTE_LENGTH)));
        }
        return parts;
    }

    private static String label(Map<Integer, String> labels, Integer code) {
        return code == null ? null : labels.get(code);
    }

    /** Giorno della chiamata con l'ora, che Access tiene in un campo a parte sul suo giorno zero. */
    private static Timestamp calledAt(LocalDateTime date, LocalDateTime time) {
        if (date == null) {
            return null;
        }
        return timestamp(date.toLocalDate().atTime(time == null ? LocalTime.MIDNIGHT : time.toLocalTime()), null);
    }

    /** Data locale di Access come istante (ora italiana); assente → {@code fallback}. */
    private static Timestamp timestamp(LocalDateTime local, Instant fallback) {
        if (local == null) {
            return fallback == null ? null : Timestamp.from(fallback);
        }
        return Timestamp.from(local.atZone(ROME).toInstant());
    }

    private List<MunicipalityMatcher.Candidate> municipalities() {
        return jdbc.query("""
                SELECT m.id, m.name, p.abbreviation FROM municipality m JOIN province p ON p.id = m.province_id
                WHERE m.valid_to IS NULL""",
                (rs, i) -> new MunicipalityMatcher.Candidate(rs.getLong(1), rs.getString(2), rs.getString(3)));
    }

    private Set<Long> ids(String sql) {
        return new HashSet<>(jdbc.queryForList(sql, Long.class));
    }

    /** Alias dei comuni ({@code testo;comune}), con i commenti che cominciano con {@code #}. */
    static Map<String, String> aliases() {
        Map<String, String> aliases = new HashMap<>();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                new ClassPathResource(ALIASES).getInputStream(), StandardCharsets.UTF_8))) {
            in.lines().map(String::strip).filter(l -> !l.isEmpty() && !l.startsWith("#")).forEach(l -> {
                String[] parts = l.split(";", 2);
                aliases.put(parts[0].strip(), parts[1].strip());
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return aliases;
    }

    /** Recapiti dell'azienda nel formato dei campi, e i testi originali che non ci stanno. */
    private record ContactDetails(String phone, String mobile, String fax, String email, String secondaryEmail,
            Map<String, String> originals) {

        static ContactDetails of(LegacyCompany c) {
            Map<String, String> originals = new LinkedHashMap<>();
            ContactCleaner.Phone phone = ContactCleaner.phone(c.phone());
            ContactCleaner.Phone mobile = ContactCleaner.phone(c.mobile());
            ContactCleaner.Phone fax = ContactCleaner.phone(c.fax());
            ContactCleaner.Emails first = ContactCleaner.emails(c.email());
            ContactCleaner.Emails second = ContactCleaner.emails(c.secondaryEmail());
            original(originals, "Telefono", phone.original());
            original(originals, "Cellulare", mobile.original());
            original(originals, "Fax", fax.original());
            original(originals, "Email", first.original());
            original(originals, "Seconda email", second.original());
            Map<String, String> emails = new LinkedHashMap<>();
            Stream.concat(first.valid().stream(), second.valid().stream())
                    .forEach(e -> emails.putIfAbsent(e.toLowerCase(Locale.ROOT), e));
            List<String> all = new ArrayList<>(emails.values());
            if (all.size() > 2) {
                original(originals, "Altre email", String.join(", ", all.subList(2, all.size())));
            }
            return new ContactDetails(phone.value(), mobile.value(), fax.value(), all.isEmpty() ? null : all.get(0),
                    all.size() < 2 ? null : all.get(1), originals);
        }

        private static void original(Map<String, String> originals, String label, String text) {
            if (text != null) {
                originals.put(label, text);
            }
        }

        String noteText() {
            StringBuilder text = new StringBuilder("Recapiti del database attuale che non stanno nei campi:");
            originals.forEach((label, value) -> text.append("\n- ").append(label).append(": «").append(value)
                    .append('»'));
            return text.length() > NOTE_LENGTH ? text.substring(0, NOTE_LENGTH) : text.toString();
        }
    }
}
