## ADDED Requirements

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
