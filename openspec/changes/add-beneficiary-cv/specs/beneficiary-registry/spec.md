## ADDED Requirements

### Requirement: Curriculum del beneficiario
Il Tutor proprietario SHALL poter caricare, sostituire e togliere il CV del beneficiario. Il CV MUST essere un PDF,
riconosciuto dal contenuto, di al massimo 5 MB. Il CV SHALL potersi scaricare solo da chi vede il beneficiario
(proprietario, Tutor dei progetti delle sue segnalazioni, Call Center, ADMIN), sempre come allegato. L'anonimizzazione
MUST cancellare il CV; un beneficiario anonimizzato MUST NOT avere un CV. Ogni caricamento, sostituzione e rimozione
SHALL essere registrato nell'audit senza il nome del file.

#### Scenario: Caricamento
- **WHEN** il Tutor proprietario carica un PDF di 300 KB
- **THEN** la scheda mostra nome e dimensione del CV e chi vede il beneficiario può scaricarlo

#### Scenario: File non valido
- **WHEN** il Tutor carica un'immagine rinominata in `.pdf`, oppure un PDF di 6 MB
- **THEN** il caricamento è rifiutato con «Il CV deve essere un file PDF» o «Il CV supera i 5 MB»

#### Scenario: Chi non lo vede
- **WHEN** un Tutor che non vede il beneficiario chiede il CV
- **THEN** la risposta è `404`; un operatore che prova a caricarlo riceve `403`

#### Scenario: Anonimizzazione
- **WHEN** un beneficiario con il CV viene anonimizzato
- **THEN** il CV è cancellato e non se ne può caricare un altro
