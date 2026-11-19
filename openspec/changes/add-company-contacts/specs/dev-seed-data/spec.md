## ADDED Requirements

### Requirement: Referenti delle aziende dimostrative
Le aziende dimostrative senza referenti SHALL ricevere referenti con il ruolo, e una di loro SHALL avere anche la sede
operativa quando l'elenco dei comuni è caricato. Le aziende dimostrative che hanno già referenti o una sede operativa
MUST NOT cambiare.

#### Scenario: Demo nuova
- **WHEN** il backend locale si avvia su un database vuoto con i dati dimostrativi
- **THEN** ogni azienda dimostrativa ha almeno un referente con il ruolo, e una ne ha due

#### Scenario: Referenti già presenti
- **WHEN** un'azienda dimostrativa ha già dei referenti
- **THEN** l'avvio non li cambia
