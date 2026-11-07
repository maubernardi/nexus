## ADDED Requirements

### Requirement: Recupero della password
La pagina di accesso SHALL offrire il collegamento «Password dimenticata?». Indicando username o email, l'utente SHALL
ricevere per email, dal mittente di NEXUS, un collegamento per impostare una nuova password, valido 30 minuti. La pagina
di recupero SHALL usare il tema di NEXUS, in italiano. Quando la password cambia, l'utente SHALL ricevere un'email di
sicurezza; le password sbagliate MUST NOT generare email (richiesta del PO del 07/11).

#### Scenario: Collegamento per email
- **WHEN** un utente sceglie «Password dimenticata?» e scrive la sua email
- **THEN** riceve un'email con il collegamento; aprendolo entro 30 minuti imposta una nuova password ed entra in NEXUS

#### Scenario: Collegamento scaduto
- **WHEN** l'utente apre il collegamento dopo 30 minuti
- **THEN** Keycloak lo rifiuta e propone di chiederne uno nuovo

#### Scenario: Email di sicurezza
- **WHEN** un utente imposta una nuova password con il collegamento
- **THEN** riceve una sola email che lo avvisa del cambio; una password sbagliata all'accesso non manda email

#### Scenario: Pagina con il tema
- **WHEN** si apre la pagina di recupero dalla pagina di accesso
- **THEN** la pagina ha l'intestazione e lo stile di NEXUS
