# ticket-lifecycle Specification

## Purpose
Regole comuni a tutti i cambi di stato del ticket: solo le transizioni ammesse per stato e ruolo, cronologia completa,
nessuna sovrascrittura silenziosa tra utenti concorrenti.
## Requirements
### Requirement: Transizioni ammesse
Il sistema SHALL applicare a un ticket solo le transizioni dichiarate nel catalogo per il suo stato corrente e per il
ruolo dell'utente. Una transizione non ammessa dallo stato corrente SHALL essere rifiutata con `409` senza modificare il
ticket; una transizione non consentita al ruolo SHALL essere rifiutata con `403`.

#### Scenario: Transizione non ammessa
- **WHEN** si richiede una transizione il cui stato di partenza non è quello corrente del ticket
- **THEN** la risposta è `409`, lo stato del ticket non cambia e non viene scritta cronologia

#### Scenario: Ruolo non autorizzato
- **WHEN** un utente richiede una transizione non consentita al suo ruolo
- **THEN** la risposta è `403` e il ticket non cambia

### Requirement: Cronologia degli stati
Il sistema SHALL registrare in `ticket_status_history` ogni cambio di stato riuscito, compresa la creazione, con stato di
partenza (nullo alla creazione), stato di arrivo, autore, istante e nota facoltativa.

#### Scenario: Transizione riuscita
- **WHEN** una transizione ammessa viene applicata
- **THEN** il ticket assume il nuovo stato e la cronologia ha una nuova riga con autore e nota

### Requirement: Modifiche concorrenti
Il sistema SHALL rifiutare con `409` una transizione basata su una versione del ticket non più attuale, sia quando la
versione attesa inviata dal client è superata, sia quando due transazioni modificano lo stesso ticket in parallelo.

#### Scenario: Due operatori sullo stesso ticket
- **WHEN** due richieste partono dalla stessa versione del ticket e la prima va a buon fine
- **THEN** la seconda riceve `409`, lo stato resta quello impostato dalla prima e la cronologia ha una sola riga nuova

### Requirement: Accettazione della proposta
Il Tutor della segnalazione SHALL poter accettare la proposta di una segnalazione in `PROPOSTA_AZIENDA`, previa
conferma: la segnalazione passa in `PROPOSTA_ACCOLTA` e parte un timer che scade alla fine del 7° giorno lavorativo
successivo, secondo il calendario unico. La scadenza SHALL essere visibile nel dettaglio a chi vede la segnalazione.
L'accettazione SHALL essere registrata nell'audit e nello storico dell'azienda. Altri utenti MUST NOT poter accettare.

#### Scenario: Accettazione
- **WHEN** il Tutor della segnalazione accetta la proposta martedì 27 ottobre 2026
- **THEN** lo stato è "Proposta accolta", la scadenza del timer è giovedì 5 novembre 2026, la cronologia mostra
  "Proposta accettata" con la scadenza e lo storico dell'azienda conta una proposta accettata

#### Scenario: Collega di progetto o Call Center
- **WHEN** un collega di progetto o un operatore apre la segnalazione
- **THEN** non trova il comando di accettazione e l'API risponde `403`

#### Scenario: Accettazione già avvenuta o versione superata
- **WHEN** il Tutor accetta una proposta già accettata o partendo da una versione superata
- **THEN** il sistema risponde `409`

### Requirement: Appuntamento fissato
Il Tutor della segnalazione SHALL poter registrare, previa conferma e con una nota facoltativa, che l'appuntamento con
l'azienda è fissato, per una segnalazione in `PROPOSTA_ACCOLTA`: la segnalazione passa in `APPUNTAMENTO` e il timer si
ferma. L'evento SHALL essere registrato nell'audit (con i valori del timer), nella cronologia e nello storico
dell'azienda, e notificato all'operatore che ha in carico la segnalazione. Altri utenti MUST NOT poterlo registrare.

#### Scenario: Appuntamento registrato
- **WHEN** il Tutor registra l'appuntamento con la nota "Giovedì alle 10"
- **THEN** lo stato è "Appuntamento", la scadenza del timer non c'è più, la cronologia mostra "Appuntamento fissato" con
  la nota e l'operatore riceve la notifica

#### Scenario: Prima dell'accettazione
- **WHEN** il Tutor prova a registrare l'appuntamento su una proposta non ancora accettata
- **THEN** il sistema risponde `409`

#### Scenario: Collega o Call Center
- **WHEN** un collega di progetto o un operatore prova a registrare l'appuntamento
- **THEN** la risposta è `403`

### Requirement: Appuntamento non andato a buon fine
Il Tutor della segnalazione SHALL poter registrare, per una segnalazione in `APPUNTAMENTO`, che l'appuntamento non è
andato a buon fine, indicando una causa (dall'elenco) e una nota, obbligatoria con la causa "Altro". La segnalazione
passa in `RIAPERTO`, resta in carico allo stesso operatore e la mansione torna libera; l'azienda MUST NOT essere esclusa
automaticamente. L'evento SHALL essere registrato nell'audit, in cronologia (con causa e nota) e nello storico
dell'azienda, e notificato all'operatore. Una segnalazione riaperta SHALL poter essere abbinata di nuovo.

#### Scenario: Appuntamento fallito
- **WHEN** il Tutor registra la causa "L'azienda non ha confermato il tirocinio"
- **THEN** la segnalazione è "Riaperto", la mansione è libera, la cronologia mostra la causa, lo storico dell'azienda
  conta un appuntamento fallito e l'operatore riceve la notifica

#### Scenario: Causa "Altro" senza nota
- **WHEN** il Tutor sceglie "Altro" senza scrivere la nota
- **THEN** il sistema chiede la nota

#### Scenario: Nuova proposta
- **WHEN** l'operatore apre la segnalazione riaperta
- **THEN** trova le mansioni compatibili e può proporne una, anche della stessa azienda

#### Scenario: Altri utenti o stato diverso
- **WHEN** un operatore prova a registrarlo, o il Tutor lo fa prima dell'appuntamento
- **THEN** la risposta è rispettivamente `403` e `409`

### Requirement: Rifiuto della proposta
Il Tutor della segnalazione SHALL poter rifiutare, con motivo obbligatorio, una proposta non ancora accettata
(`PROPOSTA_AZIENDA`): la segnalazione passa in `RIAPERTO` in carico allo stesso operatore, la mansione torna libera e
l'azienda è esclusa per quella segnalazione, così da non comparire più tra le mansioni compatibili. Il rifiuto SHALL
essere registrato nell'audit, in cronologia (con il motivo) e nello storico dell'azienda, e notificato all'operatore.

#### Scenario: Rifiuto
- **WHEN** il Tutor rifiuta la proposta di Alfa Logistica con il motivo "Orari incompatibili"
- **THEN** la segnalazione è "Riaperto", la mansione è libera, Alfa Logistica non compare tra le compatibili della
  segnalazione e l'operatore riceve la notifica

#### Scenario: Motivo mancante
- **WHEN** il Tutor prova a rifiutare senza motivo
- **THEN** il sistema chiede il motivo

#### Scenario: Dopo l'accettazione o da altri utenti
- **WHEN** il Tutor prova a rifiutare una proposta già accettata, o la rifiuta un collega o un operatore
- **THEN** la risposta è rispettivamente `409` e `403`

### Requirement: Promemoria del timer
Il sistema SHALL controllare ogni ora le proposte accettate con il timer attivo e, a partire dal 4° giorno lavorativo
dopo l'accettazione (calendario unico), SHALL inviare al Tutor della segnalazione un promemoria come notifica in app.
Il promemoria MUST essere inviato una sola volta per timer, anche se il controllo gira più volte, e SHALL comparire in
cronologia come azione del sistema. Le segnalazioni con il timer fermo (es. appuntamento fissato) MUST NOT ricevere il
promemoria.

#### Scenario: Promemoria al 4° giorno lavorativo
- **WHEN** la proposta è stata accettata lunedì 2 novembre 2026 e il controllo gira venerdì 6 novembre
- **THEN** il Tutor riceve il promemoria; giovedì 5 novembre non l'avrebbe ricevuto

#### Scenario: Una sola volta
- **WHEN** il controllo gira di nuovo dopo l'invio
- **THEN** il promemoria non viene ripetuto

#### Scenario: Timer fermo
- **WHEN** l'appuntamento è già stato fissato
- **THEN** nessun promemoria

### Requirement: Scadenza del timer
Il sistema SHALL controllare ogni ora le proposte accettate e, raggiunta la scadenza del timer (fine del 7° giorno
lavorativo, ora di Roma), SHALL riportare la segnalazione in `RIAPERTO` in carico allo stesso operatore, azzerare il
timer e liberare la mansione, registrando l'evento nell'audit e nello storico degli stati come azione del sistema e
notificandolo al Tutor e all'operatore. La scadenza MUST essere applicata solo dal sistema, mai su richiesta di un
utente, e MUST NOT essere seguita dal promemoria.

#### Scenario: Scadenza
- **WHEN** il controllo gira all'istante della scadenza di una proposta accettata senza appuntamento
- **THEN** la segnalazione è "Riaperto", la mansione è libera, in cronologia c'è "Scadenza del timer" con autore
  "Sistema", e Tutor e operatore ricevono la notifica

#### Scenario: Prima della scadenza
- **WHEN** il controllo gira un secondo prima della scadenza
- **THEN** non succede nulla

#### Scenario: Nessun utente
- **WHEN** un utente, anche ADMIN, prova ad applicare la scadenza
- **THEN** l'operazione è negata

