## ADDED Requirements

### Requirement: Ambiente di collaudo sul PC
Il sistema SHALL offrire un ambiente di collaudo che si avvia sul PC del PO con un comando, per una PR, un branch o un
commit, prima del merge. L'ambiente MUST usare lo stack della produzione di quel commit: servizi, immagini costruite dai
suoi sorgenti, realm con le stesse regole e lo stesso proxy. MUST avere dati e utenti finti e MUST NOT usare i dati
veri. Le email MUST restare in una casella finta, leggibile nel browser, e MUST NOT uscire dal PC. Le porte MUST essere
aperte solo su `127.0.0.1` e MUST NOT includere quella del tunnel verso la console della produzione (8081). Ogni avvio
MUST ripartire dai dati finti; l'ambiente si può fermare e riprendere con i suoi dati, e cancellare senza lasciare
container, volumi né immagini. La CI MUST provarlo a ogni PR.

#### Scenario: Collaudo di una PR
- **WHEN** il comando di avvio riceve il numero di una PR
- **THEN** l'ambiente parte con il codice della PR, i dati finti e gli utenti finti
- **AND** NEXUS risponde su `http://localhost:8090`; le API senza accesso rispondono `401`

#### Scenario: Accesso come in produzione
- **WHEN** un utente finto accede dalla pagina di Keycloak dell'ambiente
- **THEN** entra in NEXUS con il suo ruolo, con la pagina, la policy delle password e le durate di sessione della
  produzione

#### Scenario: Email nella casella finta
- **WHEN** NEXUS o Keycloak mandano un'email, per esempio un codice o il recupero della password
- **THEN** l'email compare nella casella del collaudo, su `http://localhost:8025`
- **AND** non esce dal PC

#### Scenario: Accesso degli amministratori bloccato
- **WHEN** si apre `/admin` o `/realms/master` dall'indirizzo di Keycloak dell'ambiente
- **THEN** la risposta è `403`, come in produzione

#### Scenario: Porta del tunnel libera
- **WHEN** l'ambiente è avviato
- **THEN** la porta 8081 del PC resta libera per il tunnel verso la produzione

#### Scenario: Nuovo avvio
- **WHEN** si avvia il collaudo di un'altra PR
- **THEN** i dati del collaudo precedente sono cancellati e l'ambiente riparte dai dati finti

#### Scenario: Cancellazione
- **WHEN** si cancella l'ambiente
- **THEN** non restano container, volumi, immagini né sorgenti del collaudo
