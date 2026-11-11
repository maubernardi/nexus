## ADDED Requirements

### Requirement: Sede legale con il comune
La sede legale dell'azienda SHALL avere l'indirizzo (via e numero civico) e il comune scelto dall'elenco ufficiale;
entrambi restano facoltativi. Il dettaglio dell'abbinamento SHALL mostrare la sede legale con comune e sigla della
provincia. L'audit SHALL registrare indirizzo e comune cambiati con i valori prima e dopo.

#### Scenario: Sede legale completa
- **WHEN** l'operatore indica «Via Roma 1» e sceglie «Firenze (FI)» e salva
- **THEN** l'azienda mostra la sede legale «Via Roma 1, Firenze (FI)» e l'audit registra indirizzo e comune

#### Scenario: Indirizzo già inserito come testo libero
- **WHEN** l'operatore apre un'azienda registrata prima di questa funzione
- **THEN** l'indirizzo contiene il testo di prima e il comune è vuoto, da scegliere
