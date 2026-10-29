## ADDED Requirements

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
