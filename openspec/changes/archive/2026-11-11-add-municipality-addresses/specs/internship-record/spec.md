## ADDED Requirements

### Requirement: Sede e residenza con il comune
Nella scheda del tirocinio la sede SHALL avere l'indirizzo e il comune scelto dall'elenco ufficiale; la residenza del
beneficiario SHALL avere comune, via e numero civico. Per l'avvio del tirocinio la sede MUST avere indirizzo e comune e
la residenza MUST avere comune, via e numero civico: se manca una parte, la scheda indica la sede o la residenza tra i
dati mancanti. L'audit SHALL registrare la sede con i valori e la residenza solo come cambiata. L'anonimizzazione del
beneficiario MUST cancellare tutta la residenza.

#### Scenario: Residenza completa
- **WHEN** il Tutor sceglie «Sesto Fiorentino (FI)», scrive «Via Gramsci» e «12» e salva
- **THEN** la scheda mostra la residenza e non la elenca tra i dati mancanti

#### Scenario: Sede senza comune
- **WHEN** la scheda ha l'indirizzo della sede ma non il comune
- **THEN** la sede è tra i dati che mancano per l'avvio

#### Scenario: Residenza già inserita come testo libero
- **WHEN** il Tutor apre una scheda compilata prima di questa funzione
- **THEN** il testo della residenza è nella via, comune e numero civico sono vuoti e la residenza è tra i dati mancanti

#### Scenario: Anonimizzazione
- **WHEN** il beneficiario viene anonimizzato
- **THEN** comune, via e numero civico della residenza nella scheda sono cancellati
