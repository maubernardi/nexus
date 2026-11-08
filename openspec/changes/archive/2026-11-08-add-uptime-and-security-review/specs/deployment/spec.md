## ADDED Requirements

### Requirement: Controllo della disponibilità
Un controllo esterno SHALL verificare ogni 10 minuti, dalle 8 alle 20 ora italiana, che la PWA, Keycloak e il backend
della demo rispondano e che i certificati non scadano entro 14 giorni. Un controllo fallito dopo 3 tentativi SHALL
avvisare il PO per email. Il controllo MUST NOT usare segreti.

#### Scenario: Demo online
- **WHEN** il controllo gira alle 10:00 e sito, Keycloak e API rispondono come previsto
- **THEN** il controllo riesce e nessuno viene avvisato

#### Scenario: Backend giù
- **WHEN** l'API risponde `502` per tre tentativi di fila
- **THEN** il controllo fallisce e il PO riceve un'email da GitHub

#### Scenario: Fuori orario
- **WHEN** il controllo pianificato parte alle 21:30 ora italiana
- **THEN** non controlla nulla e non avvisa

### Requirement: Revisione di sicurezza
Prima del passaggio in produzione SHALL esserci una revisione di sicurezza secondo OWASP ASVS livello 1, con l'esito di
ogni requisito applicabile, le lacune e le azioni proposte; SHALL essere aggiornata quando cambiano parti esaminate
(caricamento di file, produzione).

#### Scenario: Esito documentato
- **WHEN** il PO apre `docs/security/asvs-l1.md`
- **THEN** trova per ogni capitolo i requisiti con esito ed evidenza, e le lacune con chi e quando le chiude
