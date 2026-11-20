## Why

Storia **US-510** (Sprint 15, PO 19/11). Nel database attuale un'azienda ha fino a **due referenti**, ognuno con il
suo ruolo (Titolare, Responsabile del personale…), e più recapiti di quelli di NEXUS: cellulare, fax, seconda email e
una sede operativa ([analisi](../../../docs/analisi/database-attuale.md), G5). NEXUS ha un solo referente, senza
ruolo, e un solo telefono e una sola email. Il PO vuole che NEXUS sostituisca il database attuale in tutto: referenti e
recapiti entrano nell'azienda prima dell'importazione (US-1309).

Nel database attuale (13.799 aziende):
- il primo referente è compilato nel 36% dei casi, il secondo nel 12%;
- ci sono 1.129 ruoli senza nome (per esempio solo «Responsabile del personale»);
- il cellulare è compilato nel 24% dei casi, il fax nel 5% e la seconda email nel 10%;
- la sede operativa ha il comune nel 17% dei casi e l'indirizzo nel 3%.

## What Changes

- **Catalogo «Ruoli dei referenti»** (V47), gestito dall'ADMIN come gli altri cataloghi.
  - Parte con i **12 ruoli** dei «Tipi di contatti» del database attuale, ripuliti:
    - tolte le righe vuote, quelle che non sono ruoli («pelle», «DI», «ele») e il doppione di «Legale
      Rappresentante»;
    - maiuscole uniformi («Capo area», «Legale rappresentante»).
  - L'id di ogni voce iniziale è fisso: 893700000000000500 più il numero del ruolo in Access, come per le categorie
    merceologiche (US-509).
- **Referenti dell'azienda**: da zero a 10, in ordine.
  - Ognuno ha nome, ruolo, telefono, cellulare ed email, tutti facoltativi, ma serve almeno il nome o il ruolo: nel
    database attuale ci sono referenti con il solo ruolo.
  - Il referente di oggi diventa il primo, senza ruolo.
  - Un ruolo disattivato resta sui referenti che l'hanno.
- **Recapiti dell'azienda**: oltre a telefono ed email, **cellulare, fax e seconda email**.
- **Sede operativa**: indirizzo e comune scelto dall'elenco ufficiale, come la sede legale (US-1007), facoltativi.
- **Audit**:
  - referenti e recapiti sono dati personali e entrano nell'audit dell'azienda solo come «cambiati», senza valori;
  - la sede operativa entra con i valori prima e dopo, come la sede legale.
- **Elenco delle aziende**: la colonna «Referente» mostra il primo referente, con il numero degli altri.
- **Dettaglio dell'abbinamento**: tutti i referenti con ruolo e recapiti, i recapiti nuovi e la sede operativa.
- **API**:
  - `CompanyDTO` con `contacts`, `mobile`, `fax`, `secondaryEmail`, `operatingAddress` e `operatingMunicipality`, senza
    `contactPerson`;
  - il modulo cambia allo stesso modo, con `contacts` (elenco), `operatingMunicipalityId` e senza `contactPerson`;
  - `GET /reference/contact-roles` (Call Center e ADMIN);
  - `/catalogs/contact-roles` per l'ADMIN.
- **Dati dimostrativi**: referenti con il ruolo per le aziende dimostrative senza referenti, e una sede operativa.

## Non-goals

- **L'importazione** di referenti e recapiti: è US-1309, che usa gli id fissi dei ruoli.
- **Il legale rappresentante** (US-506): resta nella scheda del tirocinio, con i dati per i documenti. Un referente
  con il ruolo «Legale rappresentante» non lo compila.
- **Segnaposto dei documenti** per referenti, recapiti nuovi e sede operativa: i modelli reali non li usano. Telefono
  ed email dell'azienda restano nei documenti come oggi.
- **Ricerca per referente** nel campo di ricerca: resta per ragione sociale e partita IVA.
- **Togliere la colonna `contact_person`**: resta, non più usata, perché la release precedente possa ripartire (A16).

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `reference-data`: catalogo dei ruoli dei referenti con le voci iniziali del database attuale.
- `company-registry`: più referenti con ruolo e recapiti, recapiti nuovi e sede operativa dell'azienda.
- `dev-seed-data`: referenti e sede operativa delle aziende dimostrative.

## Impact

- Migrazione **V47**, solo additiva (A16):
  - tabella `contact_role` con i 12 ruoli;
  - tabella `company_contact`, con il referente di oggi come primo;
  - colonne facoltative `mobile`, `fax`, `secondary_email`, `operating_address` e `operating_municipality_id` su
    `company`;
  - vincoli con nome testati in `DomainConstraintsIT`.
- Frontend:
  - sezione del catalogo nella pagina «Cataloghi»;
  - modulo dell'azienda con i referenti (aggiunta e rimozione), i recapiti e la sede operativa;
  - colonna «Referente» nell'elenco;
  - dettaglio dell'abbinamento.
- Accessibilità (WCAG 2.2 AA):
  - ogni referente è un gruppo con un titolo («Referente 1») ed etichette proprie;
  - aggiungendo un referente il focus va al suo nome, togliendolo va al pulsante «Aggiungi referente»;
  - errori nel riepilogo, con il collegamento al campo;
  - test con axe.
- PWA: nessun impatto, le API restano fuori dalla cache.
