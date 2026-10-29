## ADDED Requirements

### Requirement: Approvazione della corsia veloce
Il Call Center e l'ADMIN SHALL poter approvare la corsia veloce di una segnalazione speciale presa in carico. In
un'unica operazione la segnalazione passa a `PROPOSTA_AZIENDA` con la mansione dell'annuncio, la mansione si blocca,
l'annuncio si archivia e il Tutor riceve la notifica della proposta. La corsia veloce è consumata, il tipo SPECIAL
resta. Finché la corsia veloce aspetta la decisione, la segnalazione MUST NOT essere abbinata a un'altra mansione.

#### Scenario: Approvazione
- **WHEN** l'operatore che ha preso in carico la segnalazione speciale approva la corsia veloce e conferma
- **THEN** la segnalazione è una proposta all'azienda dell'annuncio, la mansione è bloccata, l'annuncio è archiviato
  e il Tutor riceve la notifica

#### Scenario: Mansione non più disponibile
- **WHEN** la mansione dell'annuncio è stata ritirata o bloccata, oppure l'annuncio non è più in bacheca
- **THEN** l'approvazione è rifiutata con il motivo e nulla cambia

#### Scenario: Abbinamento con la corsia veloce in attesa
- **WHEN** l'operatore prova ad abbinare un'altra mansione prima di decidere sulla corsia veloce
- **THEN** l'abbinamento è rifiutato: prima si decide sulla corsia veloce
