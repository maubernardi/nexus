## ADDED Requirements

### Requirement: Data di fine effettiva nel report di restituzione
Il report di restituzione SHALL contenere la data di fine effettiva del tirocinio, obbligatoria. Il modulo SHALL
proporla uguale alla data di fine prevista della scheda del tirocinio, modificabile. La data MUST NOT precedere la data
di inizio della scheda; altrimenti l'invio è rifiutato con l'errore sul campo e la segnalazione resta in tirocinio.

La data SHALL salvarsi con il report, senza modifiche dopo l'invio, e SHALL comparire nel report inviato e nell'audit
della chiusura con il valore. La data vale solo per la relazione finale: la scheda del tirocinio MUST restare
com'era. I report inviati prima di questa funzione restano senza la data.

#### Scenario: Fine come prevista
- **WHEN** il Tutor apre il report di un tirocinio con fine prevista il 31/12/2026 e lo invia senza cambiare la data
- **THEN** il report inviato riporta la fine effettiva al 31/12/2026

#### Scenario: Fine anticipata
- **WHEN** il Tutor indica come fine effettiva il 15/11/2026 per un tirocinio con fine prevista il 31/12/2026
- **THEN** il report riporta il 15/11/2026, l'audit della chiusura registra la data e la scheda ha ancora la fine
  prevista al 31/12/2026

#### Scenario: Fine prima dell'inizio
- **WHEN** il Tutor indica una fine effettiva precedente alla data di inizio del tirocinio
- **THEN** l'invio è rifiutato con l'errore sul campo e la segnalazione resta in tirocinio

#### Scenario: Data mancante
- **WHEN** il Tutor invia il report senza la data di fine effettiva
- **THEN** l'invio è rifiutato con l'errore sul campo

#### Scenario: Report inviato prima
- **WHEN** si apre un report inviato prima di questa funzione
- **THEN** il report si mostra senza la data di fine effettiva
