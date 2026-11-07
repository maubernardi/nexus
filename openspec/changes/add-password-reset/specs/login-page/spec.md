## ADDED Requirements

### Requirement: Recupero della password
La pagina di accesso SHALL offrire il collegamento «Password dimenticata?». Indicando username o email, l'utente SHALL
ricevere per email, dal mittente di NEXUS, un collegamento per impostare una nuova password, valido 30 minuti. La pagina
di recupero SHALL usare il tema di NEXUS, in italiano.

#### Scenario: Collegamento per email
- **WHEN** un utente sceglie «Password dimenticata?» e scrive la sua email
- **THEN** riceve un'email con il collegamento; aprendolo entro 30 minuti imposta una nuova password ed entra in NEXUS

#### Scenario: Collegamento scaduto
- **WHEN** l'utente apre il collegamento dopo 30 minuti
- **THEN** Keycloak lo rifiuta e propone di chiederne uno nuovo

#### Scenario: Pagina con il tema
- **WHEN** si apre la pagina di recupero dalla pagina di accesso
- **THEN** la pagina ha l'intestazione e lo stile di NEXUS
