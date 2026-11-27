## ADDED Requirements

### Requirement: Avviso di chiusura della richiesta di contatto
Alla chiusura di una richiesta di contatto il Tutor che l'ha inviata SHALL ricevere l'avviso nell'app e per email, con
il numero della richiesta, l'esito e la nota, e il collegamento alla pagina della richiesta. I testi SHALL essere tra i
«Testi dei messaggi» modificabili dall'ADMIN, con i predefiniti e i segnaposto `{numero}`, `{esito}` e `{nota}`.
L'email SHALL avere l'oggetto «PORTALE NEXUS - Richiesta di contatto n. N - …» e la firma «Portale Nexus».

#### Scenario: Avviso nell'app
- **WHEN** l'operatore chiude la richiesta di contatto n. 3 del Tutor
- **THEN** il Tutor riceve l'avviso con l'esito e la nota, che apre la pagina della richiesta

#### Scenario: Email
- **WHEN** parte l'avviso di chiusura della richiesta n. 3
- **THEN** l'email ha l'oggetto «PORTALE NEXUS - Richiesta di contatto n. 3 - Esito della richiesta di contatto», il
  collegamento alla richiesta e la firma «Portale Nexus»
