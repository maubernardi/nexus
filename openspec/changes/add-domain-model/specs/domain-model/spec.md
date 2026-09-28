## Purpose

Definisce le entità del dominio NEXUS (progetti, utenti, candidati, segnalazioni, aziende, mansioni, bacheca), i loro
identificativi e i vincoli di integrità che il sistema garantisce indipendentemente dalla logica applicativa.

## ADDED Requirements

### Requirement: Identificativi delle entità
Ogni entità SHALL avere un identificativo tecnico TSID (intero a 64 bit ordinato nel tempo). Verso l'esterno
l'identificativo MUST essere rappresentato come stringa di 13 caratteri, mai come numero. Ticket e BoardPost SHALL
avere inoltre un numero progressivo leggibile, unico per tipo di entità e assegnato dal sistema alla creazione.

#### Scenario: Numerazione progressiva dei ticket
- **WHEN** vengono creati due ticket in sequenza
- **THEN** il secondo riceve un numero progressivo maggiore del primo e i due numeri sono diversi

#### Scenario: Numerazione indipendente della bacheca
- **WHEN** viene creato un post di bacheca
- **THEN** riceve un numero progressivo della sequenza dei post, indipendente da quella dei ticket

### Requirement: Tracciabilità delle modifiche
Ogni record SHALL registrare data e autore della creazione e dell'ultima modifica, valorizzati automaticamente con
l'utente autenticato o con un identificativo di sistema per le operazioni automatiche. Le date MUST essere in UTC.

#### Scenario: Creazione da parte di un utente
- **WHEN** un utente autenticato crea un record
- **THEN** creazione e ultima modifica riportano quell'utente e l'istante corrente

### Requirement: Modifiche concorrenti
Ticket, JobSlot e BoardPost SHALL essere protetti dalle modifiche concorrenti: se due modifiche partono dalla stessa
versione di un record, la seconda MUST essere rifiutata invece di sovrascrivere la prima.

#### Scenario: Due operatori sulla stessa mansione
- **WHEN** due operatori leggono la stessa mansione e la modificano uno dopo l'altro
- **THEN** la modifica del secondo operatore viene rifiutata per conflitto di versione

### Requirement: Utenti e progetti
Ogni utente SHALL avere un identificativo esterno unico (il soggetto dell'identity provider), username ed email unici,
un solo ruolo tra `TUTOR`, `CALL_CENTER` e `ADMIN` e uno stato attivo/disattivo. Gli utenti SHALL poter essere assegnati a
più progetti. Ogni progetto SHALL avere un codice unico. Utenti, progetti e aziende MUST essere disattivati, mai cancellati,
quando esistono dati collegati.

#### Scenario: Codice progetto duplicato
- **WHEN** si tenta di creare un progetto con un codice già esistente
- **THEN** l'operazione viene rifiutata

#### Scenario: Cancellazione di un tutor con ticket
- **WHEN** si tenta di cancellare un utente che è tutor di almeno un ticket
- **THEN** l'operazione viene rifiutata e l'utente può solo essere disattivato

### Requirement: Dati del candidato
Ogni candidato SHALL avere un tutor proprietario. Genere, nazionalità e cittadinanza (codici ISO 3166-1 alpha-2), titolo
di studio, mezzo di trasporto, tipi di patente e livello delle lingue (A1–C2 o madrelingua) MUST assumere solo valori
ammessi. Il possesso della patente MUST essere coerente con la presenza di almeno un tipo di patente. Ogni lingua
compare al massimo una volta per candidato. Il candidato SHALL poter riferire un file di CV e registrare la data di
un'eventuale anonimizzazione.

#### Scenario: Patente incoerente
- **WHEN** si salva un candidato che dichiara la patente senza indicarne alcun tipo
- **THEN** l'operazione viene rifiutata

#### Scenario: Livello linguistico non valido
- **WHEN** si registra una lingua con livello "B3"
- **THEN** l'operazione viene rifiutata

### Requirement: Segnalazione (Ticket)
Ogni ticket SHALL riferire tutor, progetto e candidato, avere tipo `NORMAL` o `SPECIAL` e uno stato tra `NUOVA`,
`IN_ATTESA_APPROVAZIONE_ADMIN`, `IN_LAVORAZIONE`, `PROPOSTA_AZIENDA`, `PROPOSTA_ACCOLTA`, `APPUNTAMENTO`, `IN_TIROCINIO`,
`FORM_RESTITUZIONE`, `RIAPERTO`. La mansione richiesta MUST essere indicata da una tipologia del catalogo oppure da un
testo libero, mai entrambi e mai nessuno dei due. Un ticket `SPECIAL` MUST riferire il post di bacheca da cui è nato;
un ticket fast-track MUST essere di tipo `SPECIAL`. Il timer SHALL registrare avvio, invio del promemoria e scadenza;
scadenza e avvio MUST essere presenti insieme. Azienda e zona della proposta si ricavano dalla mansione abbinata.

#### Scenario: Mansione richiesta doppia
- **WHEN** si salva un ticket con sia una tipologia del catalogo sia un testo libero
- **THEN** l'operazione viene rifiutata

#### Scenario: Segnalazione speciale senza post
- **WHEN** si salva un ticket `SPECIAL` senza riferimento a un post di bacheca
- **THEN** l'operazione viene rifiutata

#### Scenario: Stato non previsto
- **WHEN** si salva un ticket con uno stato non elencato
- **THEN** l'operazione viene rifiutata

### Requirement: Storico e blacklist del ticket
Il sistema SHALL conservare lo storico dei cambi di stato di ogni ticket (stato di partenza, stato di arrivo, istante,
autore, nota) e l'elenco delle aziende escluse per quel ticket, ciascuna al massimo una volta, con data e motivo.

#### Scenario: Azienda esclusa due volte
- **WHEN** si esclude per lo stesso ticket un'azienda già esclusa
- **THEN** l'operazione viene rifiutata

### Requirement: Mansione e blocco
Ogni mansione SHALL appartenere a un'azienda, a una tipologia del catalogo e a una zona, con stato `LIBERA` o `BLOCCATA`.
Una mansione MUST essere `BLOCCATA` se e solo se riferisce il ticket che la blocca. Un ticket MUST bloccare al massimo
una mansione.

#### Scenario: Mansione bloccata senza ticket
- **WHEN** si imposta una mansione a `BLOCCATA` senza indicare il ticket
- **THEN** l'operazione viene rifiutata

#### Scenario: Stesso ticket su due mansioni
- **WHEN** si tenta di far bloccare a un ticket una seconda mansione
- **THEN** l'operazione viene rifiutata

### Requirement: Post di bacheca
Ogni post SHALL riferire una mansione (azienda e zona si ricavano da essa), avere stato `DRAFT`, `PUBLISHED` o `ARCHIVED`
e poter essere riservato a un progetto o pubblico. Un post `PUBLISHED` MUST avere la data di pubblicazione. Per ogni
mansione MUST esistere al massimo un post in stato `DRAFT` o `PUBLISHED`. I requisiti di età, se presenti, esprimono
vincoli del programma di finanziamento e MUST rispettare `age_min <= age_max`; ore settimanali e durata in mesi MUST
essere positive. Il sistema SHALL registrare quando un post riservato è stato reso pubblico e il ticket da cui è nato.

#### Scenario: Secondo post attivo sulla stessa mansione
- **WHEN** si crea un post in bozza per una mansione che ha già un post pubblicato
- **THEN** l'operazione viene rifiutata

#### Scenario: Pubblicato senza data
- **WHEN** si salva un post `PUBLISHED` senza data di pubblicazione
- **THEN** l'operazione viene rifiutata

#### Scenario: Post archiviato e nuovo post
- **WHEN** il post di una mansione è `ARCHIVED` e si crea un nuovo post per la stessa mansione
- **THEN** l'operazione è consentita

### Requirement: Tabelle di riferimento
Zone e tipologie di mansione SHALL essere tabelle con codice unico, nome e stato attivo/disattivo, condivise da
candidati, mansioni e segnalazioni.

#### Scenario: Zona duplicata
- **WHEN** si crea una zona con un codice già esistente
- **THEN** l'operazione viene rifiutata
