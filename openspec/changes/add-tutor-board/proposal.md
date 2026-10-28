## Why

Storia **US-901** (Sprint 6): il Tutor consulta gli annunci aperti a tutti e quelli dei suoi progetti, per proporre
beneficiari adatti. Il nome dell'azienda non arriva mai al suo browser. Due decisioni del PO:
- (review dello Sprint 5, §8.2) il Tutor non vede né titolo né note, che potrebbero contenere il nome dell'azienda;
- (28/10) un annuncio vive solo finché la sua mansione è disponibile.

## What Changes

- `GET /api/v1/board`, solo per il Tutor:
  - annunci **pubblicati**, per tutti o riservati ai **suoi progetti**, dal più recente;
  - filtri facoltativi `zoneId` e `jobCategoryId`;
  - DTO dedicato (`TutorBoardPostDTO`): numero, tipologia, zona, età, ore, durata, progetto riservatario, data.
    **Mai** azienda, mansione, titolo né note.
- Archiviazione automatica degli annunci attivi quando la mansione non è più disponibile, con il motivo nell'audit:
  - abbinamento a una segnalazione: "Mansione abbinata alla segnalazione n. N";
  - ritiro della mansione: "Mansione ritirata";
  - disattivazione dell'azienda: "Azienda disattivata".
- Frontend:
  - voce "Bacheca" anche per il Tutor; `/bacheca` mostra la consultazione al Tutor e la gestione a Call Center e ADMIN;
  - schede pensate per lo smartphone, con il riservato indicato in testo e i filtri nell'indirizzo;
  - ritirando una mansione con annuncio, l'esito dice che l'annuncio è stato archiviato.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `job-board`: bacheca del Tutor e archiviazione automatica.
