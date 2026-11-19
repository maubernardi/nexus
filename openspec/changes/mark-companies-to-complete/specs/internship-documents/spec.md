## ADDED Requirements

### Requirement: Dati dell'azienda da completare nei documenti
Nei documenti, la P.IVA segnaposto «00000000000» e il nome segnaposto «Azienda senza nome n. ‹numero›» di un'azienda
arrivata dal database attuale SHALL diventare la riga da completare, come ogni dato mancante. I valori segnaposto MUST
NOT comparire nei documenti.

#### Scenario: Azienda senza partita IVA
- **WHEN** il Tutor scarica la Convenzione di un tirocinio in un'azienda con la P.IVA «00000000000»
- **THEN** al posto della partita IVA c'è la riga da completare

#### Scenario: Azienda senza nome
- **WHEN** l'azienda del tirocinio si chiama «Azienda senza nome n. 1234»
- **THEN** al posto della ragione sociale c'è la riga da completare
