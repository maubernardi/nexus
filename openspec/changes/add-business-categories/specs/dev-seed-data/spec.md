## ADDED Requirements

### Requirement: Categoria delle aziende dimostrative
Le aziende dimostrative SHALL avere una categoria merceologica e una descrizione dell'attività. Le aziende dimostrative
già modificate a mano MUST NOT cambiare.

#### Scenario: Elenco con le categorie
- **WHEN** il backend locale si avvia con i dati dimostrativi
- **THEN** l'elenco delle aziende mostra una categoria per ogni azienda dimostrativa

#### Scenario: Categoria già scelta a mano
- **WHEN** un'azienda dimostrativa ha già una categoria o un'attività scelta a mano
- **THEN** il riavvio non la cambia
