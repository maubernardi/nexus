# beneficiary-registry Specification

## Purpose
Permette ai Tutor di registrare i beneficiari (tirocinanti) e ai ruoli autorizzati di consultarli, garantendo dati validi e
accesso limitato ai soli aventi diritto.
## Requirements
### Requirement: Registrazione del beneficiario
Un utente con ruolo `TUTOR` SHALL poter registrare un beneficiario indicando nome, cognome, anno di nascita, genere, zona di
residenza e, facoltativamente, nazionalità, cittadinanza, tipi di patente, possesso di un veicolo, mezzo di trasporto,
iscrizione al collocamento mirato (L. 68/99), titolo di studio, vincoli e lingue conosciute con livello. Il Tutor che
registra il beneficiario SHALL diventarne il proprietario. Utenti con altri ruoli MUST NOT poter registrare beneficiari.

#### Scenario: Registrazione riuscita
- **WHEN** un Tutor invia i dati obbligatori validi
- **THEN** il sistema risponde 201 con il beneficiario creato, il cui proprietario è quel Tutor

#### Scenario: Ruolo non autorizzato
- **WHEN** un operatore Call Center tenta di registrare un beneficiario
- **THEN** il sistema risponde 403

### Requirement: Validazione dei dati del beneficiario
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
- **THEN** il beneficiario risulta in possesso di patente

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

### Requirement: Modulo accessibile di registrazione
Il modulo di registrazione SHALL associare a ogni campo un'etichetta visibile, indicare in testo i campi obbligatori,
mostrare gli errori accanto ai campi e in un riepilogo in cima al modulo con collegamenti ai campi, annunciare il
riepilogo alle tecnologie assistive e spostarvi il focus all'invio non riuscito. Il modulo SHALL essere utilizzabile solo
da tastiera e senza scroll orizzontale a 320 px.

#### Scenario: Invio con errori
- **WHEN** il Tutor invia il modulo senza cognome
- **THEN** il focus si sposta sul riepilogo degli errori, che contiene un collegamento al campo Cognome, e il campo è marcato come non valido

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

### Requirement: Conservazione e anonimizzazione
Il sistema SHALL segnare ogni notte «Da anonimizzare» i beneficiari senza attività (scheda, segnalazioni, schede dei
tirocini) da più del periodo di conservazione, per ora 2 anni; una nuova attività SHALL togliere il segno. Il Tutor
proprietario e l'ADMIN SHALL vedere evidenziati e filtrare i beneficiari da anonimizzare e decidere: anonimizzarli, dopo
una conferma, oppure tenerne i dati per sempre con un motivo obbligatorio. L'anonimizzazione MUST lasciare di nome e
cognome solo le iniziali e cancellare CV, vincoli e, dalle schede dei tirocini, codice fiscale, data e luogo di nascita,
residenza e note; i dati statistici MUST restare. Ogni passaggio SHALL essere registrato nell'audit senza valori
personali. Un beneficiario anonimizzato MUST NOT essere modificato né segnalato.

#### Scenario: Segno notturno
- **WHEN** un beneficiario non ha attività da più di 2 anni
- **THEN** il controllo della notte lo segna «Da anonimizzare», senza che il segno conti come attività

#### Scenario: Nuova attività
- **WHEN** una segnalazione del beneficiario segnato si muove o il Tutor ne modifica la scheda
- **THEN** il beneficiario non è più da anonimizzare

#### Scenario: Anonimizzazione confermata
- **WHEN** il Tutor conferma l'anonimizzazione di Mario Rossi
- **THEN** restano «M» e «R», l'anno di nascita, la zona e gli altri dati statistici; vincoli e dati identificativi della
  scheda del tirocinio sono cancellati e l'audit non contiene i valori

#### Scenario: Dati tenuti
- **WHEN** l'ADMIN sceglie di tenere i dati senza scrivere il motivo
- **THEN** la richiesta è rifiutata; con il motivo i dati si tengono e il beneficiario non viene più segnato

#### Scenario: Chi decide
- **WHEN** un altro Tutor o un operatore prova a decidere su un beneficiario, o qualcuno decide su un beneficiario non
  da anonimizzare
- **THEN** la richiesta è rifiutata

### Requirement: Curriculum del beneficiario
Il Tutor proprietario SHALL poter caricare, sostituire e togliere il CV del beneficiario. Il CV MUST essere un PDF,
riconosciuto dal contenuto, di al massimo 5 MB. Il CV SHALL potersi scaricare solo da chi vede il beneficiario
(proprietario, Tutor dei progetti delle sue segnalazioni, Call Center, ADMIN), sempre come allegato. L'anonimizzazione
MUST cancellare il CV; un beneficiario anonimizzato MUST NOT avere un CV. Ogni caricamento, sostituzione e rimozione
SHALL essere registrato nell'audit senza il nome del file.

#### Scenario: Caricamento
- **WHEN** il Tutor proprietario carica un PDF di 300 KB
- **THEN** la scheda mostra nome e dimensione del CV e chi vede il beneficiario può scaricarlo

#### Scenario: File non valido
- **WHEN** il Tutor carica un'immagine rinominata in `.pdf`, oppure un PDF di 6 MB
- **THEN** il caricamento è rifiutato con «Il CV deve essere un file PDF» o «Il CV supera i 5 MB»

#### Scenario: Chi non lo vede
- **WHEN** un Tutor che non vede il beneficiario chiede il CV
- **THEN** la risposta è `404`; un operatore che prova a caricarlo riceve `403`

#### Scenario: Anonimizzazione
- **WHEN** un beneficiario con il CV viene anonimizzato
- **THEN** il CV è cancellato e non se ne può caricare un altro

### Requirement: Contatti e presa in carico
Il Tutor proprietario SHALL poter indicare per il beneficiario, tutti facoltativi: mail e telefono, l'ente che lo ha in
carico (dal catalogo degli enti attivi) e il case manager (nome, sede operativa, telefono, mail). L'ente MUST NOT
cambiare mentre il beneficiario ha un tirocinio aperto, cioè una segnalazione non in FORM_RESTITUZIONE con la scheda del
tirocinio. Il case manager SHALL potersi sostituire anche durante il tirocinio, ma durante un tirocinio di inclusione
aperto nome, mail e telefono MUST NOT essere svuotati. Nell'audit SHALL comparire solo quali campi sono cambiati;
l'anonimizzazione MUST cancellare contatti, ente e case manager.

#### Scenario: Ente e case manager
- **WHEN** il Tutor indica l'ente «Comune di Firenze» e il case manager con nome, mail e telefono
- **THEN** la scheda del beneficiario li mostra e l'audit registra i campi cambiati senza i valori

#### Scenario: Ente bloccato
- **WHEN** il Tutor cambia l'ente di un beneficiario con un tirocinio aperto
- **THEN** il campo segnala che l'ente non si può cambiare durante il tirocinio e nulla è salvato

#### Scenario: Case manager durante l'inclusione
- **WHEN** il Tutor svuota la mail del case manager durante un tirocinio di inclusione aperto
- **THEN** il campo segnala che è obbligatoria durante il tirocinio di inclusione

#### Scenario: Anonimizzazione
- **WHEN** un beneficiario con contatti, ente e case manager viene anonimizzato
- **THEN** questi dati sono cancellati

