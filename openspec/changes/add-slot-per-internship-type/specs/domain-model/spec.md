## MODIFIED Requirements

### Requirement: Mansione e blocco
Ogni mansione SHALL appartenere a un'azienda, a una tipologia del catalogo e a una zona, con stato `LIBERA` o
`BLOCCATA`. Una mansione SHALL essere assegnata a più segnalazioni, al massimo una per tipo di tirocinio: il database
MUST rifiutare la seconda assegnazione dello stesso tipo. Una segnalazione MUST essere assegnata al più a una mansione.
Lo stato SHALL essere `BLOCCATA` se e solo se la mansione ha almeno un'assegnazione, e la mansione MUST riferire una
delle segnalazioni assegnate.

#### Scenario: Mansione bloccata senza ticket
- **WHEN** si imposta una mansione a `BLOCCATA` senza indicare il ticket
- **THEN** l'operazione viene rifiutata

#### Scenario: Stesso ticket su due mansioni
- **WHEN** si tenta di far bloccare a un ticket una seconda mansione
- **THEN** l'operazione viene rifiutata

#### Scenario: Due assegnazioni dello stesso tipo
- **WHEN** si assegna la stessa mansione a due segnalazioni dello stesso tipo di tirocinio
- **THEN** il database rifiuta la seconda (`uq_ticket_job_slot_internship_type`)
