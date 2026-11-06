## ADDED Requirements

### Requirement: Progetti assegnati ai Tutor
L'ADMIN SHALL assegnare e togliere i progetti di un Tutor da NEXUS. Il Tutor MUST vedere in sola lettura i ticket dei
progetti assegnati e poter segnalare solo su quelli, a partire dalla richiesta successiva. Solo i Tutor SHALL avere
progetti assegnati: Call Center e ADMIN vedono già tutti i progetti. Un progetto disattivato MUST NOT essere assegnato,
ma se è già assegnato SHALL poter essere tolto. Ogni modifica SHALL essere registrata nell'audit con i progetti prima
e dopo.

#### Scenario: Assegnazione di un progetto
- **WHEN** l'ADMIN assegna il progetto POLIS a un Tutor
- **THEN** il Tutor vede in sola lettura i ticket di POLIS, può scegliere POLIS in una nuova segnalazione e la
  modifica è nell'audit

#### Scenario: Progetto tolto
- **WHEN** l'ADMIN toglie il progetto POLIS a un Tutor
- **THEN** il Tutor non vede più i ticket di POLIS degli altri Tutor; i ticket che ha segnalato lui restano suoi

#### Scenario: Progetto disattivato
- **WHEN** l'ADMIN prova ad assegnare un progetto disattivato
- **THEN** l'assegnazione è rifiutata; un progetto disattivato già assegnato si può togliere

#### Scenario: Utente che non è Tutor
- **WHEN** l'ADMIN prova ad assegnare progetti a un operatore del Call Center o a un ADMIN
- **THEN** l'assegnazione è rifiutata e l'elenco degli utenti indica «Tutti» per i loro progetti

#### Scenario: Solo l'ADMIN
- **WHEN** un Tutor o un operatore prova a cambiare i progetti di un utente
- **THEN** la risposta è `403`
