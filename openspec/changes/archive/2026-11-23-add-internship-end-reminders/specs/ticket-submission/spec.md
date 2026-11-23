## ADDED Requirements

### Requirement: Nessuna nuova segnalazione con tirocini finiti da chiudere
Dal giorno dopo la fine prevista di un suo tirocinio, che tiene conto delle proroghe, finché il tirocinio non è chiuso
con il report di restituzione o prorogato, il Tutor MUST NOT poter aprire nuove segnalazioni, né dal modulo né dalla
bacheca (PO, 22/11). Le pagine SHALL mostrare al posto del modulo l'elenco dei tirocini da chiudere, con il numero
della segnalazione, la fine prevista e il collegamento. Il backend SHALL rifiutare con `409` e l'elenco delle
segnalazioni. Gli altri Tutor MUST NOT essere bloccati.

#### Scenario: Tirocinio finito ieri
- **WHEN** il Tutor ha un tirocinio in corso finito ieri e apre «Nuova segnalazione»
- **THEN** vede «Prima chiudi i tirocini finiti» con il collegamento alla segnalazione, e il modulo non c'è

#### Scenario: Richiesta diretta all'API
- **WHEN** lo stesso Tutor invia comunque una segnalazione, o una candidatura dalla bacheca
- **THEN** la risposta è `409` e il messaggio elenca le segnalazioni da chiudere

#### Scenario: Fine oggi
- **WHEN** il tirocinio finisce oggi
- **THEN** il Tutor apre nuove segnalazioni normalmente

#### Scenario: Proroga
- **WHEN** il Tutor registra una proroga del tirocinio finito
- **THEN** l'elenco si svuota e può di nuovo aprire segnalazioni
