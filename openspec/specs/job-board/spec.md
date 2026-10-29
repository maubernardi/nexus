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

### Requirement: Candidatura a un annuncio
Il Tutor SHALL poter candidare dalla bacheca un suo beneficiario senza segnalazioni aperte a un annuncio pubblicato,
aperto a tutti o riservato a un suo progetto. Nasce una segnalazione speciale con la corsia veloce, in lavorazione,
collegata all'annuncio e con la tipologia della mansione. Il progetto è quello dell'annuncio se riservato, altrimenti
uno dei progetti del Tutor. Un annuncio MUST NOT avere più di una candidatura in valutazione: la prima lo blocca finché
il Call Center non decide, e in bacheca l'annuncio mostra «Candidatura in valutazione», senza il comando.

#### Scenario: Candidatura
- **WHEN** il Tutor sceglie "Candida un beneficiario" su un annuncio libero e invia la candidatura
- **THEN** nasce la segnalazione speciale n. N, in cima alla coda del Call Center con la corsia veloce, e la cronologia
  riporta "Candidatura all'annuncio #M della bacheca"

#### Scenario: Annuncio bloccato
- **WHEN** un altro beneficiario è già candidato all'annuncio
- **THEN** in bacheca l'annuncio mostra «Candidatura in valutazione» e una nuova candidatura è rifiutata con il motivo

#### Scenario: Annuncio non visibile
- **WHEN** l'annuncio è una bozza o è riservato a un progetto del quale il Tutor non fa parte
- **THEN** per il Tutor l'annuncio non esiste

#### Scenario: Gestione
- **WHEN** l'operatore apre un annuncio bloccato
- **THEN** vede quale segnalazione lo blocca, con il link

### Requirement: Approvazione della corsia veloce
Il Call Center e l'ADMIN SHALL poter approvare la corsia veloce di una segnalazione speciale presa in carico. In
un'unica operazione la segnalazione passa a `PROPOSTA_AZIENDA` con la mansione dell'annuncio, la mansione si blocca,
l'annuncio si archivia e il Tutor riceve la notifica della proposta. La corsia veloce è consumata, il tipo SPECIAL
resta. Finché la corsia veloce aspetta la decisione, la segnalazione MUST NOT essere abbinata a un'altra mansione.

#### Scenario: Approvazione
- **WHEN** l'operatore che ha preso in carico la segnalazione speciale approva la corsia veloce e conferma
- **THEN** la segnalazione è una proposta all'azienda dell'annuncio, la mansione è bloccata, l'annuncio è archiviato
  e il Tutor riceve la notifica

#### Scenario: Mansione non più disponibile
- **WHEN** la mansione dell'annuncio è stata ritirata o bloccata, oppure l'annuncio non è più in bacheca
- **THEN** l'approvazione è rifiutata con il motivo e nulla cambia

#### Scenario: Abbinamento con la corsia veloce in attesa
- **WHEN** l'operatore prova ad abbinare un'altra mansione prima di decidere sulla corsia veloce
- **THEN** l'abbinamento è rifiutato: prima si decide sulla corsia veloce

### Requirement: Rifiuto della corsia veloce
Il Call Center e l'ADMIN SHALL poter rifiutare, con un motivo obbligatorio, la corsia veloce di una segnalazione
speciale presa in carico. La segnalazione resta SPECIAL e in lavorazione, senza corsia veloce, e si lavora come le
altre; l'annuncio resta pubblicato e torna candidabile; il Tutor riceve la notifica e il motivo è in cronologia.

#### Scenario: Rifiuto
- **WHEN** l'operatore rifiuta la corsia veloce scrivendo il motivo
- **THEN** la segnalazione continua in lavorazione normale, l'annuncio #N torna candidabile e il Tutor è avvisato

#### Scenario: Motivo mancante
- **WHEN** l'operatore conferma il rifiuto senza motivo
- **THEN** il motivo è segnalato come obbligatorio e nulla cambia

### Requirement: Corsia veloce che decade con l'annuncio
Quando un annuncio con una candidatura in attesa viene archiviato, la segnalazione SHALL perdere la corsia veloce
nella stessa operazione, con il motivo in cronologia e la notifica al Tutor. Vale sia per l'archiviazione a mano, sia
per quella automatica (mansione abbinata o ritirata, azienda disattivata). La conferma dell'archiviazione SHALL
avvisare l'operatore.

#### Scenario: Archiviazione a mano
- **WHEN** l'operatore archivia un annuncio con una candidatura in attesa
- **THEN** la conferma lo avvisa, e dopo la segnalazione è in lavorazione normale con il motivo "Annuncio #N archiviato
  dal Call Center"

#### Scenario: Mansione abbinata a un'altra segnalazione
- **WHEN** la mansione dell'annuncio viene abbinata a un'altra segnalazione
- **THEN** la candidatura in attesa perde la corsia veloce con il motivo

