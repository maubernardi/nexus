# company-registry Specification

## Purpose
Anagrafica delle aziende ospitanti, gestita da Call Center e ADMIN, base per l'abbinamento delle mansioni.
## Requirements
### Requirement: Gestione delle aziende
Il sistema SHALL permettere a Call Center e ADMIN di cercare, registrare, modificare, disattivare e riattivare le
aziende, senza mai cancellarle. Il Tutor SHALL NOT accedere all'anagrafica.

#### Scenario: Registrazione
- **WHEN** un operatore registra un'azienda con ragione sociale e P.IVA valide
- **THEN** l'azienda è attiva e l'audit registra `CREATE` con autore e dati

#### Scenario: Disattivazione
- **WHEN** un operatore disattiva un'azienda
- **THEN** non compare più nella ricerca predefinita, resta consultabile tra le disattivate e l'audit registra
  `DEACTIVATE`

#### Scenario: Accesso del Tutor
- **WHEN** un Tutor richiede l'anagrafica delle aziende
- **THEN** la risposta è `403`

### Requirement: Partita IVA valida e univoca
Il sistema SHALL accettare solo P.IVA di 11 cifre e SHALL rifiutare una P.IVA già usata da un'altra azienda con un
errore sul campo. La P.IVA segnaposto «00000000000» delle aziende arrivate dal database attuale senza partita IVA
SHALL NOT contare per l'unicità: più aziende possono averla.

#### Scenario: P.IVA duplicata
- **WHEN** si registra o modifica un'azienda con la P.IVA di un'altra
- **THEN** la risposta è `400` con errore sul campo `vatCode`

#### Scenario: Più aziende senza partita IVA
- **WHEN** il database contiene già un'azienda con la P.IVA «00000000000» e ne arriva un'altra con la stessa P.IVA
- **THEN** il database le accetta entrambe, mentre una P.IVA vera ripetuta resta rifiutata dal vincolo
  `uq_company_vat_code`

### Requirement: Modifiche concorrenti e tracciate
Il sistema SHALL rifiutare con `409` una modifica basata su una versione superata e SHALL registrare nell'audit solo i
campi effettivamente cambiati, con i valori prima e dopo.

#### Scenario: Due operatori sulla stessa azienda
- **WHEN** due operatori modificano la stessa azienda partendo dalla stessa versione
- **THEN** il primo salva, il secondo riceve `409` e un messaggio che lo invita a ricaricare

### Requirement: Mansioni dell'azienda
Il sistema SHALL permettere a Call Center e ADMIN di aggiungere mansioni a un'azienda attiva (titolo, tipologia, zona,
descrizione), modificarle, ritirarle e rimetterle a disposizione, con blocco ottimistico e audit di ogni azione. Lo
stato LIBERA/BLOCCATA SHALL NOT essere modificabile a mano.

#### Scenario: Nuova mansione
- **WHEN** un operatore aggiunge una mansione a un'azienda attiva
- **THEN** la mansione è LIBERA e a disposizione, e l'audit registra `CREATE`

#### Scenario: Stato in sola lettura
- **WHEN** una modifica tenta di impostare lo stato BLOCCATA
- **THEN** lo stato resta invariato

#### Scenario: Mansione bloccata
- **WHEN** un operatore tenta di ritirare una mansione bloccata da una segnalazione
- **THEN** la risposta è `409` con il numero della segnalazione, e la mansione resta a disposizione

#### Scenario: Azienda disattivata
- **WHEN** si aggiunge una mansione a un'azienda disattivata
- **THEN** la risposta è `409`

### Requirement: Disattivazione con proposte in corso
Il sistema SHALL rifiutare con `409` la disattivazione di un'azienda che ha mansioni BLOCCATE da segnalazioni, indicando
per ciascuna il titolo della mansione e il numero della segnalazione; l'azienda resta attiva.

#### Scenario: Mansione bloccata
- **WHEN** un operatore disattiva un'azienda la cui mansione «Magazziniere» è bloccata dalla segnalazione n. 30
- **THEN** la risposta è `409` con il messaggio "Non si può disattivare l'azienda: la mansione «Magazziniere» è bloccata
  dalla segnalazione n. 30" e la scheda azienda lo mostra

### Requirement: Diario delle note dell'azienda
Il sistema SHALL consentire a Call Center e ADMIN di aggiungere note di testo (obbligatorio, al massimo 2000 caratteri)
alla scheda di un'azienda e di consultarle dalla più recente, con data, ora e autore. Le note MUST NOT essere modificate
né cancellate, e il database SHALL rifiutarne modifica, cancellazione e svuotamento. L'aggiunta SHALL essere registrata
nell'audit senza il testo della nota.

#### Scenario: Aggiunta di una nota
- **WHEN** un operatore aggiunge la nota "Chiusura per ferie ad agosto"
- **THEN** la nota compare in cima al diario con data, ora e nome dell'operatore, e l'audit registra `NOTE_ADD` senza testo

#### Scenario: Nota vuota
- **WHEN** l'operatore invia una nota vuota o di soli spazi
- **THEN** il sistema la rifiuta indicando il campo

#### Scenario: Note immodificabili
- **WHEN** si tenta di modificare o cancellare una nota direttamente nel database
- **THEN** l'operazione è rifiutata

#### Scenario: Tutor
- **WHEN** un Tutor chiede le note di un'azienda
- **THEN** la risposta è `403`

### Requirement: Legale rappresentante dell'azienda
Il Call Center e l'ADMIN SHALL poter registrare e correggere nella scheda dell'azienda il legale rappresentante, che
può non coincidere con il referente: nome e cognome (obbligatori), codice fiscale, data e luogo di nascita, residenza.
Il codice fiscale MUST essere valido, carattere di controllo compreso. Sono dati personali: l'audit SHALL registrare
quali campi sono cambiati, MUST NOT registrarne i valori.

#### Scenario: Prima registrazione
- **WHEN** l'operatore indica il legale rappresentante con un codice fiscale scritto in minuscolo
- **THEN** i dati sono salvati con il codice fiscale in maiuscolo e la scheda li mostra

#### Scenario: Codice fiscale non valido
- **WHEN** il carattere di controllo del codice fiscale non torna
- **THEN** il campo segnala "Codice fiscale non valido" e nulla è salvato

#### Scenario: Audit senza valori
- **WHEN** l'operatore corregge la residenza
- **THEN** l'audit registra che la residenza è cambiata, senza il vecchio né il nuovo indirizzo

### Requirement: Codici ATECO dell'azienda
Il Call Center e l'ADMIN SHALL poter aggiungere e togliere i codici ATECO di un'azienda, uno o più, con una
descrizione facoltativa. Un'azienda MUST NOT avere due volte lo stesso codice; il formato SHALL essere `NN`, `NN.NN` o
`NN.NN.NN`.

#### Scenario: Aggiunta
- **WHEN** l'operatore aggiunge 56.10.11 con la descrizione
- **THEN** il codice compare nell'elenco dell'azienda e l'aggiunta è nell'audit

#### Scenario: Codice già presente
- **WHEN** l'operatore aggiunge un codice che l'azienda ha già
- **THEN** il campo lo segnala

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

### Requirement: Dati da completare
Un'azienda arrivata dal database attuale SHALL avere al posto dei dati che mancano i valori segnaposto:
- la P.IVA «00000000000»;
- il nome «Azienda senza nome n. ‹numero›».

Il sistema SHALL indicare per ogni azienda quali di questi dati sono da completare.
- **Nell'elenco delle aziende** SHALL comparire «Da completare» nella colonna del dato che manca, e l'elenco SHALL
  potersi filtrare sulle sole aziende da completare.
- **Nella scheda e nel dettaglio dell'abbinamento** SHALL comparire quali dati mancano, MUST NOT comparire la P.IVA
  segnaposto.

Registrazione e modifica dell'azienda MUST rifiutare con `400` sul campo:
- la P.IVA «00000000000»;
- un nome che comincia con «Azienda senza nome n.», senza distinguere maiuscole e minuscole.

Nella scheda i campi con il segnaposto SHALL aprirsi vuoti, così il salvataggio chiede i dati veri. Le altre sezioni
della scheda (legale rappresentante, codici ATECO, mansioni, note) SHALL restare modificabili.

#### Scenario: Azienda senza partita IVA nell'elenco
- **WHEN** l'operatore cerca un'azienda che ha la P.IVA «00000000000»
- **THEN** nella colonna della partita IVA compare «Da completare» al posto degli zeri

#### Scenario: Filtro sulle aziende da completare
- **WHEN** l'operatore sceglie «Solo le aziende da completare»
- **THEN** l'elenco mostra solo le aziende senza partita IVA o senza nome, e il filtro resta nell'indirizzo della
  pagina

#### Scenario: Scheda da completare
- **WHEN** l'operatore apre la scheda dell'azienda «Azienda senza nome n. 1234», che non ha la partita IVA
- **THEN** la pagina dice che mancano ragione sociale e partita IVA, e nel modulo i due campi sono vuoti e obbligatori

#### Scenario: Segnaposto rifiutato
- **WHEN** si registra o modifica un'azienda con la P.IVA «00000000000» o con il nome «azienda senza nome n. 12»
- **THEN** la risposta è `400` con l'errore sul campo `vatCode` o `name`, che chiede il dato vero, e nulla è salvato

#### Scenario: Completamento
- **WHEN** l'operatore inserisce ragione sociale e partita IVA vere e salva
- **THEN** l'azienda non è più da completare e l'audit registra i valori prima e dopo

### Requirement: Categoria merceologica e attività dell'azienda
Il Call Center e l'ADMIN SHALL poter indicare nella scheda dell'azienda:
- la categoria merceologica, facoltativa, scelta tra le voci attive del catalogo;
- la descrizione dell'attività, facoltativa, al massimo 200 caratteri.

Un'azienda con una categoria poi disattivata SHALL mantenerla, anche quando si salva la scheda; una categoria
disattivata o inesistente MUST NOT essere scelta altrimenti (`400` sul campo). L'audit SHALL registrare categoria e
attività cambiate, con i valori prima e dopo. L'elenco delle aziende SHALL mostrare la categoria e SHALL potersi
filtrare per categoria, insieme alla ricerca e agli altri filtri, con il filtro nell'indirizzo della pagina. Il
dettaglio dell'abbinamento SHALL mostrare categoria e attività.

#### Scenario: Categoria e attività
- **WHEN** l'operatore sceglie «Ristorante / Trattoria / Bistrot», scrive «Pizzeria» come attività e salva
- **THEN** la scheda e l'elenco mostrano la categoria, e l'audit registra categoria e attività con i valori prima e
  dopo

#### Scenario: Filtro per categoria
- **WHEN** l'operatore sceglie nell'elenco la categoria «Bar / Pub»
- **THEN** l'elenco mostra solo le aziende di quella categoria e l'indirizzo della pagina contiene il filtro

#### Scenario: Categoria disattivata sull'azienda
- **WHEN** l'operatore salva la scheda di un'azienda la cui categoria è stata disattivata
- **THEN** la categoria resta e il salvataggio riesce

#### Scenario: Categoria non valida
- **WHEN** il modulo invia una categoria disattivata che l'azienda non ha, o una categoria inesistente
- **THEN** la risposta è `400` con l'errore sul campo `businessCategoryId`

### Requirement: Referenti dell'azienda
Il Call Center e l'ADMIN SHALL poter registrare nella scheda dell'azienda **fino a 10 referenti**, in ordine. Ogni
referente ha nome, ruolo, telefono, cellulare ed email, tutti facoltativi. Ogni referente MUST avere almeno il nome o il
ruolo. Il ruolo si sceglie tra le voci attive del catalogo. Un referente con un ruolo poi disattivato SHALL mantenerlo
quando si salva la scheda. Un ruolo disattivato o inesistente MUST NOT essere scelto altrimenti (`400` sul campo).

Il referente registrato prima di questa funzione SHALL diventare il primo referente, senza ruolo. I referenti sono
dati personali: l'audit dell'azienda MUST registrarli solo come «cambiati», senza valori. L'elenco delle aziende SHALL
mostrare il primo referente e quanti sono gli altri. Il dettaglio dell'abbinamento SHALL mostrare tutti i referenti con
ruolo e recapiti.

#### Scenario: Due referenti con il ruolo
- **WHEN** l'operatore aggiunge «Paola Neri», Titolare, con il cellulare, e un secondo referente, Responsabile del
  personale, con l'email, e salva
- **THEN** la scheda mostra i due referenti in quell'ordine e l'audit registra i referenti come cambiati, senza valori

#### Scenario: Solo il ruolo
- **WHEN** l'operatore aggiunge un referente con il solo ruolo «Responsabile del personale»
- **THEN** il salvataggio riesce

#### Scenario: Né nome né ruolo
- **WHEN** l'operatore aggiunge un referente con il solo telefono
- **THEN** la risposta è `400` con l'errore sul nome di quel referente

#### Scenario: Referente di prima
- **WHEN** l'operatore apre un'azienda che aveva il referente «Mario Rossi»
- **THEN** «Mario Rossi» è il primo referente, senza ruolo

#### Scenario: Ruolo disattivato sul referente
- **WHEN** l'operatore salva la scheda di un'azienda con un referente il cui ruolo è stato disattivato
- **THEN** il ruolo resta e il salvataggio riesce

#### Scenario: Ruolo non valido
- **WHEN** il modulo invia un ruolo disattivato che nessun referente dell'azienda ha, o un ruolo inesistente
- **THEN** la risposta è `400` con l'errore sul ruolo di quel referente

#### Scenario: Troppi referenti
- **WHEN** il modulo invia 11 referenti
- **THEN** la risposta è `400`

### Requirement: Recapiti e sede operativa dell'azienda
L'azienda SHALL avere, oltre a telefono ed email, il **cellulare**, il **fax** e una **seconda email**, facoltativi e
con gli stessi controlli di formato di telefono ed email. L'azienda SHALL avere anche una **sede operativa**
facoltativa, con l'indirizzo e il comune scelto dall'elenco ufficiale, come la sede legale. L'audit MUST registrare i
recapiti nuovi solo come «cambiati», come telefono ed email, e SHALL registrare la sede operativa con i valori prima e
dopo. Il dettaglio dell'abbinamento SHALL mostrare i recapiti nuovi e la sede operativa.

#### Scenario: Recapiti nuovi
- **WHEN** l'operatore indica cellulare, fax e seconda email e salva
- **THEN** la scheda li mostra e l'audit li registra come cambiati, senza valori

#### Scenario: Sede operativa
- **WHEN** l'operatore indica «Via dei Mille 3» e sceglie «Prato (PO)» come sede operativa e salva
- **THEN** il dettaglio dell'abbinamento mostra la sede operativa «Via dei Mille 3, Prato (PO)» e l'audit registra
  indirizzo e comune

#### Scenario: Fax non valido
- **WHEN** l'operatore scrive lettere nel fax
- **THEN** la risposta è `400` con l'errore sul campo `fax`

### Requirement: Elenco delle aziende a pagine
L'elenco delle aziende SHALL arrivare una pagina alla volta, in ordine di ragione sociale, con il numero delle aziende
che rispondono ai filtri. L'operatore SHALL poter scegliere 10, 25, 50 o 100 aziende per pagina (25 se non sceglie);
il sistema MUST NOT restituire più di 100 aziende per richiesta. Pagina e aziende per pagina SHALL restare
nell'indirizzo, come i filtri; un filtro nuovo SHALL ripartire dalla prima pagina.

#### Scenario: Prima pagina
- **WHEN** il Call Center apre l'elenco con 13.799 aziende
- **THEN** vede le prime 25 in ordine di ragione sociale, «13.799 aziende: dalla 1 alla 25» e «Pagina 1 di 552»

#### Scenario: Pagina successiva
- **WHEN** l'operatore sceglie «Successiva»
- **THEN** vede le aziende dalla 26 alla 50 e l'indirizzo ha `pagina=2`

#### Scenario: Aziende per pagina
- **WHEN** l'operatore sceglie 100 dal menu «Aziende per pagina»
- **THEN** vede le prime 100 aziende, dalla prima pagina

#### Scenario: Filtro nuovo
- **WHEN** alla pagina 3 l'operatore spunta «Solo le aziende da completare»
- **THEN** vede la prima pagina delle aziende da completare, con le stesse aziende per pagina

#### Scenario: Troppe aziende per pagina
- **WHEN** una richiesta all'API chiede 1.000 aziende per pagina, o una pagina negativa
- **THEN** la risposta è `400`

#### Scenario: Pagina oltre l'ultima
- **WHEN** un indirizzo chiede la pagina 9 e le pagine sono 2
- **THEN** l'elenco passa alla pagina 2

### Requirement: Affidabilità e interesse al progetto dell'azienda
L'azienda SHALL avere due valutazioni facoltative del Call Center: l'**affidabilità**, da 1 a 4 stelle, e l'**interesse
al progetto**, da 1 a 2 pollici alzati. Operatori del Call Center e ADMIN SHALL poterle scegliere, cambiare e togliere
dal modulo dell'azienda, nuova o esistente. Il sistema MUST rifiutare con `400` un valore fuori da questi intervalli,
e il database MUST rifiutarlo con un vincolo. Ogni cambio SHALL entrare nell'audit dell'azienda con il valore di prima
e di dopo.

Nel modulo ogni valutazione SHALL essere un gruppo di opzioni: tutte le icone vuote finché non si sceglie, e piene fino
a quella scelta. Il gruppo SHALL funzionare da tastiera e avere un nome per ogni opzione, per esempio «3 stelle su 4»;
lo stato MUST NOT dipendere solo dal colore.

L'elenco delle aziende SHALL mostrare stelle e pollici in due colonne, e offrire due filtri che si sommano agli altri:
«almeno N stelle» (da 1 a 4) e «interesse al progetto» (almeno un pollice, oppure due). I filtri SHALL restare
nell'indirizzo come gli altri.

#### Scenario: Scelta delle stelle
- **WHEN** l'operatore apre la scheda di un'azienda senza affidabilità, clicca la terza stella e salva
- **THEN** prima del clic vede 4 stelle vuote; dopo, le prime tre piene. L'azienda ha affidabilità 3 e l'audit
  registra il cambio da nessuna a 3

#### Scenario: Valutazione tolta
- **WHEN** l'operatore toglie l'interesse al progetto di un'azienda con due pollici e salva
- **THEN** l'azienda resta senza interesse al progetto e l'audit registra il cambio da 2 a nessuno

#### Scenario: Da tastiera
- **WHEN** l'operatore arriva alle stelle con il tasto Tab e preme la freccia a destra
- **THEN** si sceglie la stella successiva, e il lettore di schermo legge per esempio «Affidabilità, 2 stelle su 4»

#### Scenario: Valore fuori intervallo
- **WHEN** una richiesta all'API salva un'azienda con affidabilità 5 o interesse 3
- **THEN** la risposta è `400`, con l'errore sul campo, e l'azienda non cambia

#### Scenario: Colonne dell'elenco
- **WHEN** il Call Center apre l'elenco delle aziende
- **THEN** ogni riga mostra le stelle e i pollici dell'azienda, con il testo per il lettore di schermo; un trattino
  per quelle senza valutazione

#### Scenario: Filtro per affidabilità
- **WHEN** l'operatore sceglie «Almeno 3 stelle»
- **THEN** vede, dalla prima pagina, solo le aziende con 3 o 4 stelle, e l'indirizzo ricorda il filtro

#### Scenario: Filtro per interesse
- **WHEN** l'operatore sceglie «Due pollici»
- **THEN** vede solo le aziende con interesse al progetto 2

#### Scenario: Ruoli
- **WHEN** un Tutor chiede l'elenco o salva un'azienda dall'API
- **THEN** la risposta è `403`

### Requirement: Nome senza i simboli del database attuale
Nel database attuale affidabilità e interesse si segnano all'inizio del nome: ogni `$` è un punto di affidabilità,
ogni `£` di interesse; una `&` o un `^` iniziali sono refusi. Le aziende già presenti SHALL essere convertite senza
perdere il dato:
- ogni `$` all'inizio del nome vale una stella, al massimo 4 (cinque `$` valgono 4 stelle);
- ogni `£` all'inizio del nome vale un pollice, al massimo 2;
- `&` e `^` all'inizio del nome si tolgono;
- i simboli si contano in qualunque ordine, anche separati da spazi; poi il nome si ripulisce.

La `&` dentro il nome, come in «Rossi & C.», MUST restare. Un nome che resterebbe vuoto SHALL diventare il segnaposto
dei nomi da completare.

Dopo la conversione il nome di un'azienda MUST NOT iniziare con `$`, `£`, `&` o `^`: il modulo SHALL dirlo, indicando
stelle e pollici; l'API SHALL rispondere `400` con l'errore sul campo; il database MUST rifiutarlo con un vincolo.

#### Scenario: Stelle e pollici dal nome
- **WHEN** la conversione trova l'azienda «£$$ Officina Rossi»
- **THEN** l'azienda si chiama «Officina Rossi», ha 2 stelle e un pollice

#### Scenario: Cinque simboli
- **WHEN** la conversione trova «$$$$$ Bar Centrale»
- **THEN** l'azienda si chiama «Bar Centrale» e ha 4 stelle

#### Scenario: Refusi
- **WHEN** la conversione trova «&Ferramenta Bianchi» e «£& Forno Verdi»
- **THEN** le aziende si chiamano «Ferramenta Bianchi», senza valutazioni, e «Forno Verdi», con un pollice

#### Scenario: & dentro il nome
- **WHEN** la conversione trova «Rossi & C. S.n.c.»
- **THEN** il nome non cambia e l'azienda resta senza valutazioni

#### Scenario: Nome fatto solo di simboli
- **WHEN** la conversione trova l'azienda importata n. 1234 chiamata «$$»
- **THEN** l'azienda si chiama «Azienda senza nome n. 1234», ha 2 stelle ed è tra le aziende da completare

#### Scenario: Traccia della conversione
- **WHEN** la conversione cambia un'azienda
- **THEN** l'audit dell'azienda registra il nome di prima e di dopo, e le stelle e i pollici ricevuti

#### Scenario: Simbolo scritto nel modulo
- **WHEN** l'operatore salva un'azienda chiamata «$$ Officina Rossi»
- **THEN** il modulo non la salva e dice che il nome non può iniziare con `$`, `£`, `&` o `^`, e che affidabilità e
  interesse si segnano con stelle e pollici

### Requirement: Zona dell'azienda
L'azienda SHALL avere una zona facoltativa, scelta dal catalogo delle zone (US-106) tra le voci attive, o quella che
l'azienda ha già anche se nel frattempo disattivata. La sceglie il Call Center o l'ADMIN dal modulo dell'azienda, e il
cambio SHALL entrare nell'audit con il nome della zona di prima e di dopo. L'elenco delle aziende del Call Center SHALL
mostrare la zona in una colonna e filtrare per zona; il filtro SHALL restare nell'indirizzo. Il database MUST
rifiutare una zona inesistente con un vincolo.

#### Scenario: Zona scelta
- **WHEN** l'operatore sceglie la zona «Rifredi» per un'azienda e salva
- **THEN** l'azienda ha la zona Rifredi e l'audit registra il cambio da nessuna a «Rifredi»

#### Scenario: Filtro per zona
- **WHEN** l'operatore sceglie la zona «Rifredi» nel filtro dell'elenco
- **THEN** vede, dalla prima pagina, solo le aziende di Rifredi

#### Scenario: Zona non valida
- **WHEN** una richiesta all'API salva un'azienda con una zona inesistente o disattivata che l'azienda non ha
- **THEN** la risposta è `400`, con l'errore sul campo della zona

### Requirement: Elenco delle aziende per il Tutor
Il Tutor SHALL avere nel menu la voce «Aziende», con l'elenco in sola lettura delle sole aziende attive, una pagina
alla volta come l'elenco del Call Center. Per ogni azienda SHALL vedere soltanto ragione sociale, settore, zona,
indirizzo con il comune (la sede operativa se indicata, altrimenti la legale), affidabilità e interesse al progetto.
SHALL poter cercare per ragione sociale e filtrare per settore e zona, con i filtri nell'indirizzo.

L'API per il Tutor MUST restituire soltanto quei campi: niente partita IVA, recapiti, referenti, note, mansioni o
storico. La ricerca MUST NOT guardare la partita IVA. Il Tutor MUST NOT creare, modificare, attivare o disattivare
aziende, né usare l'API completa delle aziende.

#### Scenario: Elenco del Tutor
- **WHEN** il Tutor apre «Aziende»
- **THEN** vede le aziende attive in ordine di ragione sociale, con settore, zona, indirizzo con il comune, stelle e
  pollici, e nessun altro dato

#### Scenario: Sede operativa
- **WHEN** un'azienda ha la sede legale a Firenze e la sede operativa a Prato
- **THEN** nell'elenco del Tutor l'indirizzo è quello di Prato

#### Scenario: Ricerca e filtri
- **WHEN** il Tutor cerca «officina» e sceglie la zona «Rifredi»
- **THEN** vede, dalla prima pagina, le aziende attive di Rifredi con «officina» nel nome, e l'indirizzo ricorda la
  ricerca e il filtro

#### Scenario: Partita IVA esclusa
- **WHEN** il Tutor cerca la partita IVA di un'azienda
- **THEN** non trova l'azienda, e nella risposta dell'API la partita IVA non c'è mai

#### Scenario: Aziende disattivate
- **WHEN** un'azienda è disattivata
- **THEN** non compare nell'elenco del Tutor

#### Scenario: Solo lettura
- **WHEN** un Tutor chiama l'API completa delle aziende, o prova a crearne o modificarne una
- **THEN** la risposta è `403`

### Requirement: Azzeramento dei filtri dell'elenco delle aziende
Negli elenchi delle aziende, del Call Center e del Tutor, quando è attiva almeno una ricerca o un filtro SHALL esserci
il pulsante «Azzera i filtri». Il pulsante SHALL togliere ricerca e filtri e riportare l'elenco alla prima pagina,
lasciando le aziende per pagina scelte; poi il focus SHALL andare al campo di ricerca. Senza filtri il pulsante MUST NOT
esserci.

#### Scenario: Azzeramento
- **WHEN** l'operatore ha una ricerca, il filtro «Almeno 2 stelle» e 50 aziende per pagina, e preme «Azzera i filtri»
- **THEN** vede la prima pagina di tutte le aziende attive, 50 per pagina, con il cursore nel campo di ricerca

#### Scenario: Nessun filtro
- **WHEN** l'elenco non ha ricerca né filtri
- **THEN** il pulsante «Azzera i filtri» non c'è

### Requirement: Valutazioni dei tirocini nella scheda dell'azienda
La scheda dell'azienda SHALL avere la sezione «Valutazioni dei tirocini». La sezione SHALL mostrare la media delle
valutazioni dei tirocini dell'azienda, cioè la media delle medie dei tre voti di ogni report, come faccina con il
numero, e quanti tirocini sono stati valutati. Sotto SHALL esserci l'elenco delle valutazioni, a pagine e dalla più
recente: data di fine del tirocinio, faccina della media, nota e nome del Tutor che l'ha data. Il nome del beneficiario
MUST NOT comparire. La vedono operatori del Call Center, ADMIN e Tutor. Senza valutazioni la sezione SHALL dirlo.

#### Scenario: Media delle medie
- **WHEN** un'azienda ha due tirocini valutati, con medie 4,3 e 5
- **THEN** la sezione mostra «Ottima, 4,7 su 5 · 2 tirocini» e le due valutazioni, dalla più recente

#### Scenario: Nome del Tutor
- **WHEN** l'operatore guarda una valutazione
- **THEN** vede la data di fine del tirocinio, la faccina, la nota e il nome del Tutor, e nessun dato del beneficiario

#### Scenario: Molte valutazioni
- **WHEN** un'azienda ha più di 10 valutazioni
- **THEN** l'elenco mostra le 10 più recenti, con i pulsanti per le pagine successive

#### Scenario: Nessuna valutazione
- **WHEN** l'azienda non ha tirocini valutati
- **THEN** la sezione dice «Nessun tirocinio valutato»

### Requirement: Scheda dell'azienda per il Tutor
Nella lista delle aziende del Tutor il nome di ogni azienda SHALL aprire una scheda in sola lettura. La scheda contiene
i dati della lista (categoria, zona, indirizzo, stelle e pollici) e la sezione «Valutazioni dei tirocini». La lista
SHALL avere anche una colonna con la faccina della media e il numero di tirocini valutati. L'API della scheda del
Tutor MUST restituire solo i dati della lista, e solo per le aziende attive (altrimenti `404`).

#### Scenario: Scheda del Tutor
- **WHEN** il Tutor apre un'azienda dalla sua lista
- **THEN** vede categoria, zona, indirizzo, stelle, pollici e le valutazioni, senza poter modificare nulla

#### Scenario: Azienda disattivata
- **WHEN** il Tutor apre l'indirizzo della scheda di un'azienda disattivata
- **THEN** vede «Azienda non trovata»

