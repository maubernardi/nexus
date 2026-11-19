## ADDED Requirements

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
I documenti SHALL riportare i dati del soggetto promotore presi dalla configurazione del server, non dai modelli:
- denominazione, sede e codice fiscale;
- legale rappresentante con ruolo, luogo e data di nascita;
- telefono, mail e PEC;
- posizione INAIL;
- polizza RC con compagnia, numero e scadenza.

I dati MUST NOT stare nel repository. Un dato non configurato SHALL restare una riga da completare.

#### Scenario: Dati configurati
- **WHEN** sul server è configurata la polizza RC con la scadenza «22/07/2027»
- **THEN** i documenti riportano polizza e scadenza

#### Scenario: Dati non configurati
- **WHEN** sul server manca la PEC del promotore
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
