## ADDED Requirements

### Requirement: Firma e testi delle email
Ogni email inviata da NEXUS, dal backend o da Keycloak, SHALL chiudersi con la firma «Portale Nexus». L'avviso di
cambio della password SHALL dire «<username>, le tue credenziali sono state cambiate il <data> alle <ora> da questo
indirizzo IP: <ip>» e invitare a contattare l'amministratore se non è stato l'utente; data e ora MUST essere quelle
italiane (fuso di Roma).

#### Scenario: Firma nelle email del backend
- **WHEN** NEXUS invia una notifica, un codice per il cambio di indirizzo o l'avviso al vecchio indirizzo
- **THEN** l'email finisce con «Portale Nexus»

#### Scenario: Firma nelle email di Keycloak
- **WHEN** Keycloak invia l'invito, il collegamento per reimpostare la password o l'avviso di cambio
- **THEN** l'email finisce con «Portale Nexus», nel testo semplice e nell'HTML

#### Scenario: Avviso di cambio della password
- **WHEN** l'utente `mario.rossi` cambia la password alle 12:46 italiane del 7 novembre 2026
- **THEN** riceve «mario.rossi, le tue credenziali sono state cambiate il 07/11/2026 alle 12:46 da questo indirizzo
  IP: …», con l'invito a contattare l'amministratore e la firma
