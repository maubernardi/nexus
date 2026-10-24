## ADDED Requirements

### Requirement: Collegamento alla scheda del beneficiario
Nel dettaglio della segnalazione il nome del beneficiario SHALL aprire la sua scheda, con un nome accessibile che
comincia con il testo visibile; arrivando dalla segnalazione, la scheda SHALL offrire il ritorno alla segnalazione.
La visibilità della scheda segue le regole del beneficiario, verificate dall'API.

#### Scenario: Dalla segnalazione alla scheda e ritorno
- **WHEN** un operatore apre il beneficiario dal dettaglio della segnalazione n. 24
- **THEN** vede la scheda del beneficiario e il link "Torna alla segnalazione n. 24" riporta al dettaglio
