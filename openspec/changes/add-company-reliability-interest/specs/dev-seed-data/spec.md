## ADDED Requirements

### Requirement: Affidabilità e interesse delle aziende dimostrative
Alcune aziende dimostrative SHALL avere affidabilità e interesse al progetto, come le aziende convertite dal database
attuale: almeno una con stelle e pollici, una con le sole stelle e una senza valutazioni. Servono a provare nel
collaudo la scheda, le colonne e i filtri. Le aziende dimostrative già valutate a mano MUST NOT cambiare.

#### Scenario: Collaudo nuovo
- **WHEN** il collaudo parte dai dati dimostrativi
- **THEN** nell'elenco delle aziende ce n'è almeno una con stelle e pollici, una con le sole stelle e una senza
  valutazioni

#### Scenario: Valutazione già scelta a mano
- **WHEN** un'azienda dimostrativa ha già affidabilità o interesse scelti a mano
- **THEN** il riavvio non li cambia
