## Why

Storia **US-515** (Sprint 18), chiesta dal PO il 25/11. Alcune aziende sono adatte come **«nave scuola»**: ottime per un
primo tirocinio. Lo sa il Tutor che ha seguito un tirocinio lì, ma oggi nessuno lo segna. L'operatore che sceglie
un'azienda per un beneficiario non lo vede.

## What Changes

- **Report di restituzione**: la domanda facoltativa «L'azienda è adatta come nave scuola?».
- **Proposta del Tutor**, se risponde sì:
  - l'operatore della segnalazione riceve l'avviso nell'app e per email, con il collegamento alla scheda dell'azienda.
    I testi si cambiano da «Testi dei messaggi» (US-1204);
  - la proposta resta anche nel diario dell'azienda.
- **Scheda dell'azienda**: la casella «Nave scuola», che segnano o tolgono operatori e ADMIN, anche senza una proposta.
  Il cambio entra nell'audit.
- **Simbolo 🚢**, un'icona con il testo «Nave scuola», accanto al nome dell'azienda dove la si sceglie:
  - elenco delle aziende del Call Center, con il filtro «Solo le navi scuola»;
  - lista e scheda del Tutor (US-511);
  - abbinamento (mansioni compatibili) e proposta;
  - bacheca del Call Center.

  Non compare nella bacheca dei Tutor, che è anonima.
- **Avvisi verso un'azienda**: un avviso può portare alla scheda di un'azienda invece che alla segnalazione.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `company-registry`: azienda nave scuola nella scheda, nell'elenco e nella lista del Tutor.
- `ticket-lifecycle`: proposta di nave scuola nel report di restituzione.
- `notifications`: avviso della proposta, con il collegamento alla scheda dell'azienda.
- `dev-seed-data`: una nave scuola tra le aziende dimostrative.

## Non-goals

- Un elenco delle proposte da decidere: la proposta arriva come avviso e resta nel diario.
- Il 🚢 nei documenti del tirocinio e nella bacheca dei Tutor.
- Togliere la nave scuola in automatico quando arrivano valutazioni negative: decide l'operatore.

## Impact

- Database: **V55**:
  - `company.training_ship`;
  - `closure_report.training_ship_proposed`;
  - `notification.company_id`, con FK.

  Le colonne booleane nascono false. Il report resta immodificabile: si aggiunge solo una colonna.
- Backend:
  - `Company`, DTO e modulo con la nave scuola, audit, filtro;
  - `JobSlotMatchDTO` con la nave scuola, per abbinamento, proposta e bacheca;
  - elenco del Tutor;
  - report con la proposta;
  - `NotificationType.TRAINING_SHIP_PROPOSED` con i testi predefiniti;
  - notifica ed email con l'azienda;
  - nota nel diario.
- Frontend:
  - atomo `TrainingShipBadge`;
  - casella nel modulo dell'azienda;
  - filtro e simboli negli elenchi;
  - domanda nel report;
  - avviso che apre la scheda dell'azienda;
  - testi in `it.ts`.
- Accessibilità: il simbolo ha il testo «Nave scuola» per il lettore di schermo e il suggerimento al passaggio del
  mouse; la casella ha un'etichetta e un aiuto.
- Collaudo: prove T-39 sul PC del PO prima del merge (EN-11).
