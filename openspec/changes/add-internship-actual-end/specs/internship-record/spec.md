## ADDED Requirements

### Requirement: Funzione o ruolo dei tutor aziendali
Nella scheda del tirocinio ogni tutor aziendale SHALL avere, oltre al nome e al recapito, la funzione o ruolo in
azienda: testo libero facoltativo, al massimo 200 caratteri. Un testo di soli spazi MUST essere salvato come vuoto.
L'audit SHALL registrare il cambio del ruolo come cambio dei tutor aziendali, senza valori.

#### Scenario: Tutor con il ruolo
- **WHEN** il Tutor scrive «Responsabile di produzione» come ruolo del primo tutor aziendale e salva
- **THEN** riaprendo la scheda il tutor ha quel ruolo

#### Scenario: Ruolo non indicato
- **WHEN** il Tutor salva un tutor aziendale senza il ruolo
- **THEN** la scheda è salvata e il tutor conta per l'avvio del tirocinio

#### Scenario: Audit del ruolo
- **WHEN** il Tutor cambia soltanto il ruolo di un tutor aziendale
- **THEN** l'audit registra che i tutor aziendali sono cambiati, senza il ruolo
