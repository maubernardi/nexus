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

### Requirement: Un documento per ogni convenzione e per ogni progetto formativo
Il Tutor della segnalazione SHALL poter scaricare:
- la Convenzione iniziale e una Convenzione per ogni proroga, tutte con lo stesso modello;
- un Progetto Formativo per ogni progetto formativo di ogni convenzione.

Ogni documento MUST riportare il numero, le date e la data di stipula della sua convenzione. Il Progetto Formativo MUST
riportare anche numero, date, mesi, ore e mansione del suo progetto. Senza progetti formativi il Progetto Formativo
SHALL restare un documento unico, con i dati del progetto da completare. Una proroga o un progetto che non esistono
MUST dare `404`. Il nome del file e la cronologia SHALL indicare la proroga e il progetto.

#### Scenario: Convenzione della proroga
- **WHEN** il Tutor scarica la convenzione della proroga 1, numero «2027/200» dal 15/06/2027 al 30/09/2027
- **THEN** il documento usa il modello della convenzione, con «2027/200» e le date della proroga; il file si chiama
  `Convenzione-proroga-1-segnalazione-12.docx`

#### Scenario: Progetto formativo di un progetto
- **WHEN** il Tutor scarica il progetto formativo 2 della convenzione iniziale, dal 01/03/2027 al 31/05/2027 come
  «Cuoco»
- **THEN** il documento riporta il progetto 2 con le sue date, i suoi mesi, le sue ore e la mansione «Cuoco», con il
  numero e la data della convenzione iniziale

#### Scenario: Progetto inesistente
- **WHEN** il Tutor chiede il progetto formativo 3 di una convenzione che ne ha due
- **THEN** la risposta è `404`

### Requirement: Relazione finale a tirocinio chiuso
Il Tutor della segnalazione SHALL poter scaricare la Relazione finale solo a tirocinio chiuso, cioè con il report di
restituzione inviato. Prima la richiesta MUST essere rifiutata con `409` e la scheda SHALL dire che si scarica a
tirocinio chiuso. La Relazione finale SHALL riportare:
- la fine effettiva del tirocinio;
- la sede di competenza come luogo;
- la data di chiusura;
- il motivo di chiusura e, per l'interruzione anticipata, la sua descrizione.

Il modello si sceglie come per gli altri documenti; senza modelli caricati vale il modello generico nel codice.

#### Scenario: Tirocinio chiuso
- **WHEN** il report di restituzione è stato inviato con la fine effettiva 31/05/2027 e la chiusura regolare
- **THEN** il Tutor scarica la Relazione finale con «31/05/2027», la sede di competenza, la data dell'invio e
  «Chiusura regolare»

#### Scenario: Tirocinio in corso
- **WHEN** il Tutor chiede la Relazione finale di un tirocinio non ancora chiuso
- **THEN** la risposta è `409` e la scheda non offre il pulsante

### Requirement: Dati del soggetto promotore
I documenti SHALL riportare i dati del soggetto promotore del progetto della segnalazione, indicati dall'ADMIN nei
cataloghi, non dai modelli né dalla configurazione del server:
- denominazione, sede e codice fiscale;
- legale rappresentante con ruolo, luogo e data di nascita;
- telefono, mail e PEC;
- posizione INAIL;
- polizza RC con compagnia, numero e scadenza.

I dati MUST NOT stare nel repository. Un dato non indicato SHALL restare una riga da completare.

#### Scenario: Dati configurati
- **WHEN** per il progetto della segnalazione è indicata la polizza RC con la scadenza 22/07/2027
- **THEN** i documenti riportano polizza e scadenza «22/07/2027»

#### Scenario: Dati non configurati
- **WHEN** per il progetto della segnalazione manca la PEC del promotore
- **THEN** i documenti hanno la riga da completare al suo posto

### Requirement: Dati dei documenti reali
I documenti SHALL poter riportare, oltre ai dati di oggi:
- intestazione del progetto (codice e CUP) e riferimento all'avviso;
- sede di competenza della segnalazione;
- ente di presa in carico (denominazione, sede, codice fiscale, rappresentante con qualifica, luogo e data di nascita,
  referente con telefono e mail) e case manager (nome, servizio, telefono, mail);
- tirocinante: cognome e nome separati, sesso, cittadinanza, titolo di studio, telefono, mail, comune, provincia, via e
  numero civico della residenza;
- comune e provincia della sede legale dell'azienda e della sede del tirocinio, telefono e mail dell'azienda;
- orario: giorni della settimana a caselle, fasce del mattino e del pomeriggio, fasce di ogni giorno;
- mesi del tirocinio, arrotondati al mese più vicino.

Regole dei valori:
- il sesso SHALL comparire come caselle: `☒` su M o F, `☐` sull'altra; con un sesso diverso da M e F restano vuote
  entrambe;
- i giorni dell'orario SHALL comparire allo stesso modo;
- la fascia di un giorno SHALL essere l'orario («09:00–13:00») oppure niente, se il giorno non ha quella fascia;
- i mesi del tirocinio SHALL contare i giorni coperti da una convenzione, dall'inizio alla fine effettiva o, se il
  tirocinio non è chiuso, alla fine prevista, sospensioni comprese. Si arrotondano al mese più vicino: 15 giorni o più
  valgono un mese.

Gli altri dati mancanti MUST restare righe da completare. CCNL, dipendenti, ADA/ISTAT, obiettivi, attività e permesso
di soggiorno restano da compilare a mano.

#### Scenario: Sesso a caselle
- **WHEN** il beneficiario ha sesso F
- **THEN** il documento ha `☐` su M e `☒` su F

#### Scenario: Sesso non indicato
- **WHEN** il beneficiario ha sesso «Preferisce non dichiarare»
- **THEN** il documento ha `☐` su M e su F

#### Scenario: Orario per giorno
- **WHEN** la griglia ha il lunedì solo al mattino, dalle 09:00 alle 13:00
- **THEN** il documento ha `☒` sul lunedì, «09:00–13:00» nel mattino del lunedì e niente nel pomeriggio

#### Scenario: Mesi con la proroga
- **WHEN** il tirocinio va dal 01/12/2026 al 31/05/2027 e la proroga dal 01/06/2027 al 15/07/2027, ed è chiuso il
  15/07/2027
- **THEN** i mesi del tirocinio sono 8: 7 mesi e 15 giorni arrotondati per eccesso

### Requirement: Formattazione dei modelli conservata
La compilazione SHALL cambiare solo il testo dei segnaposto: grassetti, tabulazioni, simboli e campi del modello MUST
restare come sono, anche nelle righe con segnaposto. Un segnaposto spezzato in più parti di formattazione diversa
SHALL prendere la formattazione della parte in cui comincia; un a capo nel valore SHALL restare con quella
formattazione.

#### Scenario: Riga con parti in grassetto
- **WHEN** il modello ha la riga «{PROMOTORE} d'ora in poi denominato “**soggetto promotore**”»
- **THEN** il documento ha la denominazione del promotore e «soggetto promotore» ancora in grassetto

#### Scenario: Caselle e tabulazioni
- **WHEN** il modello ha una tabulazione, «Lunedì» in grassetto e `{GIORNO_LUNEDI}` nella stessa riga
- **THEN** il documento ha la tabulazione, «Lunedì» in grassetto e la casella al posto del segnaposto

### Requirement: Tipologia dell'ente a caselle
I documenti SHALL poter riportare la tipologia dell'ente di presa in carico a caselle, con un segnaposto per ogni
tipologia (Comune, Unione dei Comuni, Comunità montana, Società della Salute, Azienda Sanitaria, Ufficio
interdistrettuale di esecuzione penale esterna, Altro soggetto): `☒` sulla tipologia dell'ente del beneficiario, `☐`
sulle altre. Senza ente le caselle MUST essere tutte `☐`.

#### Scenario: Ente comunale
- **WHEN** il beneficiario è in carico al Comune di Firenze, di tipologia Comune
- **THEN** il documento ha `☒` su «Comune» e `☐` su «Società della Salute» e sulle altre

#### Scenario: Senza ente
- **WHEN** il tirocinio è Extracurriculare e il beneficiario non ha un ente di presa in carico
- **THEN** le caselle della tipologia sono tutte `☐`

### Requirement: Variabili del progetto nei documenti
I documenti SHALL sostituire ogni variabile del progetto della segnalazione con il suo valore.

#### Scenario: Variabile compilata
- **WHEN** GOL ha la variabile `SEDE_OPERATIVA` con valore «Via Roma 1, Firenze» e il modello contiene
  `{SEDE_OPERATIVA}`
- **THEN** il documento di una segnalazione di GOL riporta «Via Roma 1, Firenze»

### Requirement: Dati dell'azienda da completare nei documenti
Nei documenti, la P.IVA segnaposto «00000000000» e il nome segnaposto «Azienda senza nome n. ‹numero›» di un'azienda
arrivata dal database attuale SHALL diventare la riga da completare, come ogni dato mancante. I valori segnaposto MUST
NOT comparire nei documenti.

#### Scenario: Azienda senza partita IVA
- **WHEN** il Tutor scarica la Convenzione di un tirocinio in un'azienda con la P.IVA «00000000000»
- **THEN** al posto della partita IVA c'è la riga da completare

#### Scenario: Azienda senza nome
- **WHEN** l'azienda del tirocinio si chiama «Azienda senza nome n. 1234»
- **THEN** al posto della ragione sociale c'è la riga da completare

