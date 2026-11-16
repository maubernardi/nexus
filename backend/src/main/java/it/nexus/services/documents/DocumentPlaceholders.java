package it.nexus.services.documents;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Segnaposto che NEXUS sa compilare nei documenti del tirocinio (US-1004, US-1007, US-1009, US-1011): i modelli nel
 * codice e quelli caricati dall'ADMIN (US-1010) possono usare solo questi. Ogni voce ha un gruppo e una descrizione,
 * mostrati all'ADMIN nella pagina dei modelli; l'ordine è quello della pagina.
 */
public final class DocumentPlaceholders {

    /** Un segnaposto: si scrive {@code {NOME}} nel modello. */
    public record Placeholder(String name, String group, String description) {
    }

    /** Giorni della settimana nei segnaposto dell'orario, da lunedì. */
    public static final List<String> DAYS = List.of("LUNEDI", "MARTEDI", "MERCOLEDI", "GIOVEDI", "VENERDI", "SABATO",
            "DOMENICA");

    private static final String[] DAY_NAMES = {"lunedì", "martedì", "mercoledì", "giovedì", "venerdì", "sabato",
            "domenica"};

    public static final List<Placeholder> ALL = build();

    public static final Set<String> KNOWN = ALL.stream().map(Placeholder::name)
            .collect(Collectors.toUnmodifiableSet());

    private DocumentPlaceholders() {
    }

    private static List<Placeholder> build() {
        List<Placeholder> all = new ArrayList<>();
        Group g = new Group(all, "Segnalazione e progetto");
        g.add("NUMERO_SEGNALAZIONE", "Numero della segnalazione");
        g.add("PROGETTO", "Nome del progetto della segnalazione, es. GOL");
        g.add("INTESTAZIONE_PROGETTO", "Riga di intestazione del progetto con codice e CUP, dai cataloghi");
        g.add("AVVISO", "Riferimento all'avviso del progetto, dai cataloghi (senza: l'Avviso pubblico n. 10 di GOL)");
        g.add("TIPO_TIROCINIO", "Tipo di tirocinio della scheda");
        g.add("SEDE_COMPETENZA", "Sede di competenza della segnalazione, es. il luogo della relazione finale");
        g.add("TUTOR_PROMOTORE", "Nome e cognome del Tutor della segnalazione");
        g.add("DATA_DOCUMENTO", "Data in cui si scarica il documento");

        g = new Group(all, "Soggetto promotore");
        g.add("PROMOTORE", "Denominazione del soggetto promotore");
        g.add("SEDE_PROMOTORE", "Sede del soggetto promotore");
        g.add("CODICE_FISCALE_PROMOTORE", "Codice fiscale del soggetto promotore");
        g.add("LEGALE_RAPPRESENTANTE_PROMOTORE", "Nome e cognome del legale rappresentante del promotore");
        g.add("RUOLO_LEGALE_RAPPRESENTANTE_PROMOTORE", "Ruolo del legale rappresentante del promotore");
        g.add("LUOGO_NASCITA_LEGALE_RAPPRESENTANTE_PROMOTORE",
                "Luogo di nascita del legale rappresentante del promotore");
        g.add("DATA_NASCITA_LEGALE_RAPPRESENTANTE_PROMOTORE",
                "Data di nascita del legale rappresentante del promotore");
        g.add("TELEFONO_PROMOTORE", "Telefono del soggetto promotore");
        g.add("EMAIL_PROMOTORE", "Mail del soggetto promotore");
        g.add("PEC_PROMOTORE", "PEC del soggetto promotore");
        g.add("POSIZIONE_INAIL", "Posizione INAIL per gli infortuni sul lavoro");
        g.add("COMPAGNIA_RC", "Compagnia della polizza di responsabilità civile");
        g.add("POLIZZA_RC", "Numero della polizza di responsabilità civile");
        g.add("SCADENZA_RC", "Scadenza della polizza di responsabilità civile");

        g = new Group(all, "Ente di presa in carico");
        g.add("ENTE", "Denominazione dell'ente che ha in carico il beneficiario");
        g.add("SEDE_ENTE", "Sede legale dell'ente");
        g.add("CODICE_FISCALE_ENTE", "Codice fiscale o partita IVA dell'ente");
        g.add("RAPPRESENTANTE_ENTE", "Nome del rappresentante dell'ente");
        g.add("QUALIFICA_RAPPRESENTANTE_ENTE", "Qualifica del rappresentante dell'ente, es. dirigente");
        g.add("LUOGO_NASCITA_RAPPRESENTANTE_ENTE", "Luogo di nascita del rappresentante dell'ente");
        g.add("DATA_NASCITA_RAPPRESENTANTE_ENTE", "Data di nascita del rappresentante dell'ente");
        g.add("REFERENTE_ENTE", "Referente dell'ente per la convenzione");
        g.add("TELEFONO_REFERENTE_ENTE", "Telefono del referente dell'ente");
        g.add("EMAIL_REFERENTE_ENTE", "Mail del referente dell'ente");
        g.add("CASE_MANAGER", "Nome del case manager del beneficiario");
        g.add("SERVIZIO_CASE_MANAGER", "Servizio o ufficio del case manager");
        g.add("TELEFONO_CASE_MANAGER", "Telefono del case manager");
        g.add("EMAIL_CASE_MANAGER", "Mail del case manager");

        g = new Group(all, "Soggetto ospitante");
        g.add("RAGIONE_SOCIALE", "Ragione sociale dell'azienda");
        g.add("PARTITA_IVA", "Partita IVA dell'azienda");
        g.add("SEDE_LEGALE", "Sede legale dell'azienda in una riga, es. «Via Roma 1, Firenze (FI)»");
        g.add("COMUNE_SEDE_LEGALE", "Comune della sede legale dell'azienda");
        g.add("PROVINCIA_SEDE_LEGALE", "Sigla della provincia della sede legale, es. FI");
        g.add("TELEFONO_AZIENDA", "Telefono dell'azienda");
        g.add("EMAIL_AZIENDA", "Mail dell'azienda");
        g.add("CODICE_ATECO", "Codice ATECO della scheda con la descrizione");
        g.add("LEGALE_RAPPRESENTANTE", "Nome e cognome del legale rappresentante dell'azienda");
        g.add("CODICE_FISCALE_LEGALE_RAPPRESENTANTE", "Codice fiscale del legale rappresentante dell'azienda");
        g.add("DATA_NASCITA_LEGALE_RAPPRESENTANTE", "Data di nascita del legale rappresentante dell'azienda");
        g.add("LUOGO_NASCITA_LEGALE_RAPPRESENTANTE", "Luogo di nascita del legale rappresentante dell'azienda");
        g.add("RESIDENZA_LEGALE_RAPPRESENTANTE", "Residenza del legale rappresentante dell'azienda");
        g.add("TUTOR_AZIENDALI", "Tutor aziendali, ognuno con ruolo e recapito");

        g = new Group(all, "Tirocinante");
        g.add("NOME_TIROCINANTE", "Nome e cognome del tirocinante");
        g.add("NOME_PROPRIO_TIROCINANTE", "Solo il nome del tirocinante");
        g.add("COGNOME_TIROCINANTE", "Solo il cognome del tirocinante");
        g.add("SESSO_M", "Casella del sesso maschile: ☒ se il tirocinante è M, altrimenti ☐");
        g.add("SESSO_F", "Casella del sesso femminile: ☒ se la tirocinante è F, altrimenti ☐");
        g.add("CODICE_FISCALE_TIROCINANTE", "Codice fiscale del tirocinante");
        g.add("DATA_NASCITA_TIROCINANTE", "Data di nascita del tirocinante");
        g.add("LUOGO_NASCITA_TIROCINANTE", "Luogo di nascita del tirocinante");
        g.add("CITTADINANZA_TIROCINANTE", "Cittadinanza del tirocinante");
        g.add("TITOLO_STUDIO_TIROCINANTE", "Titolo di studio del tirocinante");
        g.add("TELEFONO_TIROCINANTE", "Telefono del tirocinante");
        g.add("EMAIL_TIROCINANTE", "Mail del tirocinante");
        g.add("RESIDENZA_TIROCINANTE",
                "Residenza del tirocinante in una riga, es. «Via Gramsci 12, Sesto Fiorentino (FI)»");
        g.add("COMUNE_RESIDENZA_TIROCINANTE", "Comune di residenza del tirocinante");
        g.add("PROVINCIA_RESIDENZA_TIROCINANTE", "Sigla della provincia di residenza del tirocinante");
        g.add("VIA_RESIDENZA_TIROCINANTE", "Via della residenza del tirocinante");
        g.add("CIVICO_RESIDENZA_TIROCINANTE", "Numero civico della residenza del tirocinante");

        g = new Group(all, "Tirocinio e orario");
        g.add("MANSIONE", "Mansione della segnalazione");
        g.add("SEDE_TIROCINIO", "Sede del tirocinio in una riga, es. «Via Roma 1, Firenze (FI)»");
        g.add("COMUNE_SEDE_TIROCINIO", "Comune della sede del tirocinio");
        g.add("PROVINCIA_SEDE_TIROCINIO", "Sigla della provincia della sede del tirocinio");
        g.add("NOME_PROVINCIA_SEDE_TIROCINIO", "Nome della provincia della sede del tirocinio, es. Firenze");
        g.add("DATA_INIZIO", "Inizio del tirocinio");
        g.add("DATA_FINE", "Fine prevista del tirocinio, con le proroghe");
        g.add("MESI_TIROCINIO",
                "Mesi del tirocinio fino alla fine effettiva o prevista, arrotondati al mese più vicino");
        g.add("SOSPENSIONI", "Periodi di sospensione, es. «dal 10/08/2027 al 23/08/2027»");
        g.add("ORE_SETTIMANALI", "Ore settimanali");
        g.add("MONTE_ORE_TOTALE", "Monte ore di tutte le convenzioni");
        g.add("GRIGLIA_ORARIA", "Orario giorno per giorno, una riga per giorno");
        g.add("MATTINA_DALLE", "Inizio della fascia del mattino, se c'è almeno un giorno al mattino");
        g.add("MATTINA_ALLE", "Fine della fascia del mattino");
        g.add("POMERIGGIO_DALLE", "Inizio della fascia del pomeriggio, se c'è almeno un giorno al pomeriggio");
        g.add("POMERIGGIO_ALLE", "Fine della fascia del pomeriggio");
        for (int d = 0; d < DAYS.size(); d++) {
            g.add("GIORNO_" + DAYS.get(d),
                    "Casella del " + DAY_NAMES[d] + ": ☒ se è un giorno di tirocinio, altrimenti ☐");
        }
        for (int d = 0; d < DAYS.size(); d++) {
            g.add(DAYS.get(d) + "_MATTINA",
                    "Orario del mattino del " + DAY_NAMES[d] + ", es. «09:00–13:00»; vuoto se non c'è");
            g.add(DAYS.get(d) + "_POMERIGGIO", "Orario del pomeriggio del " + DAY_NAMES[d] + "; vuoto se non c'è");
        }

        g = new Group(all, "Convenzione");
        g.add("NUMERO_CONVENZIONE", "Numero della convenzione: quella iniziale o la proroga del documento");
        g.add("DATA_CONVENZIONE", "Data della convenzione");
        g.add("DATA_STIPULA_CONVENZIONE", "Data di stipula della convenzione");
        g.add("INIZIO_CONVENZIONE", "Inizio della convenzione");
        g.add("FINE_CONVENZIONE", "Fine della convenzione");

        g = new Group(all, "Progetto formativo");
        g.add("NUMERO_PROGETTO_FORMATIVO", "Numero del progetto formativo nella sua convenzione");
        g.add("INIZIO_PROGETTO_FORMATIVO", "Inizio del progetto formativo");
        g.add("FINE_PROGETTO_FORMATIVO", "Fine del progetto formativo");
        g.add("MESI_PROGETTO_FORMATIVO", "Mesi del progetto formativo, arrotondati al mese più vicino");
        g.add("ORE_PROGETTO_FORMATIVO", "Monte ore del progetto formativo");
        g.add("MANSIONE_PROGETTO_FORMATIVO", "Mansione del progetto formativo");
        g.add("DETTAGLIO_MANSIONE_PROGETTO_FORMATIVO", "Dettaglio della mansione del progetto formativo");

        g = new Group(all, "Chiusura");
        g.add("DATA_FINE_EFFETTIVA", "Fine effettiva del tirocinio, dal report di restituzione");
        g.add("DATA_CHIUSURA", "Data dell'invio del report di restituzione");
        g.add("MOTIVO_CHIUSURA", "Motivo di chiusura, es. «Chiusura regolare»");
        g.add("DESCRIZIONE_INTERRUZIONE", "Descrizione dell'interruzione anticipata; vuota con la chiusura regolare");
        return List.copyOf(all);
    }

    /** Aggiunge le voci di un gruppo, nell'ordine. */
    private record Group(List<Placeholder> all, String name) {
        void add(String placeholder, String description) {
            all.add(new Placeholder(placeholder, name, description));
        }
    }
}
