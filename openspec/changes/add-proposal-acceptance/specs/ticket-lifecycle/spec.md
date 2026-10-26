## ADDED Requirements

### Requirement: Accettazione della proposta
Il Tutor della segnalazione SHALL poter accettare la proposta di una segnalazione in `PROPOSTA_AZIENDA`, previa
conferma: la segnalazione passa in `PROPOSTA_ACCOLTA` e parte un timer che scade alla fine del 7° giorno lavorativo
successivo, secondo il calendario unico. La scadenza SHALL essere visibile nel dettaglio a chi vede la segnalazione.
L'accettazione SHALL essere registrata nell'audit e nello storico dell'azienda. Altri utenti MUST NOT poter accettare.

#### Scenario: Accettazione
- **WHEN** il Tutor della segnalazione accetta la proposta martedì 27 ottobre 2026
- **THEN** lo stato è "Proposta accolta", la scadenza del timer è giovedì 5 novembre 2026, la cronologia mostra
  "Proposta accettata" con la scadenza e lo storico dell'azienda conta una proposta accettata

#### Scenario: Collega di progetto o Call Center
- **WHEN** un collega di progetto o un operatore apre la segnalazione
- **THEN** non trova il comando di accettazione e l'API risponde `403`

#### Scenario: Accettazione già avvenuta o versione superata
- **WHEN** il Tutor accetta una proposta già accettata o partendo da una versione superata
- **THEN** il sistema risponde `409`
