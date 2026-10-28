# job-board Specification

## Purpose
Bacheca delle opportunità: il Call Center crea, pubblica e archivia gli annunci delle mansioni libere; i Tutor li
consultano senza mai vedere l'azienda. Un annuncio vive solo finché la sua mansione è disponibile.
## Requirements
### Requirement: Gestione degli annunci
Solo il Call Center e l'ADMIN SHALL vedere, modificare, pubblicare e archiviare gli annunci della bacheca, divisi in
"Bozze", "Pubblicati" e "Archiviati". Ogni azione SHALL essere registrata nell'audit con autore, istante e valori prima
e dopo.

#### Scenario: Elenco per stato
- **WHEN** un operatore apre la bacheca
- **THEN** vede le bozze con numero (#N), titolo, azienda, mansione, zona e progetto, e può passare ai pubblicati e
  agli archiviati

#### Scenario: Ruolo non abilitato
- **WHEN** un Tutor chiede la gestione degli annunci all'API
- **THEN** la risposta è `403`

### Requirement: Modifica di un annuncio
L'operatore SHALL poter modificare titolo, fascia d'età, ore settimanali, durata e note di una bozza o di un annuncio
pubblicato. Il progetto riservatario SHALL essere modificabile solo nella bozza. Un annuncio archiviato MUST NOT essere
modificato. Le modifiche MUST usare il blocco ottimistico.

#### Scenario: Modifica di una bozza
- **WHEN** l'operatore cambia titolo e progetto di una bozza
- **THEN** l'annuncio è salvato e l'audit registra i campi cambiati

#### Scenario: Progetto di un annuncio pubblicato
- **WHEN** l'operatore prova a cambiare il progetto di un annuncio pubblicato
- **THEN** la risposta è `409`: si rende pubblico solo con lo sblocco dopo 7 giorni

#### Scenario: Fascia d'età incoerente
- **WHEN** l'età minima è maggiore della massima
- **THEN** il modulo segnala l'errore sul campo dell'età massima

### Requirement: Pubblicazione e archiviazione
L'operatore SHALL poter pubblicare una bozza, registrandone la data, solo se la mansione è libera e a disposizione e
l'azienda è attiva. L'operatore SHALL poter archiviare una bozza o un annuncio pubblicato.

#### Scenario: Pubblicazione
- **WHEN** l'operatore pubblica una bozza e conferma
- **THEN** l'annuncio passa tra i pubblicati con la data di pubblicazione

#### Scenario: Mansione non proponibile
- **WHEN** la mansione dell'annuncio è bloccata da una segnalazione
- **THEN** la pubblicazione è rifiutata con il motivo

#### Scenario: Archiviazione
- **WHEN** l'operatore archivia un annuncio e conferma
- **THEN** l'annuncio passa tra gli archiviati, in sola lettura

### Requirement: Annuncio da una mansione libera
Il Call Center e l'ADMIN SHALL poter creare dalla scheda dell'azienda una bozza di annuncio per una mansione libera e a
disposizione di un'azienda attiva. La bozza nasce con il titolo della mansione e visibile a tutti i progetti. Una
mansione MUST NOT avere più di un annuncio attivo (bozza o pubblicato). La creazione SHALL essere registrata
nell'audit.

#### Scenario: Creazione
- **WHEN** l'operatore sceglie "Crea annuncio" accanto a una mansione libera
- **THEN** nasce la bozza #N e si apre la sua scheda con l'esito; accanto alla mansione compare "Annuncio #N"

#### Scenario: Annuncio già attivo
- **WHEN** la mansione ha già una bozza o un annuncio pubblicato
- **THEN** la creazione è rifiutata con il numero dell'annuncio esistente

#### Scenario: Mansione non proponibile
- **WHEN** la mansione è bloccata da una segnalazione o ritirata
- **THEN** "Crea annuncio" non compare e l'API rifiuta la creazione con il motivo

### Requirement: Bacheca del Tutor
Il Tutor SHALL vedere in bacheca gli annunci pubblicati aperti a tutti e quelli riservati ai suoi progetti, dal più
recente, filtrabili per zona e tipologia e consultabili da smartphone. La risposta MUST NOT contenere l'azienda (né
nome né identificativo), la mansione, il titolo o le note dell'annuncio. Un annuncio riservato SHALL indicare in testo
il progetto del Tutor a cui è riservato.

#### Scenario: Annunci visibili
- **WHEN** un Tutor del progetto GOL apre la bacheca
- **THEN** vede gli annunci pubblicati per tutti e quelli riservati a GOL, non quelli di altri progetti, le bozze o
  gli archiviati

#### Scenario: Nessun dato dell'azienda
- **WHEN** titolo e note di un annuncio citano l'azienda
- **THEN** la risposta al Tutor non contiene né il nome né l'identificativo dell'azienda

#### Scenario: Filtri
- **WHEN** il Tutor sceglie una zona
- **THEN** vede solo gli annunci di quella zona, e il filtro resta nell'indirizzo

### Requirement: Annuncio legato alla disponibilità della mansione
Quando una mansione smette di essere disponibile il suo annuncio attivo (bozza o pubblicato) SHALL essere archiviato
nella stessa operazione, con il motivo nell'audit. Succede in tre casi: la mansione è abbinata a una segnalazione,
la mansione è ritirata, l'azienda è disattivata.

#### Scenario: Abbinamento
- **WHEN** l'operatore abbina a una segnalazione una mansione con un annuncio pubblicato
- **THEN** l'annuncio è archiviato con il motivo "Mansione abbinata alla segnalazione n. N"

#### Scenario: Ritiro della mansione
- **WHEN** l'operatore ritira una mansione con un annuncio
- **THEN** l'annuncio è archiviato e l'esito lo dice all'operatore

### Requirement: Rilancio in bacheca dopo un appuntamento fallito
Registrando un appuntamento fallito, il Tutor della segnalazione SHALL poter chiedere il rilancio della mansione in
bacheca. Il sistema SHALL creare nella stessa operazione una bozza di annuncio con la mansione appena liberata,
riservata al progetto della segnalazione e collegata a essa, e SHALL notificare l'operatore che ha in carico la
segnalazione. Pubblicarla resta una scelta del Call Center. La bozza MUST NOT essere visibile ai Tutor finché non è
pubblicata.

#### Scenario: Rilancio chiesto
- **WHEN** il Tutor registra l'appuntamento fallito spuntando "Chiedi il rilancio della mansione in bacheca"
- **THEN** nasce la bozza #N riservata al progetto della segnalazione, l'operatore riceve la notifica e la
  cronologia riporta "rilancio in bacheca richiesto"

#### Scenario: Link alla bozza
- **WHEN** l'operatore apre la segnalazione
- **THEN** la sezione "Rilancio in bacheca" porta alla scheda dell'annuncio #N; il Tutor vede solo numero e stato

#### Scenario: Senza rilancio
- **WHEN** il Tutor registra l'appuntamento fallito senza spuntare la casella
- **THEN** non nasce nessun annuncio

### Requirement: Sblocco a tutti dopo 7 giorni
Il Call Center e l'ADMIN SHALL poter rendere visibile ai Tutor di tutti i progetti un annuncio pubblicato e riservato a
un progetto, solo a partire da 7 giorni solari dopo la pubblicazione. Lo sblocco SHALL togliere il progetto,
registrare la data e comparire nell'audit. Prima di quella data il comando MUST NOT essere disponibile e l'API SHALL
rispondere `409` indicando da quando si potrà fare.

#### Scenario: Troppo presto
- **WHEN** l'annuncio riservato è stato pubblicato da 2 giorni
- **THEN** la scheda dice da quando si potrà rendere visibile a tutti e il comando non c'è

#### Scenario: Sblocco
- **WHEN** sono passati 7 giorni dalla pubblicazione e l'operatore conferma "Rendi visibile a tutti"
- **THEN** l'annuncio non è più riservato, la scheda riporta "Visibile a tutti dal …" e lo vedono i Tutor di ogni
  progetto

#### Scenario: Già per tutti o non pubblicato
- **WHEN** l'annuncio è già visibile a tutti oppure è una bozza
- **THEN** lo sblocco è rifiutato con il motivo

