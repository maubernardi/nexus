## ADDED Requirements

### Requirement: Tipi di tirocinio
L'ADMIN SHALL gestire il catalogo dei tipi di tirocinio con le stesse regole di zone e tipologie: aggiunta, nuovo
nome, disattivazione e riattivazione, audit, nessuna cancellazione. Le voci iniziali SHALL essere Curriculare,
Extracurriculare, Di inclusione e Di inserimento socio-terapeutico. I tipi attivi SHALL essere disponibili per la scheda
del tirocinio.

#### Scenario: Voci iniziali
- **WHEN** l'ADMIN apre la pagina "Cataloghi"
- **THEN** la sezione "Tipi di tirocinio" mostra le quattro voci iniziali, attive

#### Scenario: Tipo disattivato
- **WHEN** l'ADMIN disattiva un tipo
- **THEN** il tipo non è più tra quelli che si possono scegliere nella scheda del tirocinio
