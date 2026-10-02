# cc-queue Specification

## Purpose
Coda di lavoro del Call Center: le segnalazioni da lavorare, in ordine equo, con priorità alle speciali.

## Requirements

### Requirement: Contenuto e ordine della coda
Il sistema SHALL mostrare al Call Center e all'ADMIN i ticket in stato `NUOVA` o `IN_LAVORAZIONE` non assegnati a un
operatore, ordinati mettendo prima i fast-track e poi dal più vecchio al più recente. Il Tutor SHALL NOT accedere alla
coda.

#### Scenario: Nuova segnalazione in coda
- **WHEN** un Tutor invia una segnalazione
- **THEN** il ticket compare nella coda del Call Center dopo i fast-track e dopo i ticket arrivati prima

#### Scenario: Fast-track in cima
- **WHEN** la coda contiene un ticket fast-track arrivato dopo alcuni ticket normali
- **THEN** il fast-track è il primo e ha un badge con il testo "Fast-track"

#### Scenario: Ticket assegnati o avanzati
- **WHEN** un ticket è assegnato a un operatore o è in uno stato successivo
- **THEN** non compare nella coda

#### Scenario: Accesso del Tutor
- **WHEN** un Tutor richiede la coda
- **THEN** la risposta è `403` e l'interfaccia mostra "Accesso non consentito"

### Requirement: Filtri della coda
La coda SHALL essere filtrabile per progetto e per zona di residenza del beneficiario, tra i progetti e le zone attivi. I
filtri SHALL essere riportati nell'indirizzo della pagina e il numero di risultati SHALL essere annunciato alle
tecnologie assistive.

#### Scenario: Filtro per progetto
- **WHEN** l'operatore sceglie un progetto
- **THEN** la coda mostra solo i ticket di quel progetto, l'indirizzo contiene il filtro e viene annunciato il numero di
  segnalazioni

#### Scenario: Filtro non valido
- **WHEN** l'API riceve un identificativo di progetto o di zona non valido
- **THEN** la risposta è `400`

### Requirement: Vista accessibile
La coda SHALL essere una tabella dati accessibile (didascalia, intestazioni di colonna e di riga), ottimizzata per
desktop e utilizzabile a 320 px senza scorrimento orizzontale della pagina; SHALL indicare quando la coda è vuota.

#### Scenario: Coda vuota
- **WHEN** non ci sono ticket da lavorare con i filtri scelti
- **THEN** la pagina mostra "Nessuna segnalazione in coda" al posto della tabella

### Requirement: Presa in carico
Il sistema SHALL permettere a Call Center e ADMIN di prendere in carico una segnalazione della coda indicando la versione
vista: il ticket passa in `IN_LAVORAZIONE`, è assegnato all'operatore, esce dalla coda e l'azione è registrata nella
cronologia e nell'audit. Se la segnalazione è già assegnata o è cambiata nel frattempo, il sistema SHALL rispondere
`409` senza modificarla.

#### Scenario: Presa in carico riuscita
- **WHEN** un operatore prende in carico una segnalazione `NUOVA`
- **THEN** il ticket è `IN_LAVORAZIONE`, assegnato a lui, non compare più nella coda e l'audit registra `TAKE_CHARGE`
  con stato prima/dopo e operatore assegnato

#### Scenario: Collega più veloce
- **WHEN** due operatori prendono in carico la stessa segnalazione partendo dalla stessa versione
- **THEN** il primo riesce e il secondo riceve `409` e vede un messaggio che lo spiega, con la coda aggiornata

#### Scenario: Ruolo non autorizzato
- **WHEN** un Tutor tenta la presa in carico
- **THEN** la risposta è `403`

### Requirement: Le mie lavorazioni
Il sistema SHALL mostrare all'operatore le segnalazioni aperte assegnate a lui, dalla più vecchia, in una tabella
accessibile come quella della coda.

#### Scenario: Dopo la presa in carico
- **WHEN** l'operatore ha preso in carico una segnalazione
- **THEN** la trova in "Le mie lavorazioni", e un altro operatore non la vede tra le proprie
