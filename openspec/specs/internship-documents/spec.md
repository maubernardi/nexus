# internship-documents Specification

## Purpose
Documenti Word del tirocinio compilati dalla scheda (Convenzione e Progetto Formativo), del solo Tutor della
segnalazione: il modello caricato dall'ADMIN che vale per la segnalazione (US-1010) oppure quello generico del tipo di
tirocinio, con i dati mancanti lasciati come righe da completare a mano. Ogni download è registrato nella cronologia,
con il modello usato.
## Requirements
### Requirement: Convenzione in Word
Il Tutor della segnalazione SHALL poter scaricare la Convenzione in formato `.docx`, compilata con i dati della scheda
del tirocinio, dell'azienda e della segnalazione. Il modello SHALL essere quello del tipo di tirocinio, oppure il
modello predefinito per i tipi senza modello. Ogni segnaposto noto MUST essere sostituito; un dato mancante resta una
riga vuota da completare. Il documento MUST aprirsi senza errori. Gli altri utenti MUST NOT poter scaricarlo.

#### Scenario: Scheda completa
- **WHEN** il Tutor scarica la Convenzione di un tirocinio curriculare con la scheda completa
- **THEN** riceve il modello del tipo Curriculare con nome del tirocinante, azienda, legale rappresentante, date,
  griglia oraria e monte ore al posto dei segnaposto

#### Scenario: Tipo senza modello proprio
- **WHEN** il tipo di tirocinio è stato aggiunto dall'ADMIN e non ha un modello
- **THEN** la Convenzione usa il modello predefinito, con il nome del tipo

#### Scenario: Scheda incompleta
- **WHEN** nella scheda manca il codice fiscale del beneficiario
- **THEN** la Convenzione si scarica con una riga vuota al suo posto e la pagina avvisa dei dati mancanti

#### Scenario: Traccia del download
- **WHEN** il Tutor scarica la Convenzione
- **THEN** la cronologia della segnalazione registra il download, senza dati personali

### Requirement: Progetto Formativo in Word
Il Tutor della segnalazione SHALL poter scaricare il Progetto Formativo in formato `.docx`, con le stesse regole della
Convenzione:
- dati della scheda, dell'azienda e della segnalazione;
- modello del tipo di tirocinio, oppure il modello predefinito;
- righe vuote per i dati mancanti;
- download registrato nella cronologia.

Gli altri utenti MUST NOT poterlo scaricare.

#### Scenario: Progetto Formativo compilato
- **WHEN** il Tutor scarica il Progetto Formativo di un tirocinio di inserimento socio-terapeutico
- **THEN** riceve il modello di quel tipo, con tirocinante, tutor aziendali, griglia oraria e monte ore compilati, e
  la cronologia registra "Progetto Formativo"

### Requirement: Indirizzi composti nei documenti
Nella Convenzione e nel Progetto Formativo la sede legale dell'azienda, la sede del tirocinio e la residenza del
tirocinante SHALL comparire come un'unica riga con indirizzo, comune e sigla della provincia, nella forma
«Via Roma 1, Firenze (FI)». Una parte mancante MUST essere omessa senza lasciare separatori vuoti; se mancano tutte
resta la riga vuota da completare.

#### Scenario: Residenza completa
- **WHEN** la residenza è «Via Gramsci» «12» a Sesto Fiorentino
- **THEN** il documento riporta «Via Gramsci 12, Sesto Fiorentino (FI)»

#### Scenario: Comune mancante
- **WHEN** la sede del tirocinio ha l'indirizzo «Via Roma 1» ma non il comune
- **THEN** il documento riporta «Via Roma 1»

### Requirement: Ruolo nella riga dei tutor aziendali
Nella Convenzione e nel Progetto Formativo ogni tutor aziendale SHALL comparire come nome, ruolo e recapito tra
parentesi, nella forma «Mario Rossi, Responsabile di produzione (055 123456)». Un ruolo o un recapito mancante MUST
essere omesso senza lasciare separatori vuoti.

#### Scenario: Tutor completo
- **WHEN** il tutor aziendale è «Mario Rossi», ruolo «Responsabile di produzione», recapito «055 123456»
- **THEN** il documento riporta «Mario Rossi, Responsabile di produzione (055 123456)»

#### Scenario: Tutor senza ruolo
- **WHEN** il tutor aziendale ha nome e recapito ma non il ruolo
- **THEN** il documento riporta «Mario Rossi (055 123456)»

### Requirement: Scelta del modello caricato
Quando il Tutor scarica un documento, NEXUS SHALL cercare i modelli caricati dall'ADMIN per quel documento che:
- sono attivi;
- sono del progetto della segnalazione;
- sono del tipo di tirocinio della scheda;
- hanno l'ente di presa in carico del beneficiario oppure nessun ente.

Se ce n'è almeno uno con l'ente del beneficiario, MUST contare solo quelli con l'ente. Poi:
- con un solo modello, il documento MUST usare quello;
- con più modelli, il Tutor SHALL sceglierlo da un elenco con i loro nomi, e il download MUST essere rifiutato finché
  non sceglie;
- senza modelli, il documento MUST usare il modello generico che resta nel codice.

Un modello che non è tra quelli validi MUST essere rifiutato. Il download SHALL registrare nella cronologia anche il
modello usato, oppure «modello generico».

#### Scenario: Un modello con l'ente
- **WHEN** per la convenzione di GOL Inclusione ci sono un modello senza ente e uno per l'ente del beneficiario
- **THEN** il Tutor scarica la convenzione con il modello dell'ente, senza scegliere

#### Scenario: Più modelli
- **WHEN** per il progetto formativo ci sono due modelli attivi senza ente per lo stesso progetto e tipo
- **THEN** la scheda chiede quale usare e il download parte con il modello scelto

#### Scenario: Nessun modello
- **WHEN** non c'è un modello caricato per il documento
- **THEN** il documento usa il modello generico, come prima di questa funzione

