# Sprint 4 — 25 – 27 ottobre 2026

**Stato:** ✅ impegno completato il 27/10 (24/24 punti), in attesa della [review](sprint-04-review.md). **Planning:** 24/10, nella [review dello Sprint 3](sprint-03-review.md) (§9); proposta del
team confermata dal Product Owner, con il calendario unico per tutti i progetti.

## Obiettivo

> L'operatore sceglie la proposta conoscendo storico e note dell'azienda (R1 chiuso); il Tutor accetta la proposta e
> parte il timer di 7 giorni lavorativi.

## Prima dello sprint (senza punti)

- Decisioni della review: "Nuovo beneficiario" e "Nuova segnalazione" come pulsanti in cima agli elenchi, fuori dal
  menu (§8.1); nome del beneficiario come link alla scheda nel dettaglio della segnalazione (§8.3).
- Deploy della demo con il lavoro dello Sprint 3 (PO, azione A1).

Fatto il 25/10: decisioni §8.1 e §8.3 in [#40](https://github.com/maubernardi/nexus/pull/40) (change
`review-sprint-03-navigation`); demo aggiornata a `0323f2e` e poi a `54ebf99` (Sprint 3, #40, US-505).

## Impegno (24 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| US-505 | Diario delle note dell'azienda | 2 | 🎉 Done | `add-company-notes` | [#42](https://github.com/maubernardi/nexus/pull/42) |
| US-503 | Storico e pregressi dell'azienda | 3 | 🎉 Done | `add-company-history` | [#44](https://github.com/maubernardi/nexus/pull/44) |
| US-604 | Dettagli di azienda e mansione durante l'abbinamento | 5 | 🎉 Done | `add-match-details` | [#45](https://github.com/maubernardi/nexus/pull/45) |
| US-107 | Calendario unico (patrono e chiusure) | 3 | 🎉 Done | `add-work-calendar` | [#47](https://github.com/maubernardi/nexus/pull/47) |
| US-703 | Calendario dei giorni lavorativi | 3 | 🎉 Done | `add-working-days` | [#48](https://github.com/maubernardi/nexus/pull/48) |
| US-701 | Accettazione della proposta | 3 | 🎉 Done | `add-proposal-acceptance` | [#49](https://github.com/maubernardi/nexus/pull/49) |
| US-302 | Nuova tipologia di mansione dal Tutor, approvata dal Call Center | 5 | 🎉 Done | `add-new-job-category` | [#51](https://github.com/maubernardi/nexus/pull/51) |

US-302 aggiunta all'impegno il 25/10 su richiesta del PO (nota operativa 3), senza togliere altre storie: impegno sopra i
~20 punti abituali. Notifica al Tutor con la soluzione provvisoria decisa in review (§8.2). Per il PO: US-1301 (PWA su
smartphone).

Demo aggiornata il 26/10 a `d36f167` (US-503, US-604). Sviluppo di **R1 completo** il 26/10 con US-604; per chiudere il rilascio resta US-1301 (PO). "Esiti negativi" del riepilogo = tirocini conclusi senza assunzione (#44, design D1): confermato dal PO in review.

Decisione del PO (27/10, US-701): accetta la proposta **solo il Tutor della segnalazione**; i tutor, di norma, non sono
intercambiabili come gli operatori del Call Center.

Ordine: US-505 → US-503 → US-604 (chiude R1), poi US-107 → US-703 → US-701, infine US-302. Una PR per storia (o piccolo gruppo
coeso); gli stati del backlog si aggiornano nella PR di documentazione dopo i merge.

## Burndown

| Momento | Punti residui |
|---|---|
| Planning (24/10) | 19 |
| 25/10 (US-302 aggiunta dal PO) | 24 |
| 25/10 (US-505 unita, #42) | 22 |
| 25/10 (US-503 unita, #44) | 19 |
| 26/10 (US-604 unita, #45: sviluppo di R1 completo) | 14 |
| 26/10 (US-107 unita, #47) | 11 |
| 26/10 (US-703 unita, #48) | 8 |
| 27/10 (US-701 unita, #49) | 5 |
| 27/10 (US-302 unita, #51) | 0 |
| Fine sprint | — |

## Review, retrospettiva e planning successivo

[sprint-04-review.md](sprint-04-review.md) (documento unico asincrono).
