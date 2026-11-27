# Sprint 19 — 27 – 29 novembre 2026

**Stato:** 🔨 in corso dal 27/11.
**Planning:** 27/11, dalla [review dello Sprint 18](sprint-18-review.md) (§8, §9).

## Obiettivo

> Il Tutor chiede al Call Center di contattare un'azienda e segue la richiesta fino all'esito; gli avvisi su sito e
> backup arrivano in tempo.

## Impegno (13 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| US-507 | Richiesta di contatto di un'azienda dal Tutor | 5 | ✅ Ready | — | — |
| US-512 | Tentativi di contatto ed esito della richiesta | 5 | ✅ Ready | — | — |
| US-1311 | Avvisi affidabili su sito e backup | 3 | ✅ Ready | — | — |

**Ordine** (proposta della review, §9):
1. **US-507, poi US-512**: la seconda usa le richieste della prima.
2. **US-1311**: UptimeRobot per il sito e Healthchecks.io per il backup (PO, 27/11). Gli account li crea il PO; il
   segnale del backup va solo nel `.env` del server.

Sotto la media (23,2), per lasciare spazio a quello che arriverà dall'uso.

## Decisioni del planning (27/11)

- **Sprint 18 chiuso** con velocity 23 (review, §8.1).
- **Avvisi** (review, §8.2): **B**, UptimeRobot e Healthchecks.io. UptimeRobot gratis non controlla la risposta 401
  dell'API: NEXUS avrà un indirizzo pubblico che dice solo «attivo», senza dati.
- **Azzeramento della pre-produzione** (review, §8.3): nuova storia US-1313, 5 punti, da pianificare quando il PO
  fissa la data. Ripartono da 1 anche i contatori (numero delle segnalazioni, annunci della bacheca e quelli nuovi),
  che il passaggio del 24/11 non aveva azzerato (PO, 27/11).

## Come li faccio

- Ogni storia: una change OpenSpec, una PR verso `main` (A45), il collaudo sul PC del PO prima del merge (EN-11).
- Prossima migrazione: **V56**, probabilmente una per US-507 e una per US-512.
- Elementi nuovi negli elenchi provati con testi lunghi come quelli veri, in chiaro e in scuro (A47).
- Nello sprint, senza punti: la ricerca dei dati che si inseriscono ma non si correggono dall'app (A42).

## Per il PO

- [ ] Creare gli account di UptimeRobot e Healthchecks.io, quando arriva US-1311.
- [ ] Collaudo di ogni storia sul PC prima del merge.

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (27/11) | 13 |
