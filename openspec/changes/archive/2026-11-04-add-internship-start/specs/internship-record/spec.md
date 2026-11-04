## ADDED Requirements

### Requirement: Scheda completa per l'avvio
La scheda SHALL indicare che cosa manca per avviare il tirocinio, in base ai dati salvati. Per l'avvio servono:
- tipo di tirocinio, data di inizio, data di fine prevista, sede, codice ATECO;
- almeno un tutor aziendale e la griglia oraria con almeno un giorno;
- codice fiscale, data e luogo di nascita e residenza del beneficiario;
- il legale rappresentante dell'azienda con codice fiscale, nascita e residenza.

Sospensioni e note MUST NOT essere richieste.

#### Scenario: Elenco di ciò che manca
- **WHEN** il Tutor apre una scheda senza codice fiscale del beneficiario e senza tutor aziendali
- **THEN** la pagina elenca proprio questi due dati, ciascuno con il collegamento al campo

#### Scenario: Legale rappresentante incompleto
- **WHEN** il legale rappresentante dell'azienda è indicato senza codice fiscale
- **THEN** la pagina dice che i suoi dati vanno completati dal Call Center
