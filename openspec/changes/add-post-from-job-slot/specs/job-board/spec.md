## ADDED Requirements

### Requirement: Annuncio da una mansione libera
Il Call Center e l'ADMIN SHALL poter creare dalla scheda dell'azienda una bozza di annuncio per una mansione libera e a
disposizione di un'azienda attiva. La bozza nasce con il titolo della mansione e visibile a tutti i progetti. Una
mansione MUST NOT avere più di un annuncio attivo (bozza o pubblicato). La creazione SHALL essere registrata
nell'audit.

#### Scenario: Creazione
- **WHEN** l'operatore sceglie "Crea annuncio" accanto a una mansione libera
- **THEN** nasce la bozza #N e si apre la sua scheda con l'esito; accanto alla mansione compare "Annuncio #N"

#### Scenario: Annuncio già attivo
- **WHEN** la mansione ha già una bozza o un annuncio pubblicato
- **THEN** la creazione è rifiutata con il numero dell'annuncio esistente

#### Scenario: Mansione non proponibile
- **WHEN** la mansione è bloccata da una segnalazione o ritirata
- **THEN** "Crea annuncio" non compare e l'API rifiuta la creazione con il motivo
