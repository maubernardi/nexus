# Sprint 20 — 28 – 30 novembre 2026

**Stato:** 🔨 in corso dal 28/11.
**Planning:** 27/11, dalla [review dello Sprint 19](sprint-19-review.md) (§8, §9).

## Obiettivo

> I dati inseriti per sbaglio si correggono dall'app, senza chiedere a nessuno di intervenire sul database.

## Impegno (8 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| US-309 | Correzione del progetto e della mansione richiesta di una segnalazione | 3 | 🔨 In corso | `add-ticket-request-correction` | — |
| US-804 | Correzione dell'appuntamento registrato | 2 | ✅ Ready | — | — |
| US-606 | Togliere l'esclusione di un'azienda per una segnalazione | 3 | ✅ Ready | — | — |

Le tre storie vengono dalla ricerca A42 (appendice della review dello Sprint 19). La quarta correzione, i dati della
richiesta di contatto, si propone dopo, se serve.

**Ordine** (proposta della review, §9): US-309, US-804, US-606. All'inizio di ognuna ti riassumo in tre righe i
percorsi principali (A49).

## Decisioni del planning (27/11)

- **Sprint 19 chiuso** con velocity 10 (review, §8.1).
- **Sprint 20 con le correzioni di A42** (review, §8.2, A). Il PO ha molte cose nuove da aggiungere: la raccolta (C)
  si fa, e le storie nuove entrano nel backlog per gli sprint successivi.
- **Azzeramento della pre-produzione** (review, §8.3): siamo ancora nella fase di brainstorming, tutto resta com'è.
  US-1313 resta nel backlog, senza data.

## Come li faccio

- Ogni storia: una change OpenSpec, una PR verso `main` (A45), il collaudo sul PC del PO prima del merge (EN-11).
- Prossima migrazione: **V58**, probabilmente solo per US-606 (lo stato «tolta» delle esclusioni).
- Le azioni senza punti stanno nel burndown (A50): qui la raccolta delle cose nuove (C).

## Per il PO

- [ ] Raccontarmi le cose nuove da aggiungere (review, §8.2, C).
- [ ] Collaudo di ogni storia sul PC prima del merge.

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (28/11) | 8 |
| Senza punti: raccolta delle cose nuove (C) | da fare |
