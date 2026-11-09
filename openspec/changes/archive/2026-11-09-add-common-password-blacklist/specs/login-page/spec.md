## ADDED Requirements

### Requirement: Password comuni rifiutate
Quando un utente imposta o cambia la password, Keycloak SHALL rifiutare una password che compare nell'elenco delle
password più usate, senza distinguere maiuscole e minuscole, con un messaggio in italiano che inviti a sceglierne
un'altra. La regola si aggiunge a quelle esistenti (almeno 12 caratteri, diversa da username ed email) e MUST valere per
la prima password dell'invito, il recupero e ogni aggiornamento.

#### Scenario: Password comune
- **WHEN** un utente sceglie `Password1234` come nuova password
- **THEN** la password è rifiutata con «Password troppo comune» e l'utente resta nella pagina per sceglierne un'altra

#### Scenario: Password non comune
- **WHEN** l'utente sceglie una password lunga che non è nell'elenco
- **THEN** la password è accettata
