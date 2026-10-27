## ADDED Requirements

### Requirement: Segnalazioni da approvare
Il Call Center e l'ADMIN SHALL avere nella Home la vista "Da approvare", con le segnalazioni in
`IN_ATTESA_APPROVAZIONE` dalla più vecchia e la tipologia proposta indicata come nuova.

#### Scenario: Vista da approvare
- **WHEN** un operatore apre la vista "Da approvare"
- **THEN** vede le segnalazioni con una nuova tipologia proposta, con l'indicazione "(nuova, da approvare)"
