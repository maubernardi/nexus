## ADDED Requirements

### Requirement: Dati del promotore per progetto
L'ADMIN SHALL poter indicare per ogni progetto, nei cataloghi, i dati del soggetto promotore usati nei documenti:
- denominazione, sede e codice fiscale;
- legale rappresentante con ruolo, luogo e data di nascita;
- telefono, mail e PEC;
- posizione INAIL;
- polizza RC con compagnia, numero e scadenza.

Tutti i dati sono facoltativi. Mail e PEC MUST essere indirizzi validi. Ogni modifica SHALL essere nell'audit del
progetto; per nome, luogo e data di nascita del legale rappresentante l'audit MUST registrare solo che sono cambiati.
Gli altri utenti MUST NOT modificarli.

#### Scenario: Dati di GOL
- **WHEN** l'ADMIN indica per GOL la polizza «RC-1» con scadenza 22/07/2027 e salva
- **THEN** i dati sono salvati e la modifica è nell'audit del progetto

#### Scenario: Data di nascita nell'audit
- **WHEN** l'ADMIN cambia la data di nascita del legale rappresentante
- **THEN** l'audit dice che è cambiata, senza la data vecchia né la nuova

#### Scenario: Solo l'ADMIN
- **WHEN** un Tutor chiede di modificare i dati del promotore di un progetto
- **THEN** la risposta è `403`

### Requirement: Variabili libere per progetto
L'ADMIN SHALL poter aggiungere, modificare e togliere variabili per ogni progetto: nome, valore e descrizione
facoltativa. Il nome MUST essere fatto di lettere maiuscole, cifre e trattino basso, cominciare con una lettera e avere
al massimo 50 caratteri; MUST NOT ripetersi nel progetto né essere un segnaposto di NEXUS. Il valore è obbligatorio.
Il nome di una variabile salvata non si modifica. Una variabile usata da un modello attivo del progetto MUST NOT
essere tolta: il messaggio SHALL dire quale modello la usa. Ogni modifica SHALL essere nell'audit del progetto.

#### Scenario: Variabile nuova
- **WHEN** l'ADMIN aggiunge a GOL la variabile `SEDE_OPERATIVA` con valore «Via Roma 1, Firenze»
- **THEN** la variabile è salvata e la modifica è nell'audit

#### Scenario: Nome di NEXUS
- **WHEN** l'ADMIN aggiunge la variabile `PROMOTORE`
- **THEN** il salvataggio è rifiutato: `PROMOTORE` è già un segnaposto di NEXUS

#### Scenario: Variabile usata da un modello
- **WHEN** l'ADMIN toglie `SEDE_OPERATIVA` e il modello attivo «Convenzione GOL» la usa
- **THEN** il salvataggio è rifiutato con il nome del modello
