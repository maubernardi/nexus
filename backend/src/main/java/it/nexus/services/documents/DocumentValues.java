package it.nexus.services.documents;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllformedLocaleException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import it.nexus.config.ApplicationProperties;
import it.nexus.config.TimeConfig;
import it.nexus.domain.AbstractReferenceEntity;
import it.nexus.domain.AppUser;
import it.nexus.domain.Beneficiary;
import it.nexus.domain.CareAgency;
import it.nexus.domain.ClosureReport;
import it.nexus.domain.Company;
import it.nexus.domain.Project;
import it.nexus.domain.Ticket;
import it.nexus.domain.dto.AtecoCodeDTO;
import it.nexus.domain.dto.ExtensionDTO;
import it.nexus.domain.dto.InternshipDTO;
import it.nexus.domain.dto.LegalRepresentativeDTO;
import it.nexus.domain.dto.MunicipalityDTO;
import it.nexus.domain.dto.ScheduleDayDTO;
import it.nexus.domain.dto.TrainingProjectDTO;
import it.nexus.domain.enumeration.CareAgencyKind;
import it.nexus.domain.enumeration.EducationLevel;
import it.nexus.domain.enumeration.Gender;
import it.nexus.domain.workflow.InternshipHours;
import it.nexus.domain.workflow.InternshipMonths;
import it.nexus.mapper.MunicipalityMapper;

/**
 * Valori dei segnaposto di {@link DocumentPlaceholders} per un documento del tirocinio (US-1004…US-1011): dati della
 * segnalazione, della scheda, del promotore, della convenzione e del progetto formativo del documento, della chiusura.
 * Un valore mancante diventa la riga {@link #BLANK} da completare a mano; fanno eccezione le caselle, sempre ☒ o ☐, e
 * le fasce per giorno e la descrizione dell'interruzione, vuote se non ci sono (D7).
 */
public final class DocumentValues {

    /** Dato mancante: una riga da completare a mano, come su un modulo cartaceo. */
    public static final String BLANK = "____________";

    public static final String CHECKED = "☒";
    public static final String UNCHECKED = "☐";

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALIAN);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.ITALIAN);

    /** Segnaposto che restano vuoti invece della riga da completare: celle della griglia e testo facoltativo. */
    private static final Set<String> EMPTY_ALLOWED = emptyAllowed();

    /** Una convenzione: quella iniziale della scheda oppure una proroga (US-1012). */
    public record Convention(String number, LocalDate date, LocalDate signedDate, LocalDate from, LocalDate to) {

        public static Convention initial(InternshipDTO s) {
            return new Convention(s.agreementNumber(), s.agreementDate(), s.agreementSignedDate(), s.startDate(),
                    s.expectedEndDate());
        }

        public static Convention of(ExtensionDTO e) {
            return new Convention(e.agreementNumber(), e.agreementDate(), e.agreementSignedDate(), e.from(), e.to());
        }
    }

    /**
     * Da dove vengono i valori. {@code project} è il progetto formativo del documento (nessuno per la convenzione, la
     * relazione finale e il progetto formativo senza progetti); {@code report} il report di restituzione, se inviato.
     */
    public record Sources(Ticket ticket, InternshipDTO scheda, ApplicationProperties.Promoter promoter,
            Convention convention, TrainingProjectDTO project, ClosureReport report, LocalDate today) {
    }

    private DocumentValues() {
    }

    public static Map<String, String> of(Sources src) {
        Map<String, String> v = new HashMap<>();
        Ticket ticket = src.ticket();
        InternshipDTO s = src.scheda();
        ticketValues(v, ticket, s, src.today());
        promoterValues(v, src.promoter());
        agencyValues(v, ticket.getBeneficiary());
        companyValues(v, ticket, s);
        beneficiaryValues(v, ticket.getBeneficiary(), s);
        internshipValues(v, ticket, s, src.report());
        scheduleValues(v, s);
        conventionValues(v, src.convention(), src.project());
        closureValues(v, src.report());
        // ogni segnaposto noto ha un valore: quelli mancanti diventano una riga vuota
        for (DocumentPlaceholders.Placeholder p : DocumentPlaceholders.ALL) {
            String value = v.get(p.name());
            if (value == null || value.isBlank()) {
                v.put(p.name(), EMPTY_ALLOWED.contains(p.name()) ? "" : BLANK);
            }
        }
        return v;
    }

    private static void ticketValues(Map<String, String> v, Ticket ticket, InternshipDTO s, LocalDate today) {
        Project project = ticket.getProject();
        v.put("NUMERO_SEGNALAZIONE", String.valueOf(ticket.getNumber()));
        v.put("PROGETTO", project.getName());
        v.put("INTESTAZIONE_PROGETTO", project.getHeaderLine());
        v.put("AVVISO", project.effectiveNoticeReference());
        v.put("TIPO_TIROCINIO", s.internshipType() == null ? null : s.internshipType().name());
        v.put("SEDE_COMPETENZA", name(ticket.getCompetenceSite()));
        v.put("TUTOR_PROMOTORE", fullName(ticket.getTutor()));
        v.put("DATA_DOCUMENTO", DAY.format(today));
    }

    private static void promoterValues(Map<String, String> v, ApplicationProperties.Promoter p) {
        v.put("PROMOTORE", p.getName());
        v.put("SEDE_PROMOTORE", p.getAddress());
        v.put("CODICE_FISCALE_PROMOTORE", p.getFiscalCode());
        v.put("LEGALE_RAPPRESENTANTE_PROMOTORE", p.getRepresentative());
        v.put("RUOLO_LEGALE_RAPPRESENTANTE_PROMOTORE", p.getRepresentativeRole());
        v.put("LUOGO_NASCITA_LEGALE_RAPPRESENTANTE_PROMOTORE", p.getRepresentativeBirthPlace());
        v.put("DATA_NASCITA_LEGALE_RAPPRESENTANTE_PROMOTORE", p.getRepresentativeBirthDate());
        v.put("TELEFONO_PROMOTORE", p.getPhone());
        v.put("EMAIL_PROMOTORE", p.getEmail());
        v.put("PEC_PROMOTORE", p.getPec());
        v.put("POSIZIONE_INAIL", p.getInailPosition());
        v.put("COMPAGNIA_RC", p.getRcInsurer());
        v.put("POLIZZA_RC", p.getRcPolicy());
        v.put("SCADENZA_RC", p.getRcExpiry());
    }

    private static void agencyValues(Map<String, String> v, Beneficiary b) {
        CareAgency a = b.getCareAgency();
        for (CareAgencyKind kind : CareAgencyKind.values()) {
            v.put(DocumentPlaceholders.agencyKind(kind), box(a != null && a.getKind() == kind));
        }
        if (a != null) {
            v.put("ENTE", a.getName());
            v.put("SEDE_ENTE", a.getLegalAddress());
            v.put("CODICE_FISCALE_ENTE", a.getFiscalCode());
            v.put("RAPPRESENTANTE_ENTE", a.getRepresentativeName());
            v.put("QUALIFICA_RAPPRESENTANTE_ENTE", a.getRepresentativeRole());
            v.put("LUOGO_NASCITA_RAPPRESENTANTE_ENTE", a.getRepresentativeBirthPlace());
            v.put("DATA_NASCITA_RAPPRESENTANTE_ENTE", day(a.getRepresentativeBirthDate()));
            v.put("REFERENTE_ENTE", a.getReferentName());
            v.put("TELEFONO_REFERENTE_ENTE", a.getReferentPhone());
            v.put("EMAIL_REFERENTE_ENTE", a.getReferentEmail());
        }
        v.put("CASE_MANAGER", b.getCaseManagerName());
        v.put("SERVIZIO_CASE_MANAGER", b.getCaseManagerOffice());
        v.put("TELEFONO_CASE_MANAGER", b.getCaseManagerPhone());
        v.put("EMAIL_CASE_MANAGER", b.getCaseManagerEmail());
    }

    private static void companyValues(Map<String, String> v, Ticket ticket, InternshipDTO s) {
        Company company = ticket.getJobSlot() == null ? null : ticket.getJobSlot().getCompany();
        if (company != null) {
            MunicipalityDTO legal = MunicipalityMapper.toDto(company.getLegalMunicipality());
            v.put("RAGIONE_SOCIALE", company.getName());
            v.put("PARTITA_IVA", company.getVatCode());
            v.put("SEDE_LEGALE", AddressLine.of(company.getLegalAddress(), legal));
            v.put("COMUNE_SEDE_LEGALE", legal == null ? null : legal.name());
            v.put("PROVINCIA_SEDE_LEGALE", legal == null ? null : legal.provinceAbbreviation());
            v.put("TELEFONO_AZIENDA", company.getPhone());
            v.put("EMAIL_AZIENDA", company.getEmail());
        }
        v.put("CODICE_ATECO", s.atecoCodes().stream()
                .filter(a -> a.id().equals(s.atecoCodeId()))
                .findFirst()
                .map(DocumentValues::ateco)
                .orElse(null));
        LegalRepresentativeDTO rep = s.legalRepresentative();
        if (rep != null) {
            v.put("LEGALE_RAPPRESENTANTE", rep.firstName() + " " + rep.lastName());
            v.put("CODICE_FISCALE_LEGALE_RAPPRESENTANTE", rep.fiscalCode());
            v.put("DATA_NASCITA_LEGALE_RAPPRESENTANTE", day(rep.birthDate()));
            v.put("LUOGO_NASCITA_LEGALE_RAPPRESENTANTE", rep.birthPlace());
            v.put("RESIDENZA_LEGALE_RAPPRESENTANTE", rep.residence());
        }
        v.put("TUTOR_AZIENDALI", s.companyTutors().isEmpty() ? null : s.companyTutors().stream()
                .map(CompanyTutorLine::of)
                .collect(Collectors.joining("; ")));
    }

    private static void beneficiaryValues(Map<String, String> v, Beneficiary b, InternshipDTO s) {
        v.put("NOME_TIROCINANTE", b.getFirstName() == null ? null : b.getFirstName() + " " + b.getLastName());
        v.put("NOME_PROPRIO_TIROCINANTE", b.getFirstName());
        v.put("COGNOME_TIROCINANTE", b.getLastName());
        v.put("SESSO_M", box(b.getGender() == Gender.M));
        v.put("SESSO_F", box(b.getGender() == Gender.F));
        v.put("CODICE_FISCALE_TIROCINANTE", s.fiscalCode());
        v.put("DATA_NASCITA_TIROCINANTE", day(s.birthDate()));
        v.put("LUOGO_NASCITA_TIROCINANTE", s.birthPlace());
        v.put("CITTADINANZA_TIROCINANTE", country(b.getCitizenship()));
        v.put("TITOLO_STUDIO_TIROCINANTE", education(b.getEducationLevel()));
        v.put("TELEFONO_TIROCINANTE", b.getPhone());
        v.put("EMAIL_TIROCINANTE", b.getEmail());
        MunicipalityDTO residence = s.residenceMunicipality();
        v.put("RESIDENZA_TIROCINANTE", AddressLine.of(s.residenceStreet(), s.residenceStreetNumber(), residence));
        v.put("COMUNE_RESIDENZA_TIROCINANTE", residence == null ? null : residence.name());
        v.put("PROVINCIA_RESIDENZA_TIROCINANTE", residence == null ? null : residence.provinceAbbreviation());
        v.put("VIA_RESIDENZA_TIROCINANTE", s.residenceStreet());
        v.put("CIVICO_RESIDENZA_TIROCINANTE", s.residenceStreetNumber());
    }

    private static void internshipValues(Map<String, String> v, Ticket ticket, InternshipDTO s,
            ClosureReport report) {
        v.put("MANSIONE", ticket.getJobSlot() == null ? null : ticket.getJobSlot().getTitle());
        MunicipalityDTO site = s.siteMunicipality();
        v.put("SEDE_TIROCINIO", AddressLine.of(s.site(), site));
        v.put("COMUNE_SEDE_TIROCINIO", site == null ? null : site.name());
        v.put("PROVINCIA_SEDE_TIROCINIO", site == null ? null : site.provinceAbbreviation());
        v.put("NOME_PROVINCIA_SEDE_TIROCINIO", site == null ? null : site.provinceName());
        v.put("DATA_INIZIO", day(s.startDate()));
        v.put("DATA_FINE", day(s.currentEndDate() != null ? s.currentEndDate() : s.expectedEndDate()));
        v.put("MESI_TIROCINIO", months(s, report));
        v.put("SOSPENSIONI", s.suspensions().isEmpty() ? "nessuno" : s.suspensions().stream()
                .map(p -> "dal %s al %s".formatted(DAY.format(p.from()), DAY.format(p.to())))
                .collect(Collectors.joining("; ")));
        v.put("ORE_SETTIMANALI", s.weeklyMinutes() == null ? null : hours(s.weeklyMinutes()));
        v.put("MONTE_ORE_TOTALE", s.totalMinutes() == null ? null : hours(s.totalMinutes()));
    }

    /**
     * Mesi del tirocinio sulle convenzioni (D4), fino alla fine effettiva oppure, se non è chiuso, alla fine prevista.
     * Un report senza la fine effettiva (inviato prima di US-1009) lascia la riga da completare.
     */
    private static String months(InternshipDTO s, ClosureReport report) {
        if (s.startDate() == null || s.expectedEndDate() == null) {
            return null;
        }
        LocalDate end;
        if (report == null) {
            end = s.currentEndDate() != null ? s.currentEndDate() : s.expectedEndDate();
        } else if (report.getActualEndDate() != null) {
            end = report.getActualEndDate();
        } else {
            return null;
        }
        List<InternshipMonths.Span> spans = new ArrayList<>();
        spans.add(new InternshipMonths.Span(s.startDate(), s.expectedEndDate()));
        s.extensions().forEach(e -> spans.add(new InternshipMonths.Span(e.from(), e.to())));
        return String.valueOf(InternshipMonths.roundedTotal(spans, end));
    }

    private static void scheduleValues(Map<String, String> v, InternshipDTO s) {
        InternshipHours.Slots slots = new InternshipHours.Slots(s.firstSlotStart(), s.firstSlotEnd(),
                s.secondSlotStart(), s.secondSlotEnd());
        v.put("GRIGLIA_ORARIA", s.schedule().isEmpty() ? null : s.schedule().stream()
                .map(d -> scheduleLine(slots, d))
                .collect(Collectors.joining("\n")));
        Map<Integer, ScheduleDayDTO> byDay = s.schedule().stream()
                .collect(Collectors.toMap(ScheduleDayDTO::dayOfWeek, Function.identity(), (a, b) -> a));
        boolean morning = false;
        boolean afternoon = false;
        for (int d = 0; d < DocumentPlaceholders.DAYS.size(); d++) {
            String name = DocumentPlaceholders.DAYS.get(d);
            ScheduleDayDTO day = byDay.get(d + 1);
            v.put("GIORNO_" + name, box(day != null));
            boolean first = day != null && day.firstSlot();
            boolean second = day != null && day.secondSlot();
            v.put(name + "_MATTINA", first ? range(slots.firstStart(), slots.firstEnd()) : null);
            v.put(name + "_POMERIGGIO", second ? range(slots.secondStart(), slots.secondEnd()) : null);
            morning |= first;
            afternoon |= second;
        }
        v.put("MATTINA_DALLE", morning ? time(slots.firstStart()) : null);
        v.put("MATTINA_ALLE", morning ? time(slots.firstEnd()) : null);
        v.put("POMERIGGIO_DALLE", afternoon ? time(slots.secondStart()) : null);
        v.put("POMERIGGIO_ALLE", afternoon ? time(slots.secondEnd()) : null);
    }

    private static void conventionValues(Map<String, String> v, Convention c, TrainingProjectDTO p) {
        v.put("NUMERO_CONVENZIONE", c.number());
        v.put("DATA_CONVENZIONE", day(c.date()));
        v.put("DATA_STIPULA_CONVENZIONE", day(c.signedDate()));
        v.put("INIZIO_CONVENZIONE", day(c.from()));
        v.put("FINE_CONVENZIONE", day(c.to()));
        if (p != null) {
            v.put("NUMERO_PROGETTO_FORMATIVO", String.valueOf(p.number()));
            v.put("INIZIO_PROGETTO_FORMATIVO", day(p.from()));
            v.put("FINE_PROGETTO_FORMATIVO", day(p.to()));
            v.put("MESI_PROGETTO_FORMATIVO", String.valueOf(p.months()));
            v.put("ORE_PROGETTO_FORMATIVO", p.totalMinutes() == null ? null : hours(p.totalMinutes()));
            v.put("MANSIONE_PROGETTO_FORMATIVO", p.jobTitle());
            v.put("DETTAGLIO_MANSIONE_PROGETTO_FORMATIVO", p.jobDetails());
        }
    }

    private static void closureValues(Map<String, String> v, ClosureReport report) {
        if (report == null) {
            return;
        }
        v.put("DATA_FINE_EFFETTIVA", day(report.getActualEndDate()));
        v.put("DATA_CHIUSURA", report.getCreatedAt() == null ? null
                : DAY.format(LocalDate.ofInstant(report.getCreatedAt(), TimeConfig.ZONE)));
        v.put("MOTIVO_CHIUSURA", name(report.getClosureReason()));
        v.put("DESCRIZIONE_INTERRUZIONE", report.getInterruptionDetails());
    }

    private static Set<String> emptyAllowed() {
        Set<String> names = new HashSet<>(Set.of("DESCRIZIONE_INTERRUZIONE"));
        DocumentPlaceholders.DAYS.forEach(d -> {
            names.add(d + "_MATTINA");
            names.add(d + "_POMERIGGIO");
        });
        return Set.copyOf(names);
    }

    /** Una riga della griglia: "Mercoledì: 09:00–13:00 e 14:00–18:00 (6 ore)". */
    private static String scheduleLine(InternshipHours.Slots slots, ScheduleDayDTO d) {
        String dayName = DayOfWeek.of(d.dayOfWeek()).getDisplayName(TextStyle.FULL, Locale.ITALIAN);
        String first = range(slots.firstStart(), slots.firstEnd());
        String second = range(slots.secondStart(), slots.secondEnd());
        String ranges = d.firstSlot() && d.secondSlot() ? first + " e " + second : d.firstSlot() ? first : second;
        int minutes = InternshipHours.dayMinutes(slots,
                new InternshipHours.Day(DayOfWeek.of(d.dayOfWeek()), d.firstSlot(), d.secondSlot(), d.minutes()));
        return "%s: %s (%s %s)".formatted(Character.toUpperCase(dayName.charAt(0)) + dayName.substring(1), ranges,
                hours(minutes), minutes == 60 ? "ora" : "ore");
    }

    private static String range(LocalTime from, LocalTime to) {
        return time(from) + "–" + time(to);
    }

    private static String time(LocalTime time) {
        return time == null ? null : TIME.format(time);
    }

    /** Ore da minuti, con la virgola: 240 → "4", 270 → "4,5", 255 → "4,25". */
    private static String hours(int minutes) {
        return minutes % 60 == 0 ? String.valueOf(minutes / 60)
                : String.valueOf(minutes / 60.0).replace('.', ',');
    }

    private static String box(boolean checked) {
        return checked ? CHECKED : UNCHECKED;
    }

    private static String ateco(AtecoCodeDTO a) {
        return a.description() == null ? a.code() : a.code() + " – " + a.description();
    }

    /** Il Paese dal codice ISO 3166-1 alpha-2 dell'anagrafica, in italiano: «IT» → «Italia». */
    private static String country(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        try {
            String name = new Locale.Builder().setRegion(code.strip()).build().getDisplayCountry(Locale.ITALIAN);
            return name.isBlank() ? code : name;
        } catch (IllformedLocaleException e) {
            return code;
        }
    }

    /** Il titolo di studio come nell'anagrafica del beneficiario. */
    private static String education(EducationLevel level) {
        return level == null ? null : switch (level) {
            case NESSUN_TITOLO -> "Nessun titolo";
            case LICENZA_ELEMENTARE -> "Licenza elementare";
            case LICENZA_MEDIA -> "Licenza media";
            case QUALIFICA_PROFESSIONALE -> "Qualifica professionale";
            case DIPLOMA -> "Diploma di scuola superiore";
            case ITS -> "Diploma ITS";
            case LAUREA_TRIENNALE -> "Laurea triennale";
            case LAUREA_MAGISTRALE -> "Laurea magistrale";
            case DOTTORATO -> "Dottorato";
        };
    }

    private static String name(AbstractReferenceEntity item) {
        return Optional.ofNullable(item).map(AbstractReferenceEntity::getName).orElse(null);
    }

    private static String day(LocalDate date) {
        return date == null ? null : DAY.format(date);
    }

    private static String fullName(AppUser user) {
        return Objects.toString(user.getFirstName(), "") + " " + Objects.toString(user.getLastName(), "");
    }
}
