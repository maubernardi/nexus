## ADDED Requirements

### Requirement: Scadenza del timer
Il sistema SHALL controllare ogni ora le proposte accettate e, raggiunta la scadenza del timer (fine del 7° giorno
lavorativo, ora di Roma), SHALL riportare la segnalazione in `RIAPERTO` in carico allo stesso operatore, azzerare il
timer e liberare la mansione, registrando l'evento nell'audit e nello storico degli stati come azione del sistema e
notificandolo al Tutor e all'operatore. La scadenza MUST essere applicata solo dal sistema, mai su richiesta di un
utente, e MUST NOT essere seguita dal promemoria.

#### Scenario: Scadenza
- **WHEN** il controllo gira all'istante della scadenza di una proposta accettata senza appuntamento
- **THEN** la segnalazione è "Riaperto", la mansione è libera, in cronologia c'è "Scadenza del timer" con autore
  "Sistema", e Tutor e operatore ricevono la notifica

#### Scenario: Prima della scadenza
- **WHEN** il controllo gira un secondo prima della scadenza
- **THEN** non succede nulla

#### Scenario: Nessun utente
- **WHEN** un utente, anche ADMIN, prova ad applicare la scadenza
- **THEN** l'operazione è negata
