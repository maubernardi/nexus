## ADDED Requirements

### Requirement: Gestione dei cataloghi
L'ADMIN SHALL poter aggiungere, rinominare, disattivare e riattivare zone e tipologie di mansione da una pagina
dedicata, senza rilasci. Le voci MUST NOT essere cancellate: una voce disattivata non è più selezionabile per le scelte
nuove ma resta sui dati che la usano. Il codice si ricava dal nome alla creazione e poi non cambia. Tra le voci attive
il nome MUST essere unico, senza distinguere maiuscole e minuscole. Ogni modifica SHALL essere registrata nell'audit.

#### Scenario: Nuova zona
- **WHEN** l'ADMIN aggiunge la zona «Sesto Fiorentino»
- **THEN** la zona è attiva con il codice `SESTO_FIORENTINO`, compare nei menu degli altri moduli e la creazione è
  nell'audit

#### Scenario: Nome già presente
- **WHEN** l'ADMIN aggiunge «fiesole» e la zona attiva «Fiesole» esiste già
- **THEN** il campo segnala che «Fiesole» è già nel catalogo

#### Scenario: Disattivazione
- **WHEN** l'ADMIN disattiva una zona usata da un beneficiario
- **THEN** la zona non compare più nei menu per le scelte nuove, ma il beneficiario la mantiene

#### Scenario: Solo l'ADMIN
- **WHEN** un operatore del Call Center chiede la gestione dei cataloghi
- **THEN** la risposta è `403`
