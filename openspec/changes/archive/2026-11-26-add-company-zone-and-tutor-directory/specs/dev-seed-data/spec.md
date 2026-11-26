## ADDED Requirements

### Requirement: Zona delle aziende dimostrative
Alcune aziende dimostrative SHALL avere una zona del catalogo, e almeno una SHALL restare senza, per provare nel
collaudo la colonna, il filtro e l'elenco del Tutor. Le aziende dimostrative con una zona scelta a mano MUST NOT
cambiare.

#### Scenario: Collaudo nuovo
- **WHEN** il collaudo parte dai dati dimostrativi
- **THEN** nell'elenco delle aziende alcune hanno una zona e almeno una no

#### Scenario: Zona già scelta a mano
- **WHEN** un'azienda dimostrativa ha già una zona scelta a mano
- **THEN** il riavvio non la cambia
