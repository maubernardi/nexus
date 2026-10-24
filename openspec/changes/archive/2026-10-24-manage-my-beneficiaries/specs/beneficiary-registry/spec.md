## MODIFIED Requirements

### Requirement: Consultazione dei beneficiari
Un Tutor SHALL poter elencare solo i beneficiari di cui è proprietario, con ricerca per nome e cognome, zona di
residenza e numero dell'eventuale segnalazione aperta, e aprirne la scheda. Il dettaglio di un beneficiario, con i dati
completi, SHALL essere visibile solo al Tutor proprietario, ai Tutor dei progetti in cui il beneficiario ha segnalazioni,
al Call Center e all'ADMIN; negli altri casi il sistema MUST rispondere 404 senza rivelare l'esistenza del beneficiario.
Il controllo SHALL essere nelle API.

#### Scenario: Elenco del Tutor
- **WHEN** un Tutor richiede l'elenco dei beneficiari
- **THEN** riceve solo i beneficiari di cui è proprietario, non quelli dei colleghi, neppure dello stesso progetto

#### Scenario: Ricerca
- **WHEN** il Tutor cerca "verdi lucia" in "I miei beneficiari"
- **THEN** l'elenco mostra solo "Verdi Lucía" e il conteggio è annunciato

#### Scenario: Collega di progetto
- **WHEN** un Tutor assegnato al progetto di una segnalazione del beneficiario ne apre la scheda
- **THEN** vede nome, cognome e gli altri dati, senza "Modifica"

#### Scenario: Dettaglio di un beneficiario altrui
- **WHEN** un Tutor assegnato solo ad altri progetti richiede il dettaglio di un beneficiario di un altro Tutor
- **THEN** il sistema risponde 404

## ADDED Requirements

### Requirement: Modifica del beneficiario
Il sistema SHALL consentire la modifica dei dati di un beneficiario solo al Tutor proprietario, con le stesse regole
della registrazione; SHALL registrare nell'audit autore, istante e i soli nomi dei campi cambiati, senza valori.

#### Scenario: Modifica del proprietario
- **WHEN** il proprietario cambia nome e iscrizione L. 68/99
- **THEN** i dati sono aggiornati e l'audit contiene un evento `UPDATE` con i campi `firstName` e `hasLaw68`, senza valori

#### Scenario: Nessuna modifica
- **WHEN** il proprietario salva senza cambiare nulla
- **THEN** non si aggiunge alcun evento di audit

#### Scenario: Non proprietario
- **WHEN** un Tutor dello stesso progetto, o il Call Center, prova a modificare il beneficiario
- **THEN** la risposta è `403`; per un Tutor che non lo vede è `404`
