## Purpose

Permette ai Tutor di registrare i candidati (tirocinanti) e ai ruoli autorizzati di consultarli, garantendo dati validi e
accesso limitato ai soli aventi diritto.

## ADDED Requirements

### Requirement: Registrazione del candidato
Un utente con ruolo `TUTOR` SHALL poter registrare un candidato indicando nome, cognome, anno di nascita, genere, zona di
residenza e, facoltativamente, nazionalità, cittadinanza, tipi di patente, possesso di un veicolo, mezzo di trasporto,
iscrizione al collocamento mirato (L. 68/99), titolo di studio, vincoli e lingue conosciute con livello. Il Tutor che
registra il candidato SHALL diventarne il proprietario. Utenti con altri ruoli MUST NOT poter registrare candidati.

#### Scenario: Registrazione riuscita
- **WHEN** un Tutor invia i dati obbligatori validi
- **THEN** il sistema risponde 201 con il candidato creato, il cui proprietario è quel Tutor

#### Scenario: Ruolo non autorizzato
- **WHEN** un operatore Call Center tenta di registrare un candidato
- **THEN** il sistema risponde 403

### Requirement: Validazione dei dati del candidato
Il sistema SHALL rifiutare con 400 e l'indicazione dei campi errati i dati non validi: campi obbligatori mancanti, anno
di nascita nel futuro o precedente al 1900, nazionalità o cittadinanza non ISO 3166-1 alpha-2, lingua non ISO 639-1,
lingua ripetuta, livello o valori codificati non ammessi, zona inesistente o disattivata. Il possesso della patente SHALL
essere dedotto dalla presenza di almeno un tipo di patente.

#### Scenario: Anno di nascita futuro
- **WHEN** un Tutor invia un anno di nascita successivo all'anno corrente
- **THEN** il sistema risponde 400 con un errore sul campo `birthYear`

#### Scenario: Lingua ripetuta
- **WHEN** un Tutor indica due volte la stessa lingua
- **THEN** il sistema risponde 400 con un errore sul campo `languages`

#### Scenario: Patente dedotta dai tipi
- **WHEN** un Tutor indica i tipi di patente B e CQC
- **THEN** il candidato risulta in possesso di patente

### Requirement: Consultazione dei candidati
Un Tutor SHALL poter elencare solo i candidati di cui è proprietario. Il dettaglio di un candidato SHALL essere visibile
solo al Tutor proprietario, al Call Center e all'ADMIN; negli altri casi il sistema MUST rispondere 404 senza rivelare
l'esistenza del candidato.

#### Scenario: Elenco del Tutor
- **WHEN** un Tutor richiede l'elenco dei candidati
- **THEN** riceve solo i candidati di cui è proprietario

#### Scenario: Dettaglio di un candidato altrui
- **WHEN** un Tutor richiede il dettaglio di un candidato di un altro Tutor
- **THEN** il sistema risponde 404

### Requirement: Modulo accessibile di registrazione
Il modulo di registrazione SHALL associare a ogni campo un'etichetta visibile, indicare in testo i campi obbligatori,
mostrare gli errori accanto ai campi e in un riepilogo in cima al modulo con collegamenti ai campi, annunciare il
riepilogo alle tecnologie assistive e spostarvi il focus all'invio non riuscito. Il modulo SHALL essere utilizzabile solo
da tastiera e senza scroll orizzontale a 320 px.

#### Scenario: Invio con errori
- **WHEN** il Tutor invia il modulo senza cognome
- **THEN** il focus si sposta sul riepilogo degli errori, che contiene un collegamento al campo Cognome, e il campo è marcato come non valido
