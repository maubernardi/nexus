# audit-log Specification

## Purpose
Registro immodificabile di ogni cambio di stato e di ogni scelta, per ricostruire chi ha fatto cosa, quando e perché.
## Requirements
### Requirement: Registro in sola aggiunta
Il sistema SHALL registrare ogni evento di audit con autore, istante, tipo e id dell'oggetto, azione, modifiche e motivo
facoltativo, e il database SHALL rifiutare modifica, cancellazione e svuotamento degli eventi registrati.

#### Scenario: Tentativo di manomissione
- **WHEN** si tenta di modificare, cancellare o svuotare gli eventi di audit
- **THEN** il database rifiuta l'operazione

### Requirement: Cambi di stato e scelte registrati
Il sistema SHALL registrare un evento di audit per ogni transizione di stato del ticket, compresa la creazione, nella
stessa transazione dell'operazione: lo stato prima e dopo, la nota come motivo e, alla creazione, le scelte fatte
(beneficiario, progetto, mansione richiesta).

#### Scenario: Invio di una segnalazione
- **WHEN** il Tutor invia una segnalazione
- **THEN** l'audit contiene un evento `SUBMIT` sul ticket con autore il Tutor, stato da nessuno a `NUOVA` e le scelte di
  beneficiario, progetto e mansione

#### Scenario: Operazione fallita
- **WHEN** un'operazione viene rifiutata o annullata
- **THEN** nell'audit non resta alcun evento di quell'operazione

### Requirement: Dati personali nell'audit
Il sistema SHALL NOT registrare nell'audit i valori dei dati personali: per i beneficiari registra solo l'azione e,
nelle modifiche, i nomi dei campi interessati; per le aziende registra referente, telefono ed email solo come campi
impostati o modificati, senza valori, mentre gli altri dati dell'azienda restano con i valori prima e dopo.

#### Scenario: Registrazione di un beneficiario
- **WHEN** il Tutor registra un beneficiario
- **THEN** l'audit contiene un evento `CREATE` sul beneficiario senza nome, cognome né altri valori personali

#### Scenario: Cambio del referente aziendale
- **WHEN** un operatore cambia il referente di un'azienda
- **THEN** l'audit registra un `UPDATE` con il solo campo `contactPerson` segnato come modificato, senza il vecchio né il
  nuovo nome

