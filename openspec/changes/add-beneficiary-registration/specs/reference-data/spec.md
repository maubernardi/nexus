## Purpose

Fornisce ai moduli dell'applicazione gli elenchi di riferimento condivisi (zone, tipologie di mansione), limitati alle
voci attive.

## ADDED Requirements

### Requirement: Elenchi di riferimento
Il sistema SHALL esporre agli utenti autenticati gli elenchi delle zone e delle tipologie di mansione **attive**, con
identificativo, codice e nome, ordinati per nome.

#### Scenario: Voce disattivata
- **WHEN** una zona è disattivata
- **THEN** non compare nell'elenco delle zone
