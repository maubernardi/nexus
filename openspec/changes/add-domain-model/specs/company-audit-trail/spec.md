## Purpose

Garantisce un registro cronologico e non modificabile degli eventi rilevanti per ogni azienda (proposte, esiti,
tirocini), a tutela della trasparenza verso aziende, enti finanziatori e controlli.

## ADDED Requirements

### Requirement: Registro in sola aggiunta
Il sistema SHALL registrare per ogni azienda eventi con tipo, dettagli, istante, autore ed eventuale ticket collegato.
Gli eventi registrati MUST NOT poter essere modificati né cancellati, neanche tramite accesso diretto al database con le
credenziali dell'applicazione.

#### Scenario: Aggiunta di un evento
- **WHEN** viene registrato un evento per un'azienda
- **THEN** l'evento è consultabile nello storico dell'azienda con istante e autore

#### Scenario: Tentativo di modifica
- **WHEN** si tenta di modificare un evento già registrato
- **THEN** il database rifiuta l'operazione

#### Scenario: Tentativo di cancellazione
- **WHEN** si tenta di cancellare uno o tutti gli eventi registrati
- **THEN** il database rifiuta l'operazione
