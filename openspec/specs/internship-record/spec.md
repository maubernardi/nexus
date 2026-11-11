# internship-record Specification

## Purpose
Scheda amministrativa del tirocinio, del solo Tutor della segnalazione: dati del tirocinio e del beneficiario per i
documenti, dati dell'azienda (legale rappresentante, codice ATECO), griglia oraria e monte ore. Si compila
dall'appuntamento fissato e si corregge in qualunque momento.
## Requirements
### Requirement: Scheda del tirocinio del Tutor
Il Tutor della segnalazione SHALL poter compilare la scheda amministrativa del tirocinio dopo l'appuntamento fissato e
correggerla in qualunque momento, anche a tirocinio avviato. La scheda contiene:
- tipo di tirocinio, data di inizio, data di fine prevista, sede (l'orario è la griglia oraria di US-1002);
- uno o più tutor aziendali, con nome e recapito;
- il codice ATECO scelto tra quelli dell'azienda;
- codice fiscale, data e luogo di nascita e residenza del beneficiario;
- periodi di sospensione;
- note.

La scheda SHALL potersi salvare anche incompleta. Solo il Tutor della segnalazione SHALL leggerla e modificarla: gli
altri utenti, Call Center e ADMIN compresi, MUST NOT accedervi.

#### Scenario: Primo salvataggio parziale
- **WHEN** il Tutor, con l'appuntamento fissato, salva la scheda con il solo tipo di tirocinio e la data di inizio
- **THEN** la scheda è salvata e, riaprendola, mostra quei dati con gli altri campi vuoti

#### Scenario: Tirocinio avviato
- **WHEN** il Tutor corregge la sede nella scheda di un tirocinio già avviato
- **THEN** la modifica è salvata

#### Scenario: Prima dell'appuntamento
- **WHEN** il Tutor apre la scheda di una segnalazione con la proposta appena accolta
- **THEN** la richiesta è rifiutata: la scheda si compila dopo l'appuntamento fissato

#### Scenario: Altri utenti
- **WHEN** un altro Tutor dello stesso progetto, l'operatore del Call Center o l'ADMIN aprono la scheda
- **THEN** l'accesso è negato

#### Scenario: Modifica concorrente
- **WHEN** il Tutor salva la scheda con una versione superata
- **THEN** il salvataggio è rifiutato con un conflitto e nulla cambia

### Requirement: Controlli sui dati della scheda
La scheda MUST controllare i dati presenti:
- la fine prevista non precede l'inizio;
- il codice fiscale è valido e corrisponde alla data di nascita, se indicata;
- il tipo di tirocinio è attivo;
- il codice ATECO è dell'azienda della segnalazione;
- ogni tutor aziendale ha il nome.

I periodi di sospensione MUST avere inizio e fine nell'ordine giusto, MUST NOT sovrapporsi e MUST stare dentro il
periodo del tirocinio quando le date sono note. Il database MUST impedire le sovrapposizioni.

#### Scenario: Sospensioni sovrapposte
- **WHEN** il Tutor indica una sospensione dal 22/12 al 6/1 e un'altra dal 6/1 al 10/1
- **THEN** il campo della seconda segnala la sovrapposizione e nulla è salvato

#### Scenario: Codice fiscale e data di nascita
- **WHEN** il codice fiscale indica un'altra data di nascita
- **THEN** il campo del codice fiscale lo segnala

#### Scenario: Codice ATECO di un'altra azienda
- **WHEN** il Tutor sceglie un codice ATECO che non è dell'azienda della segnalazione
- **THEN** la scelta è rifiutata

### Requirement: Dati dell'azienda dalla scheda
Dalla scheda il Tutor SHALL vedere il legale rappresentante e i codici ATECO dell'azienda. SHALL poter inserire il
legale rappresentante se manca, MUST NOT correggerlo: la correzione spetta al Call Center. SHALL poter aggiungere un
codice ATECO, che va nella scheda dell'azienda. Un codice ATECO scelto in una scheda MUST NOT poter essere tolto
dall'azienda.

#### Scenario: Legale rappresentante mancante
- **WHEN** l'azienda non ha il legale rappresentante e il Tutor lo inserisce dalla scheda
- **THEN** il legale rappresentante è salvato nell'azienda e l'audit dell'azienda registra il Tutor come autore

#### Scenario: Legale rappresentante già presente
- **WHEN** il Tutor prova a inserire il legale rappresentante di un'azienda che l'ha già
- **THEN** la richiesta è rifiutata: per correggerlo ci si rivolge al Call Center

#### Scenario: Codice in uso
- **WHEN** il Call Center prova a togliere dall'azienda un codice ATECO scelto in una scheda
- **THEN** la richiesta è rifiutata indicando la segnalazione

### Requirement: Appuntamento fallito dopo la compilazione
Se l'appuntamento con l'azienda non va a buon fine, la scheda SHALL tenere solo i dati del beneficiario. Gli altri
dati riguardavano quell'azienda e MUST essere azzerati, con l'azzeramento registrato nell'audit.

#### Scenario: Scheda dopo l'appuntamento fallito
- **WHEN** il Tutor registra l'appuntamento non andato a buon fine di una segnalazione con la scheda compilata
- **THEN** la scheda conserva codice fiscale, nascita e residenza del beneficiario e non ha più tipo, date, sede, ATECO, tutor aziendali, sospensioni e note

### Requirement: Audit della scheda
Ogni salvataggio con modifiche SHALL essere registrato nell'audit della segnalazione con autore e istante. I dati del
tirocinio SHALL comparire con i valori. Per i dati personali (beneficiario, tutor aziendali, note) l'audit MUST
registrare solo che sono cambiati. La cronologia della segnalazione SHALL mostrare l'aggiornamento della scheda, senza
dati.

#### Scenario: Audit senza dati personali
- **WHEN** il Tutor salva il codice fiscale del beneficiario e la data di inizio
- **THEN** l'audit registra la data di inizio con il valore e il codice fiscale solo come cambiato

### Requirement: Griglia oraria del tirocinio
La scheda del tirocinio SHALL avere due fasce orarie, precompilate con 9:00–13:00 e 14:00–18:00 e modificabili. Per
ogni giorno della settimana il Tutor SHALL poter scegliere la prima fascia, la seconda o entrambe, e indicare le ore
del giorno. Le ore indicate MUST NOT superare la durata delle fasce scelte; senza ore valgono le fasce intere. Le
fasce MUST essere in ordine e il totale settimanale MUST NOT superare le 40 ore.

#### Scenario: Fasce intere e ore ridotte
- **WHEN** il Tutor sceglie per il lunedì la prima fascia e per il martedì entrambe le fasce con 3 ore
- **THEN** la scheda conta 4 ore il lunedì e 3 il martedì, e il totale settimanale è di 7 ore

#### Scenario: Ore oltre le fasce
- **WHEN** il Tutor indica 5 ore per un giorno con la sola prima fascia di 4 ore
- **THEN** il campo delle ore di quel giorno lo segnala e nulla è salvato

#### Scenario: Fasce modificate
- **WHEN** il Tutor cambia la prima fascia in 8:30–12:30
- **THEN** i giorni con la prima fascia contano le ore della nuova fascia

### Requirement: Monte ore del tirocinio
La scheda SHALL mostrare il monte ore complessivo. Il calcolo va dalla data di inizio alla fine prevista con la
griglia oraria e salta i giorni dei periodi di sospensione. Salta anche le festività nazionali, se la scheda lo
chiede: la scelta è attiva di default. Il monte ore SHALL ricalcolarsi quando cambiano griglia, date o sospensioni. Senza
date o senza griglia, il monte ore MUST NOT essere indicato.

#### Scenario: Festività e sospensioni
- **WHEN** la griglia ha 4 ore il lunedì e il martedì, il tirocinio va da lunedì 7 a domenica 13 dicembre 2026 e le
  festività nazionali sono escluse
- **THEN** il monte ore è di 4 ore, perché martedì 8 dicembre è festivo

#### Scenario: Festività incluse
- **WHEN** nella stessa scheda il Tutor toglie l'esclusione delle festività
- **THEN** il monte ore diventa di 8 ore

#### Scenario: Ricalcolo con una sospensione
- **WHEN** il Tutor aggiunge una sospensione per lunedì 7 dicembre
- **THEN** il monte ore scende di 4 ore

### Requirement: Scheda completa per l'avvio
La scheda SHALL indicare che cosa manca per avviare il tirocinio, in base ai dati salvati. Per l'avvio servono:
- tipo di tirocinio, data di inizio, data di fine prevista, sede, codice ATECO;
- almeno un tutor aziendale e la griglia oraria con almeno un giorno;
- codice fiscale, data e luogo di nascita e residenza del beneficiario;
- il legale rappresentante dell'azienda con codice fiscale, nascita e residenza.

Sospensioni e note MUST NOT essere richieste.

#### Scenario: Elenco di ciò che manca
- **WHEN** il Tutor apre una scheda senza codice fiscale del beneficiario e senza tutor aziendali
- **THEN** la pagina elenca proprio questi due dati, ciascuno con il collegamento al campo

#### Scenario: Legale rappresentante incompleto
- **WHEN** il legale rappresentante dell'azienda è indicato senza codice fiscale
- **THEN** la pagina dice che i suoi dati vanno completati dal Call Center

### Requirement: Tipi ammessi dal progetto
La scheda del tirocinio SHALL proporre solo i tipi di tirocinio attivi ammessi dal progetto della segnalazione, più il
tipo già scelto. Il sistema MUST rifiutare un tipo nuovo che il progetto non ammette; un tipo già scelto MUST restare
valido anche se nel frattempo il progetto non lo ammette più.

#### Scenario: Segnalazione di GOL
- **WHEN** il Tutor apre la scheda del tirocinio di una segnalazione di GOL
- **THEN** può scegliere solo Extracurriculare e Di inclusione

#### Scenario: Tipo non ammesso
- **WHEN** il Tutor salva la scheda di una segnalazione di GOL con il tipo Curriculare
- **THEN** il campo segnala che il tipo non è ammesso dal progetto e nulla è salvato

### Requirement: Tirocinio di inclusione
Il tipo di tirocinio «Di inclusione» (codice `INCLUSIONE`) SHALL potersi scegliere solo se il beneficiario della
segnalazione ha l'ente che lo ha in carico e nome, mail e telefono del case manager. La scheda del tirocinio SHALL
indicare cosa manca; il sistema MUST rifiutare la scelta altrimenti. Un tipo di inclusione già scelto resta valido.

#### Scenario: Dati mancanti
- **WHEN** il Tutor sceglie «Di inclusione» per un beneficiario senza ente
- **THEN** il campo segnala che mancano l'ente e i dati del case manager e nulla è salvato

#### Scenario: Dati completi
- **WHEN** il beneficiario ha ente e case manager con nome, mail e telefono
- **THEN** il Tutor sceglie «Di inclusione» e la scheda si salva

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

### Requirement: Funzione o ruolo dei tutor aziendali
Nella scheda del tirocinio ogni tutor aziendale SHALL avere, oltre al nome e al recapito, la funzione o ruolo in
azienda: testo libero facoltativo, al massimo 200 caratteri. Un testo di soli spazi MUST essere salvato come vuoto.
L'audit SHALL registrare il cambio del ruolo come cambio dei tutor aziendali, senza valori.

#### Scenario: Tutor con il ruolo
- **WHEN** il Tutor scrive «Responsabile di produzione» come ruolo del primo tutor aziendale e salva
- **THEN** riaprendo la scheda il tutor ha quel ruolo

#### Scenario: Ruolo non indicato
- **WHEN** il Tutor salva un tutor aziendale senza il ruolo
- **THEN** la scheda è salvata e il tutor conta per l'avvio del tirocinio

#### Scenario: Audit del ruolo
- **WHEN** il Tutor cambia soltanto il ruolo di un tutor aziendale
- **THEN** l'audit registra che i tutor aziendali sono cambiati, senza il ruolo

### Requirement: Convenzione del tirocinio
La scheda del tirocinio SHALL avere una sola convenzione, inserita dal Tutor: numero (testo, al massimo 50 caratteri),
data e data di stipula, tutti facoltativi come gli altri dati della scheda. La durata della convenzione MUST coincidere
con le date del tirocinio e la convenzione vale per un solo tirocinio: la scheda li mostra senza chiederli.

#### Scenario: Convenzione salvata
- **WHEN** il Tutor scrive il numero «2026/145», la data 20/11/2026 e la data di stipula 25/11/2026 e salva
- **THEN** riaprendo la scheda la convenzione ha quei dati e la durata è quella del tirocinio

#### Scenario: Numero di soli spazi
- **WHEN** il Tutor salva un numero di soli spazi
- **THEN** la convenzione resta senza numero

### Requirement: Progetti formativi
La convenzione SHALL avere uno o più progetti formativi, al massimo 10, numerati 1, 2, 3… nell'ordine delle date.
Ogni progetto SHALL avere data di inizio, data di fine e mansione (al massimo 200 caratteri), obbligatorie, e il
dettaglio della mansione, testo facoltativo di al massimo 2000 caratteri. Tutti gli altri dati, orario compreso, sono
quelli della convenzione e del tirocinio. Un nuovo progetto SHALL proporre come mansione quella del progetto precedente
o, per il primo, la mansione della segnalazione.

#### Scenario: Riformulazione
- **WHEN** in un tirocinio dal 1/12/2026 al 31/5/2027 il Tutor ha il progetto 1 dal 1/12/2026 al 28/2/2027 e aggiunge il
  progetto 2 dal 1/3/2027 al 31/5/2027 con un'altra mansione
- **THEN** la scheda salvata ha i due progetti numerati 1 e 2, ciascuno con le sue date e la sua mansione

#### Scenario: Progetto tolto
- **WHEN** il Tutor toglie l'ultimo progetto e salva
- **THEN** la scheda ha i progetti rimasti, con la stessa numerazione

### Requirement: Controlli sui progetti formativi
Le date di un progetto formativo MUST stare dentro quelle del tirocinio: dall'inizio alla fine prevista, estremi
compresi. Con almeno un progetto la scheda MUST avere la data di inizio e la fine prevista. Ogni progetto MUST
iniziare dopo la fine del precedente: i progetti della stessa convenzione non si sovrappongono, neanche di un giorno.
Altrimenti il salvataggio è rifiutato con l'errore sul campo e la spiegazione. Il database MUST impedire le
sovrapposizioni.

#### Scenario: Progetto fuori dal tirocinio
- **WHEN** il progetto finisce dopo la fine prevista del tirocinio
- **THEN** il salvataggio è rifiutato e la data di fine del progetto lo spiega

#### Scenario: Stesso giorno in due progetti
- **WHEN** il progetto 1 finisce il 28/2/2027 e il progetto 2 inizia il 28/2/2027
- **THEN** il salvataggio è rifiutato e la data di inizio del progetto 2 indica che deve iniziare dopo il 28/02/2027

#### Scenario: Tirocinio accorciato
- **WHEN** il Tutor anticipa la fine prevista del tirocinio a una data in cui un progetto è ancora in corso
- **THEN** il salvataggio è rifiutato e la data di fine di quel progetto lo spiega

#### Scenario: Progetti senza le date del tirocinio
- **WHEN** la scheda ha un progetto formativo ma non la data di inizio del tirocinio
- **THEN** il salvataggio è rifiutato e la data di inizio del tirocinio lo spiega

### Requirement: Durata e monte ore dei progetti formativi
Per ogni progetto salvato la scheda SHALL mostrare la durata in mesi e il monte ore, calcolati sulle date del progetto.
I mesi MUST essere arrotondati al mese più vicino, con le sospensioni comprese: 15 giorni o più contano come un mese
in più. Il monte ore SHALL seguire le regole del monte ore del tirocinio: griglia oraria, sospensioni e, se escluse,
festività nazionali. Senza griglia il monte ore del progetto MUST NOT essere indicato.

#### Scenario: Mesi arrotondati
- **WHEN** un progetto va dal 1/12/2026 al 15/1/2027 e un altro dal 16/1/2027 al 28/2/2027
- **THEN** il primo dura 2 mesi (1 mese e 15 giorni) e il secondo 1 mese (1 mese e 13 giorni)

#### Scenario: Monte ore del progetto
- **WHEN** la griglia ha 4 ore il lunedì e il martedì, le festività sono escluse e un progetto va da lunedì 7 a
  domenica 13 dicembre 2026
- **THEN** il monte ore del progetto è di 4 ore, perché martedì 8 dicembre è festivo

### Requirement: Audit della convenzione e dei progetti formativi
Ogni salvataggio che cambia la convenzione o i progetti formativi SHALL essere registrato nell'audit della
segnalazione, con autore e istante:
- la convenzione con i valori;
- i progetti con numero, date e mansione;
- il dettaglio della mansione, testo libero, solo come «cambiato».

Il motivo di una riformulazione MUST NOT essere chiesto né registrato.

#### Scenario: Nuovo progetto nell'audit
- **WHEN** il Tutor aggiunge il progetto 2 con il dettaglio della mansione e salva
- **THEN** l'audit registra l'elenco dei progetti prima e dopo, con date e mansione, e il dettaglio solo come cambiato

### Requirement: Convenzione e progetti con l'appuntamento fallito
Se l'appuntamento con l'azienda non va a buon fine, la convenzione e i progetti formativi MUST essere azzerati
insieme agli altri dati del tirocinio, perché riguardavano quell'azienda.

#### Scenario: Appuntamento fallito
- **WHEN** il Tutor registra l'appuntamento non andato a buon fine di una segnalazione con convenzione e progetti
- **THEN** la scheda non ha più numero e date della convenzione né progetti formativi

### Requirement: Convenzione e progetti formativi per l'avvio
Per avviare il tirocinio la scheda MUST avere anche la convenzione completa (numero, data e data di stipula) e almeno
un progetto formativo. Se mancano, la scheda SHALL elencarli tra i dati che mancano per l'avvio, ciascuno con il
collegamento alla sua sezione, e l'avvio MUST essere rifiutato con l'elenco di ciò che manca.

#### Scenario: Convenzione senza data di stipula
- **WHEN** la scheda è completa tranne la data di stipula della convenzione
- **THEN** la scheda elenca la convenzione tra i dati mancanti e l'avvio è rifiutato

#### Scenario: Nessun progetto formativo
- **WHEN** la scheda ha la convenzione ma nessun progetto formativo
- **THEN** la scheda elenca «almeno un progetto formativo» tra i dati mancanti, con il collegamento alla sezione dei
  progetti

#### Scenario: Scheda completa
- **WHEN** la scheda ha anche la convenzione completa e un progetto formativo
- **THEN** il tirocinio si può avviare

