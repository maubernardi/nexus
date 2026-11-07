## ADDED Requirements

### Requirement: Registro degli accessi
Ogni accesso a NEXUS, riuscito o fallito, SHALL essere registrato con data, ora, indirizzo IP ed esito; un tentativo
fallito SHALL riportare il motivo. Il registro SHALL essere conservato 12 mesi e poi cancellato automaticamente.
L'ADMIN SHALL consultare il registro di ogni utente e una pagina con i tentativi falliti di tutti, anche con uno
username inesistente; ogni utente SHALL consultare i propri accessi nel profilo. Data e ora MUST essere italiane.

#### Scenario: Accessi di un utente
- **WHEN** l'ADMIN apre gli accessi di un utente che è entrato una volta e ha sbagliato la password una volta
- **THEN** vede due righe dalla più recente: «Riuscito» e «Non riuscito: password sbagliata», con data, ora e IP

#### Scenario: Tentativi falliti di tutti
- **WHEN** qualcuno prova ad accedere con uno username che non esiste
- **THEN** nella pagina «Accessi non riusciti» l'ADMIN vede lo username scritto, «Utente inesistente», data, ora e IP

#### Scenario: I miei accessi
- **WHEN** un utente apre il suo profilo
- **THEN** vede i propri accessi, riusciti e falliti, e non quelli degli altri

#### Scenario: Più di 20 accessi
- **WHEN** gli accessi sono più di 20
- **THEN** se ne vedono 20 e «Mostra accessi più vecchi» carica i successivi

#### Scenario: Registro non disponibile
- **WHEN** Keycloak non risponde
- **THEN** la pagina dice che il registro non è disponibile e propone «Riprova»

#### Scenario: Solo l'ADMIN
- **WHEN** un Tutor o un operatore chiede gli accessi di un altro utente o i tentativi falliti
- **THEN** la risposta è `403`
