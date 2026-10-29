package it.nexus.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.config.TimeConfig;
import it.nexus.domain.AppUser;
import it.nexus.domain.Company;
import it.nexus.domain.Ticket;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.AtecoCodeDTO;
import it.nexus.domain.dto.InternshipDTO;
import it.nexus.domain.dto.LegalRepresentativeDTO;
import it.nexus.domain.dto.ScheduleDayDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.InternshipDocumentKind;
import it.nexus.domain.workflow.InternshipHours;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.TicketRepository;
import it.nexus.services.AuditService;
import it.nexus.services.InternshipDocumentService;
import it.nexus.services.InternshipService;
import it.nexus.services.documents.DocxTemplate;
import it.nexus.web.errors.NotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class InternshipDocumentServiceImpl implements InternshipDocumentService {

    /** Segnaposto che i modelli possono usare; un test controlla che i modelli forniti non ne usino altri. */
    public static final List<String> PLACEHOLDERS = List.of("NUMERO_SEGNALAZIONE", "PROGETTO", "DATA_DOCUMENTO",
            "TIPO_TIROCINIO", "TUTOR_PROMOTORE", "RAGIONE_SOCIALE", "PARTITA_IVA", "SEDE_LEGALE", "CODICE_ATECO",
            "LEGALE_RAPPRESENTANTE", "CODICE_FISCALE_LEGALE_RAPPRESENTANTE", "DATA_NASCITA_LEGALE_RAPPRESENTANTE",
            "LUOGO_NASCITA_LEGALE_RAPPRESENTANTE", "RESIDENZA_LEGALE_RAPPRESENTANTE", "NOME_TIROCINANTE",
            "CODICE_FISCALE_TIROCINANTE", "DATA_NASCITA_TIROCINANTE", "LUOGO_NASCITA_TIROCINANTE",
            "RESIDENZA_TIROCINANTE", "MANSIONE", "SEDE_TIROCINIO", "DATA_INIZIO", "DATA_FINE", "SOSPENSIONI",
            "TUTOR_AZIENDALI", "ORE_SETTIMANALI", "MONTE_ORE_TOTALE", "GRIGLIA_ORARIA");
    /** Dato mancante: una riga da completare a mano, come su un modulo cartaceo. */
    public static final String BLANK = "____________";

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALIAN);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.ITALIAN);

    private final InternshipService internshipService;
    private final TicketRepository ticketRepository;
    private final AuditService auditService;
    private final Clock clock;

    @Override
    public GeneratedDocument generate(String ticketId, InternshipDocumentKind kind) {
        // accesso e stato come la scheda: solo il Tutor della segnalazione, dall'appuntamento fissato in poi
        InternshipDTO scheda = internshipService.get(ticketId);
        Ticket ticket = ticketRepository.findById(TsidMapper.toInternal(ticketId))
                .orElseThrow(() -> new NotFoundException("Segnalazione non trovata"));
        byte[] content;
        try (InputStream template = template(kind, scheda)) {
            content = DocxTemplate.fill(template, values(ticket, scheda));
        } catch (IOException e) {
            throw new UncheckedIOException("Modello " + kind + " non leggibile", e);
        }
        auditService.record(AuditEntityType.TICKET, ticket.getId(), "DOCUMENT_GENERATED",
                AuditChanges.of().value("document", null, kind.name()), null);
        return new GeneratedDocument("%s-segnalazione-%d.docx".formatted(kind.filePrefix(), ticket.getNumber()),
                content);
    }

    /** Il modello del tipo di tirocinio oppure, per i tipi senza modello (aggiunti dall'ADMIN), quello predefinito. */
    private static InputStream template(InternshipDocumentKind kind, InternshipDTO scheda) throws IOException {
        String folder = "templates/documents/" + kind.slug() + "/";
        if (scheda.internshipType() != null) {
            ClassPathResource own = new ClassPathResource(folder + scheda.internshipType().code() + ".docx");
            if (own.exists()) {
                return own.getInputStream();
            }
        }
        return new ClassPathResource(folder + "default.docx").getInputStream();
    }

    private Map<String, String> values(Ticket ticket, InternshipDTO s) {
        Company company = ticket.getJobSlot() == null ? null : ticket.getJobSlot().getCompany();
        LegalRepresentativeDTO rep = s.legalRepresentative();
        InternshipHours.Slots slots = new InternshipHours.Slots(s.firstSlotStart(), s.firstSlotEnd(),
                s.secondSlotStart(), s.secondSlotEnd());
        Map<String, String> values = new HashMap<>();
        values.put("NUMERO_SEGNALAZIONE", String.valueOf(ticket.getNumber()));
        values.put("PROGETTO", ticket.getProject().getName());
        values.put("DATA_DOCUMENTO", DAY.format(LocalDate.ofInstant(clock.instant(), TimeConfig.ZONE)));
        values.put("TIPO_TIROCINIO", s.internshipType() == null ? null : s.internshipType().name());
        values.put("TUTOR_PROMOTORE", fullName(ticket.getTutor()));
        values.put("RAGIONE_SOCIALE", company == null ? null : company.getName());
        values.put("PARTITA_IVA", company == null ? null : company.getVatCode());
        values.put("SEDE_LEGALE", company == null ? null : company.getLegalAddress());
        values.put("CODICE_ATECO", s.atecoCodes().stream()
                .filter(a -> a.id().equals(s.atecoCodeId()))
                .findFirst()
                .map(InternshipDocumentServiceImpl::ateco)
                .orElse(null));
        values.put("LEGALE_RAPPRESENTANTE", rep == null ? null : rep.firstName() + " " + rep.lastName());
        values.put("CODICE_FISCALE_LEGALE_RAPPRESENTANTE", rep == null ? null : rep.fiscalCode());
        values.put("DATA_NASCITA_LEGALE_RAPPRESENTANTE", rep == null ? null : day(rep.birthDate()));
        values.put("LUOGO_NASCITA_LEGALE_RAPPRESENTANTE", rep == null ? null : rep.birthPlace());
        values.put("RESIDENZA_LEGALE_RAPPRESENTANTE", rep == null ? null : rep.residence());
        values.put("NOME_TIROCINANTE", ticket.getBeneficiary().getFirstName() == null ? null
                : ticket.getBeneficiary().getFirstName() + " " + ticket.getBeneficiary().getLastName());
        values.put("CODICE_FISCALE_TIROCINANTE", s.fiscalCode());
        values.put("DATA_NASCITA_TIROCINANTE", day(s.birthDate()));
        values.put("LUOGO_NASCITA_TIROCINANTE", s.birthPlace());
        values.put("RESIDENZA_TIROCINANTE", s.residence());
        values.put("MANSIONE", ticket.getJobSlot() == null ? null : ticket.getJobSlot().getTitle());
        values.put("SEDE_TIROCINIO", s.site());
        values.put("DATA_INIZIO", day(s.startDate()));
        values.put("DATA_FINE", day(s.expectedEndDate()));
        values.put("SOSPENSIONI", s.suspensions().isEmpty() ? "nessuno" : s.suspensions().stream()
                .map(p -> "dal %s al %s".formatted(DAY.format(p.from()), DAY.format(p.to())))
                .collect(Collectors.joining("; ")));
        values.put("TUTOR_AZIENDALI", s.companyTutors().isEmpty() ? null : s.companyTutors().stream()
                .map(t -> t.contact() == null ? t.fullName() : "%s (%s)".formatted(t.fullName(), t.contact()))
                .collect(Collectors.joining("; ")));
        values.put("ORE_SETTIMANALI", s.weeklyMinutes() == null ? null : hours(s.weeklyMinutes()));
        values.put("MONTE_ORE_TOTALE", s.totalMinutes() == null ? null : hours(s.totalMinutes()));
        values.put("GRIGLIA_ORARIA", s.schedule().isEmpty() ? null : s.schedule().stream()
                .map(d -> scheduleLine(slots, d))
                .collect(Collectors.joining("\n")));
        // ogni segnaposto noto ha un valore: quelli mancanti diventano una riga vuota
        values.replaceAll((key, value) -> value == null || value.isBlank() ? BLANK : value);
        return values;
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
        return TIME.format(from) + "–" + TIME.format(to);
    }

    /** Ore da minuti, con la virgola: 240 → "4", 270 → "4,5", 255 → "4,25". */
    private static String hours(int minutes) {
        return minutes % 60 == 0 ? String.valueOf(minutes / 60)
                : String.valueOf(minutes / 60.0).replace('.', ',');
    }

    private static String ateco(AtecoCodeDTO a) {
        return a.description() == null ? a.code() : a.code() + " – " + a.description();
    }

    private static String day(LocalDate date) {
        return date == null ? null : DAY.format(date);
    }

    private static String fullName(AppUser user) {
        return Objects.toString(user.getFirstName(), "") + " " + Objects.toString(user.getLastName(), "");
    }
}
