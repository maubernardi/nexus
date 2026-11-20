## MODIFIED Requirements

### Requirement: Partita IVA valida e univoca
Il sistema SHALL accettare solo P.IVA di 11 cifre e SHALL rifiutare una P.IVA già usata da un'altra azienda con un
errore sul campo. La P.IVA segnaposto «00000000000» delle aziende arrivate dal database attuale senza partita IVA
SHALL NOT contare per l'unicità: più aziende possono averla.

#### Scenario: P.IVA duplicata
- **WHEN** si registra o modifica un'azienda con la P.IVA di un'altra
- **THEN** la risposta è `400` con errore sul campo `vatCode`

#### Scenario: Più aziende senza partita IVA
- **WHEN** il database contiene già un'azienda con la P.IVA «00000000000» e ne arriva un'altra con la stessa P.IVA
- **THEN** il database le accetta entrambe, mentre una P.IVA vera ripetuta resta rifiutata dal vincolo
  `uq_company_vat_code`

## ADDED Requirements

### Requirement: Dati da completare
Un'azienda arrivata dal database attuale SHALL avere al posto dei dati che mancano i valori segnaposto:
- la P.IVA «00000000000»;
- il nome «Azienda senza nome n. ‹numero›».

Il sistema SHALL indicare per ogni azienda quali di questi dati sono da completare.
- **Nell'elenco delle aziende** SHALL comparire «Da completare» nella colonna del dato che manca, e l'elenco SHALL
  potersi filtrare sulle sole aziende da completare.
- **Nella scheda e nel dettaglio dell'abbinamento** SHALL comparire quali dati mancano, MUST NOT comparire la P.IVA
  segnaposto.

Registrazione e modifica dell'azienda MUST rifiutare con `400` sul campo:
- la P.IVA «00000000000»;
- un nome che comincia con «Azienda senza nome n.», senza distinguere maiuscole e minuscole.

Nella scheda i campi con il segnaposto SHALL aprirsi vuoti, così il salvataggio chiede i dati veri. Le altre sezioni
della scheda (legale rappresentante, codici ATECO, mansioni, note) SHALL restare modificabili.

#### Scenario: Azienda senza partita IVA nell'elenco
- **WHEN** l'operatore cerca un'azienda che ha la P.IVA «00000000000»
- **THEN** nella colonna della partita IVA compare «Da completare» al posto degli zeri

#### Scenario: Filtro sulle aziende da completare
- **WHEN** l'operatore sceglie «Solo le aziende da completare»
- **THEN** l'elenco mostra solo le aziende senza partita IVA o senza nome, e il filtro resta nell'indirizzo della
  pagina

#### Scenario: Scheda da completare
- **WHEN** l'operatore apre la scheda dell'azienda «Azienda senza nome n. 1234», che non ha la partita IVA
- **THEN** la pagina dice che mancano ragione sociale e partita IVA, e nel modulo i due campi sono vuoti e obbligatori

#### Scenario: Segnaposto rifiutato
- **WHEN** si registra o modifica un'azienda con la P.IVA «00000000000» o con il nome «azienda senza nome n. 12»
- **THEN** la risposta è `400` con l'errore sul campo `vatCode` o `name`, che chiede il dato vero, e nulla è salvato

#### Scenario: Completamento
- **WHEN** l'operatore inserisce ragione sociale e partita IVA vere e salva
- **THEN** l'azienda non è più da completare e l'audit registra i valori prima e dopo
