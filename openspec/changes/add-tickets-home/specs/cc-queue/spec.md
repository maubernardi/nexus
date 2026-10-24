## MODIFIED Requirements

### Requirement: Contenuto e ordine della coda
Il sistema SHALL mostrare al Call Center e all'ADMIN, nella vista "Da prendere in carico" della Home (predefinita per
questi ruoli), i ticket in stato `NUOVA` o `IN_LAVORAZIONE` non assegnati a un operatore, ordinati mettendo prima i
fast-track e poi dal più vecchio al più recente. Il Tutor SHALL NOT accedere a questa vista.

#### Scenario: Nuova segnalazione in coda
- **WHEN** un Tutor invia una segnalazione
- **THEN** il ticket compare in "Da prendere in carico" dopo i fast-track e dopo i ticket arrivati prima

#### Scenario: Fast-track in cima
- **WHEN** la coda contiene un ticket fast-track arrivato dopo alcuni ticket normali
- **THEN** il fast-track è il primo e ha un badge con il testo "Fast-track"

#### Scenario: Ticket assegnati o avanzati
- **WHEN** un ticket è assegnato a un operatore o è in uno stato successivo
- **THEN** non compare in "Da prendere in carico"

#### Scenario: Accesso del Tutor
- **WHEN** un Tutor chiede all'API la vista "Da prendere in carico"
- **THEN** la risposta è `400` e l'interfaccia non gli mostra questa vista

### Requirement: Filtri della coda
La coda e le altre viste della Home SHALL essere filtrabili per stato e per progetto, senza filtro per zona (nota del
PO del 24/10). I filtri SHALL essere riportati nell'indirizzo della pagina e il numero di risultati SHALL essere
annunciato alle tecnologie assistive.

#### Scenario: Filtro per progetto
- **WHEN** l'operatore sceglie un progetto
- **THEN** l'elenco mostra solo i ticket di quel progetto, l'indirizzo contiene il filtro e viene annunciato il numero
  di segnalazioni

#### Scenario: Filtro per stato
- **WHEN** l'operatore sceglie uno stato
- **THEN** l'elenco mostra solo i ticket in quello stato

#### Scenario: Filtro non valido
- **WHEN** l'API riceve un identificativo di progetto non valido
- **THEN** la risposta è `400`

### Requirement: Vista accessibile
L'elenco SHALL essere una tabella dati accessibile (didascalia, intestazioni di colonna e di riga), ottimizzata per
desktop e utilizzabile a 320 px senza scorrimento orizzontale della pagina; SHALL indicare quando la vista è vuota.

#### Scenario: Coda vuota
- **WHEN** non ci sono ticket da prendere in carico con i filtri scelti
- **THEN** la pagina lo dice al posto della tabella

### Requirement: Le mie lavorazioni
Il sistema SHALL mostrare all'operatore, nella vista "In carico a me" della Home, le segnalazioni aperte assegnate a lui,
dalla più vecchia, in una tabella accessibile come quella della coda.

#### Scenario: Dopo la presa in carico
- **WHEN** l'operatore ha preso in carico una segnalazione
- **THEN** la trova in "In carico a me", e un altro operatore non la vede tra le proprie
