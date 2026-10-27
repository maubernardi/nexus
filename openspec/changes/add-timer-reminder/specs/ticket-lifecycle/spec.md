## ADDED Requirements

### Requirement: Promemoria del timer
Il sistema SHALL controllare ogni ora le proposte accettate con il timer attivo e, a partire dal 4° giorno lavorativo
dopo l'accettazione (calendario unico), SHALL inviare al Tutor della segnalazione un promemoria come notifica in app.
Il promemoria MUST essere inviato una sola volta per timer, anche se il controllo gira più volte, e SHALL comparire in
cronologia come azione del sistema. Le segnalazioni con il timer fermo (es. appuntamento fissato) MUST NOT ricevere il
promemoria.

#### Scenario: Promemoria al 4° giorno lavorativo
- **WHEN** la proposta è stata accettata lunedì 2 novembre 2026 e il controllo gira venerdì 6 novembre
- **THEN** il Tutor riceve il promemoria; giovedì 5 novembre non l'avrebbe ricevuto

#### Scenario: Una sola volta
- **WHEN** il controllo gira di nuovo dopo l'invio
- **THEN** il promemoria non viene ripetuto

#### Scenario: Timer fermo
- **WHEN** l'appuntamento è già stato fissato
- **THEN** nessun promemoria
