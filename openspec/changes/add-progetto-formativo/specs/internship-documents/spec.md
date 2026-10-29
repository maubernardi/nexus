## ADDED Requirements

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
