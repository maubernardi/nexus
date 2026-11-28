## ADDED Requirements

### Requirement: Correzione del progetto e della mansione richiesta
Dopo l'invio solo il Tutor della segnalazione SHALL poter correggere il progetto e la mansione richiesta, finché la
segnalazione non ha una proposta all'azienda: nuova, in attesa dell'approvazione della tipologia o in lavorazione. Call
Center e ADMIN MUST NOT poterli correggere, né il Tutor dopo la prima proposta o in una candidatura dalla bacheca.
- Il progetto SHALL essere uno dei progetti del Tutor. Se cambia, la sede di competenza e il tipo di tirocinio SHALL
  valere per il progetto nuovo: la sede tra le sue sedi attive, obbligatoria se ne ha; il tipo tra quelli ammessi.
- La mansione richiesta SHALL essere una tipologia attiva del catalogo; se la segnalazione aspetta l'approvazione della
  nuova tipologia, si corregge il testo libero.

La correzione SHALL entrare nell'audit con prima e dopo e comparire nello storico della segnalazione.

#### Scenario: Progetto e sede
- **WHEN** il Tutor sposta una segnalazione nuova sul suo progetto POLIS e sceglie una sede di POLIS
- **THEN** la segnalazione è di POLIS con la sede nuova, e lo storico mostra la correzione con prima e dopo

#### Scenario: Sede non del progetto
- **WHEN** il Tutor cambia progetto e lascia una sede che il progetto nuovo non ha
- **THEN** la correzione non si salva e il campo della sede è indicato

#### Scenario: Dopo la proposta
- **WHEN** la segnalazione ha già una proposta all'azienda
- **THEN** il Tutor non può più correggere progetto e mansione

#### Scenario: Altri ruoli
- **WHEN** un operatore del Call Center o l'ADMIN prova a correggere progetto e mansione
- **THEN** riceve un rifiuto
