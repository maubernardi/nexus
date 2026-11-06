## ADDED Requirements

### Requirement: Disattivazione e cambio di ruolo
L'ADMIN SHALL disattivare, riattivare e cambiare il ruolo di un utente da NEXUS; ogni cambio MUST valere sia in NEXUS
sia in Keycloak, e se Keycloak non risponde nulla cambia. Un utente disattivato MUST NOT accedere e le sue sessioni si
chiudono; i suoi ticket restano consultabili. Il nuovo ruolo SHALL valere dal login successivo. Un Tutor con
segnalazioni aperte MUST NOT essere disattivato né cambiare ruolo finché non sono spostate. I ticket in carico a un
operatore disattivato, o che diventa Tutor, MUST tornare liberi nella coda del Call Center. L'ADMIN MUST NOT
disattivare se stesso né cambiarsi il ruolo. Ogni cambio SHALL essere registrato nell'audit.

#### Scenario: Operatore disattivato
- **WHEN** l'ADMIN disattiva un operatore con due ticket in carico, dopo l'avviso
- **THEN** l'operatore non accede più a NEXUS né a Keycloak, i due ticket sono liberi nella coda e la cronologia
  mostra «Tornata in coda»

#### Scenario: Riattivazione
- **WHEN** l'ADMIN riattiva un utente disattivato
- **THEN** l'utente accede di nuovo con la sua password

#### Scenario: Tutor con segnalazioni aperte
- **WHEN** l'ADMIN prova a disattivare un Tutor, o a cambiargli il ruolo, mentre ha segnalazioni aperte
- **THEN** l'operazione è rifiutata e la pagina rimanda allo spostamento delle segnalazioni

#### Scenario: Cambio di ruolo
- **WHEN** l'ADMIN cambia il ruolo di un operatore in ADMIN
- **THEN** il ruolo cambia in NEXUS e in Keycloak, le sessioni dell'utente si chiudono e al login successivo vale il
  nuovo ruolo; un token emesso prima del cambio non riporta indietro il ruolo in NEXUS

#### Scenario: L'ADMIN stesso
- **WHEN** l'ADMIN prova a disattivare il proprio utente o a cambiarsi il ruolo
- **THEN** l'operazione è rifiutata

#### Scenario: Keycloak non raggiungibile
- **WHEN** Keycloak non risponde durante la disattivazione o il cambio di ruolo
- **THEN** la risposta è `503` e in NEXUS non cambia nulla
