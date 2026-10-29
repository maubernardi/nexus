package it.nexus.domain;

import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Scheda amministrativa del tirocinio (US-1001), una per segnalazione: la compila il Tutor dopo l'appuntamento fissato
 * e la corregge in qualunque momento. Campi segnaposto fino ai moduli ufficiali (Q5), tutti facoltativi. Contiene dati personali del beneficiario e dei
 * tutor aziendali: {@code toString()} senza valori (da {@link AbstractTsidEntity}), mai nei log.
 */
@Getter
@Setter
@Entity
@Table(name = "internship")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Internship extends AbstractTsidEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    // fasce precompilate, come i default di V24
    private static final LocalTime DEFAULT_FIRST_START = LocalTime.of(9, 0);
    private static final LocalTime DEFAULT_FIRST_END = LocalTime.of(13, 0);
    private static final LocalTime DEFAULT_SECOND_START = LocalTime.of(14, 0);
    private static final LocalTime DEFAULT_SECOND_END = LocalTime.of(18, 0);

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false, unique = true, updatable = false)
    @Setter(AccessLevel.NONE)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_type_id")
    private InternshipType internshipType;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "expected_end_date")
    private LocalDate expectedEndDate;

    @Column(name = "site", length = 300)
    private String site;

    /** Codice scelto tra quelli dell'azienda della segnalazione (US-506). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ateco_code_id")
    private CompanyAtecoCode atecoCode;

    @Column(name = "fiscal_code", length = 16)
    private String fiscalCode;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "birth_place", length = 200)
    private String birthPlace;

    @Column(name = "residence", length = 300)
    private String residence;

    @Column(name = "notes", length = 2000)
    private String notes;

    /** Fasce orarie della griglia (US-1002), precompilate e modificabili; in minuti nel database. */
    @Convert(converter = MinuteOfDayConverter.class)
    @Column(name = "first_slot_start", nullable = false)
    private LocalTime firstSlotStart = DEFAULT_FIRST_START;

    @Convert(converter = MinuteOfDayConverter.class)
    @Column(name = "first_slot_end", nullable = false)
    private LocalTime firstSlotEnd = DEFAULT_FIRST_END;

    @Convert(converter = MinuteOfDayConverter.class)
    @Column(name = "second_slot_start", nullable = false)
    private LocalTime secondSlotStart = DEFAULT_SECOND_START;

    @Convert(converter = MinuteOfDayConverter.class)
    @Column(name = "second_slot_end", nullable = false)
    private LocalTime secondSlotEnd = DEFAULT_SECOND_END;

    /** Il monte ore salta le festività nazionali (US-1002); di default sì. */
    @Column(name = "exclude_holidays", nullable = false)
    private boolean excludeHolidays = true;

    @OneToMany(mappedBy = "internship", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<InternshipCompanyTutor> companyTutors = new ArrayList<>();

    @OneToMany(mappedBy = "internship", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dateFrom")
    private List<InternshipSuspension> suspensions = new ArrayList<>();

    @OneToMany(mappedBy = "internship", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayOfWeek")
    private List<InternshipScheduleDay> scheduleDays = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    @Setter(AccessLevel.NONE)
    private long version;

    public Internship(Ticket ticket) {
        this.ticket = ticket;
    }

    public void addCompanyTutor(String fullName, String contact) {
        companyTutors.add(new InternshipCompanyTutor(this, fullName, contact));
    }

    public void addSuspension(LocalDate from, LocalDate to) {
        suspensions.add(new InternshipSuspension(this, from, to));
    }

    public void addScheduleDay(int dayOfWeek, boolean firstSlot, boolean secondSlot, Integer minutes) {
        scheduleDays.add(new InternshipScheduleDay(this, dayOfWeek, firstSlot, secondSlot, minutes));
    }

    /**
     * Appuntamento non andato a buon fine (D7): i dati del tirocinio riguardavano quell'azienda e si azzerano; restano
     * quelli del beneficiario.
     */
    public void clearCompanyData() {
        internshipType = null;
        startDate = null;
        expectedEndDate = null;
        site = null;
        atecoCode = null;
        notes = null;
        firstSlotStart = DEFAULT_FIRST_START;
        firstSlotEnd = DEFAULT_FIRST_END;
        secondSlotStart = DEFAULT_SECOND_START;
        secondSlotEnd = DEFAULT_SECOND_END;
        excludeHolidays = true;
        companyTutors.clear();
        suspensions.clear();
        scheduleDays.clear();
    }
}
