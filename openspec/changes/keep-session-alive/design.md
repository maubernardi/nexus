## Context

- Il frontend usa `keycloak-js`: token di accesso di 5 minuti, refresh token legato alla sessione di Keycloak, che
  scade dopo 30 minuti senza rinnovi e comunque dopo 10 ore (realm `nexus`).
- `getAuthHeaders` rinnova il token (`updateToken(30)`) solo prima di una chiamata all'API.
- Le chiamate periodiche (notifiche e segnalazioni ogni 60 secondi) usano `refetchInterval` di TanStack Query. Senza
  `refetchIntervalInBackground` si fermano quando la pagina non è visibile, e su Windows Chrome la considera nascosta
  anche con lo schermo bloccato o la finestra coperta.

## Goals / Non-Goals

**Goals:** nessuna richiesta di password durante la giornata con l'app aperta; avviso prima delle 10 ore; nessuna
modifica alla configurazione di Keycloak.

**Non-Goals:**
- sessioni oltre le 10 ore;
- il «ricordami» di Keycloak;
- rinnovi con il PC in sospensione, perché il browser non gira.

## Decisions

### D1. Rinnovo con un timer, non con le chiamate all'API
Dopo l'accesso, `initAuth` avvia un timer di 60 secondi che chiama `updateToken(70)`. Il token si rinnova circa ogni 4
minuti, e ogni rinnovo azzera l'inattività della sessione in Keycloak. Con la pagina nascosta Chrome rallenta i timer
fino a una volta al minuto, che basta. Le chiamate all'API non cambiano.

Alternativa scartata: `refetchIntervalInBackground` sulle query. Avrebbe caricato dati non visti, solo come effetto
collaterale.

### D2. Rinnovo fallito
Se la sessione è finita, Keycloak risponde 400. keycloak-js cancella allora i token e, con `login-required`, apre da
solo la pagina di accesso: nessun dato resta sullo schermo. Se invece manca la rete, il rinnovo fallisce senza altri
effetti e si riprova al giro successivo.

Il rinnovo quindi si limita a ignorare l'errore. Provato in locale il 24/11 con una sessione massima di 8 minuti: al
primo giro dopo la fine NEXUS è passato alla pagina di accesso, e dopo l'accesso è tornato alla stessa vista.

### D3. Fine della sessione e avviso
La fine della sessione è la scadenza del refresh token:
- con il rinnovo attivo resta circa 30 minuti avanti;
- vicino alle 10 ore si ferma al massimo della sessione.

`SessionExpiryBanner` controlla ogni 15 secondi. Quando mancano 5 minuti o meno compare la striscia, nello stesso
stile di quella dell'aggiornamento (WCAG 2.4.11), con `role="alert"`. Contiene l'ora della scadenza, ora di Roma, e
due pulsanti:
- **«Accedi di nuovo adesso»**: `keycloak.login({ prompt: 'login' })`, cioè la password e una sessione nuova di 10 ore;
- **«Ho capito»**: chiude la striscia fino alla scadenza successiva.

Solo con Keycloak: con l'accesso di sviluppo la striscia non compare.

## Risks / Trade-offs

- **PC lasciato solo con NEXUS aperto**: resta collegato fino a 10 ore. Lo copre il blocco automatico dello schermo dei
  PC, da verificare dal PO.
- **Più richieste a Keycloak**: un rinnovo ogni 4 minuti per scheda aperta. Con poche decine di utenti è trascurabile.
- **Il testo dell'avviso non dice «10 ore»**: la durata la decide Keycloak e può cambiare. L'avviso dice l'ora.
