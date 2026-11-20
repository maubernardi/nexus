## ADDED Requirements

### Requirement: Tipo di tirocinio della segnalazione
Il Tutor SHALL indicare il tipo di tirocinio quando crea una segnalazione, normale o dalla bacheca. Il tipo è
obbligatorio e si sceglie tra i tipi attivi ammessi dal progetto della segnalazione. «Di inclusione» MUST NOT essere
scelto se il beneficiario non ha l'ente, il nome, la mail e il telefono del case manager.

Il Tutor SHALL poter correggere il tipo di una propria segnalazione aperta fino all'avvio del tirocinio. Il tipo nuovo
MUST NOT essere uno che sulla mansione abbinata ha già un'altra segnalazione assegnata.

Le segnalazioni aperte prima di questa funzione prendono il tipo dalla scheda del tirocinio, se c'è. Altrimenti il tipo
è «non indicato» finché il Tutor non lo indica. Il Call Center SHALL vedere il tipo, o «tipo non indicato», nel
dettaglio della segnalazione.

#### Scenario: Segnalazione con il tipo
- **WHEN** il Tutor crea una segnalazione con il tipo «Extracurriculare», ammesso dal progetto
- **THEN** la segnalazione ha quel tipo e il Call Center lo vede nel dettaglio

#### Scenario: Tipo mancante o non ammesso
- **WHEN** il Tutor crea una segnalazione senza tipo, o con un tipo che il progetto non ammette
- **THEN** la risposta è `400` con l'errore sul campo `internshipTypeId` e nessuna segnalazione viene creata

#### Scenario: Inclusione senza case manager
- **WHEN** il Tutor sceglie «Di inclusione» per un beneficiario senza il case manager
- **THEN** la risposta è `400` sul campo `internshipTypeId`, con i dati che mancano

#### Scenario: Candidatura dalla bacheca
- **WHEN** il Tutor candida un beneficiario a un annuncio indicando il tipo
- **THEN** la segnalazione speciale ha quel tipo

#### Scenario: Correzione del tipo
- **WHEN** il Tutor cambia il tipo di una segnalazione abbinata a una mansione che non ha altre segnalazioni del tipo
  nuovo
- **THEN** il tipo cambia, anche nella scheda, e l'audit registra il tipo prima e dopo

#### Scenario: Correzione verso un tipo già assegnato
- **WHEN** il Tutor sceglie un tipo che sulla mansione abbinata ha già un'altra segnalazione
- **THEN** la risposta è `400` sul campo `internshipTypeId` con il numero di quella segnalazione

#### Scenario: Correzione dopo l'avvio
- **WHEN** il Tutor cambia il tipo di un tirocinio avviato
- **THEN** la risposta è `409` e il tipo resta com'è
