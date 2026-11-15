## ADDED Requirements

### Requirement: Motivo di chiusura nel report di restituzione
Il report di restituzione SHALL avere il motivo di chiusura del tirocinio:
- se la fine effettiva precede la fine prevista della scheda del tirocinio, il Tutor MUST scegliere il motivo tra le
  voci attive di interruzione e MUST scrivere una descrizione dell'interruzione (testo, al massimo 2000 caratteri, PO
  16/11). Senza motivo o senza descrizione, o con una voce disattivata o con la chiusura regolare, l'invio è rifiutato
  con l'errore sul campo;
- altrimenti il motivo MUST essere «Chiusura regolare», assegnato da NEXUS anche se il modulo ne indica un altro, e
  la descrizione non si salva.

Il motivo SHALL comparire nel report inviato e nella cronologia della chiusura, e SHALL essere registrato nell'audit. La
descrizione SHALL comparire nel report inviato; è testo libero, quindi MUST NOT finire nell'audit né nei log.
I report inviati prima di questa funzione restano senza motivo.

#### Scenario: Interruzione anticipata
- **WHEN** il Tutor indica come fine effettiva il 15/02/2027 per un tirocinio con fine prevista il 31/05/2027 e sceglie
  «Interruzione anticipata per volontà dell'azienda»
- **THEN** il report inviato e la cronologia riportano quel motivo

#### Scenario: Interruzione senza motivo
- **WHEN** la fine effettiva precede quella prevista e il Tutor non sceglie il motivo
- **THEN** l'invio è rifiutato con l'errore sul motivo e la segnalazione resta in tirocinio

#### Scenario: Interruzione senza descrizione
- **WHEN** il Tutor sceglie il motivo dell'interruzione ma non scrive la descrizione
- **THEN** l'invio è rifiutato con l'errore sulla descrizione

#### Scenario: Chiusura regolare
- **WHEN** la fine effettiva coincide con quella prevista
- **THEN** il motivo del report è «Chiusura regolare», senza che il Tutor lo scelga
