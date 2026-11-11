## ADDED Requirements

### Requirement: Comune delle aziende dimostrative
Quando l'elenco dei comuni è caricato, le aziende dimostrative SHALL avere la sede legale con indirizzo e comune, senza
toccare le aziende modificate a mano.

#### Scenario: Demo con l'elenco dei comuni
- **WHEN** il backend locale si avvia con l'elenco dei comuni caricato
- **THEN** le aziende dimostrative hanno la sede legale con un comune dell'elenco

#### Scenario: Azienda già corretta a mano
- **WHEN** un'azienda dimostrativa ha già un comune scelto a mano
- **THEN** l'avvio non lo cambia
