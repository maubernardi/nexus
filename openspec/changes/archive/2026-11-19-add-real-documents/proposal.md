## Why

Storia **US-1011** (Sprint 14): il Tutor deve scaricare convenzione, progetto formativo e relazione finale compilati
sui modelli reali della cooperativa, per usarli senza ritoccarli (Q5, planning del 09/11). Oggi NEXUS sa compilare
solo i segnaposto dei modelli generici: mancano i dati del promotore, dell'ente e del case manager, i numeri e le date
di ogni convenzione e di ogni progetto formativo, l'orario per giorno e la relazione finale. Senza questi i modelli di
GOL (EN-8) non si possono caricare.

## What Changes

- **Un documento per ogni convenzione e per ogni progetto formativo.**
  - Convenzione: una per la convenzione iniziale e una per ogni proroga (US-1012), con lo stesso modello (PO, 15/11).
  - Progetto formativo: uno per ogni progetto formativo di ogni convenzione. Senza progetti resta il documento unico di
    oggi.
- **Relazione finale** scaricabile, attiva solo a tirocinio chiuso:
  - «Al» = fine effettiva;
  - luogo = sede di competenza;
  - data = data di chiusura;
  - motivo di chiusura e descrizione dell'interruzione (US-1103).
- **Dati del soggetto promotore** nel `.env` del server, non nei modelli (PO, 09/11):
  - denominazione, sede e codice fiscale;
  - legale rappresentante con ruolo, luogo e data di nascita;
  - telefono, mail e PEC;
  - posizione INAIL;
  - polizza RC con compagnia e scadenza.
- **Dati dei documenti per progetto**, modificabili dall'ADMIN nei cataloghi:
  - riferimento all'avviso, con un testo predefinito (Avviso pubblico n. 10 di GOL);
  - riga di intestazione con codice e CUP (es. «Firenze in Rete 2 – Codice 9014358 – CUP B81D24000440006»).
- **Nuovi segnaposto**:
  - ente di presa in carico e case manager;
  - tirocinante: cognome e nome separati, sesso a caselle (☒/☐; senza M o F restano vuote), cittadinanza, titolo di
    studio, telefono e mail, comune, provincia, via e numero civico della residenza;
  - comune e provincia della sede legale e della sede del tirocinio;
  - telefono e mail dell'azienda;
  - numero e date della convenzione e del progetto formativo, con mesi, ore e mansione del progetto;
  - orario: giorni a caselle e fasce del mattino e del pomeriggio, per giorno e in generale;
  - sede di competenza, fine effettiva, mesi del tirocinio (arrotondati al mese più vicino: da 15 giorni per eccesso,
    sospensioni comprese), data e motivo di chiusura.

  I segnaposto di oggi non cambiano significato. Restano da compilare a mano: CCNL, numero di dipendenti, ADA/ISTAT,
  obiettivi, attività e permesso di soggiorno.
- **Segnaposto descritti**: la pagina «Modelli dei documenti» li elenca per gruppo, ognuno con il suo significato.
  **BREAKING** (solo interno): `GET /document-templates/placeholders` restituisce oggetti con nome, gruppo e
  descrizione invece dei soli nomi.
- **Migrazione V43** solo additiva: due colonne facoltative su `project`, con vincoli con nome testati.

## Non-goals

- **Preparazione dei modelli reali** (EN-8): segnaposto nei 6 file di GOL, caricamento da parte dell'ADMIN. I modelli
  restano fuori dal repository; nei test si usano modelli finti.
- **Dati da compilare a mano**: CCNL, dipendenti, ADA/ISTAT, obiettivi, attività, permesso di soggiorno (PO, 09/11).
- **Più promotori**: i dati del promotore sono unici per l'installazione.
- **Cronologia dei documenti scaricati**: resta la riga nella cronologia della segnalazione, come oggi.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `internship-documents`: documenti per convenzione e per progetto formativo, relazione finale, dati del promotore,
  nuovi segnaposto.
- `document-templates`: elenco dei segnaposto con gruppo e descrizione.
- `reference-data`: dati dei documenti per progetto (avviso, intestazione).

## Impact

- Migrazione **V43**. Sul server: i dati del promotore nel `.env` (`NEXUS_PROMOTER_*`), poi il deploy; senza, i
  documenti li lasciano come righe da completare.
- API:
  - `GET /tickets/{ticketId}/internship/documents/{kind}` accetta `extension` e `project`;
  - la relazione finale si scarica a tirocinio chiuso, altrimenti `409`;
  - `GET /document-catalogs/project-documents` e `PUT /document-catalogs/projects/{id}/documents`;
  - `GET /document-templates/placeholders` con nome, gruppo e descrizione.
- Frontend:
  - sezione «Documenti» della scheda con un pulsante per ogni convenzione e progetto formativo, più la relazione
    finale;
  - dati dei documenti dei progetti nei cataloghi dell'ADMIN;
  - segnaposto descritti nella pagina dei modelli.

  Accessibilità: WCAG 2.2 AA (1.3.1, 2.4.6, 3.3.2, 4.1.2); test axe. PWA: nessun impatto (i documenti non vanno in
  cache).
- Deploy: `infra/deploy/compose.yaml` passa `NEXUS_PROMOTER_*` al backend; il README spiega come impostarle.
- Collaudo: nuove prove T-24.3 e T-24.4.
