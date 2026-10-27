# Sprint 5 — 27 – 29 ottobre 2026

**Stato:** ✅ impegno completato il 27/10 (21/21 punti), in attesa della review. **Planning:** 27/10, nella [review dello Sprint 4](sprint-04-review.md) (§9); proposta del
team confermata dal Product Owner.

## Obiettivo

> Dopo l'accettazione il percorso prosegue: promemoria e scadenza del timer, rifiuto della proposta, esito
> dell'appuntamento; Tutor e operatori ricevono le notifiche in app.

## Impegno (21 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| US-1201 | Notifiche in app | 5 | 🎉 Done | `add-in-app-notifications` | [#53](https://github.com/maubernardi/nexus/pull/53) |
| US-801 | Appuntamento preso | 2 | 🎉 Done | `add-appointment-set` | [#54](https://github.com/maubernardi/nexus/pull/54) |
| US-802 | Appuntamento fallito, con motivo e gestione delle segnalazioni riaperte | 3 | 🎉 Done | `add-appointment-failed` | [#55](https://github.com/maubernardi/nexus/pull/55) |
| US-702 | Rifiuto della proposta | 3 | 🎉 Done | `add-proposal-reject` | [#56](https://github.com/maubernardi/nexus/pull/56) |
| US-704 | Promemoria al 4° giorno | 3 | 🎉 Done | `add-timer-reminder` | [#57](https://github.com/maubernardi/nexus/pull/57) |
| US-705 | Scadenza del timer | 3 | 🎉 Done | `add-timer-expiry` | [#59](https://github.com/maubernardi/nexus/pull/59) |
| US-603 | Esclusione di un'azienda | 2 | 🎉 Done | `add-company-exclusion` | [#60](https://github.com/maubernardi/nexus/pull/60) |

Ordine: US-1201 → US-801 → US-802 → US-702 → US-704 → US-705 → US-603. Una PR per storia; gli stati del backlog si
aggiornano nella PR di documentazione dopo i merge. Per il PO: US-1301 (PWA su smartphone) e le note operative del
25/10 (azione A5).

## Decisioni del planning (review dello Sprint 4)

- Segnalazioni riaperte: restano in carico allo stesso operatore, in cima a "In carico a me" con l'evidenza "Riaperta";
  nessuna esclusione automatica dopo un appuntamento fallito, resta quella al rifiuto della proposta (Q3).
- Motivo dell'appuntamento fallito: causa dal menu (voci provvisorie) e nota, obbligatoria con "Altro".
- Notifiche solo in app, alla controparte del passaggio; l'email resta in US-1202.
- Timer: controllo ogni ora; scadenza alla fine del 7° giorno lavorativo, ora di Roma.

Fuori impegno, su richiesta del PO: documento di collaudo passo per passo
([#58](https://github.com/maubernardi/nexus/pull/58), `docs/test/collaudo.md`, Definition of Done punto 8).

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (27/10) | 21 |
| 27/10 (US-1201, #53) | 16 |
| 27/10 (US-801, #54) | 14 |
| 27/10 (US-802, #55) | 11 |
| 27/10 (US-702, #56) | 8 |
| 27/10 (US-704, #57) | 5 |
| 27/10 (US-705, #59) | 2 |
| 27/10 (US-603, #60) | 0 |
| Fine sprint | — |

## Review, retrospettiva e planning successivo

In un unico documento asincrono a fine sprint.
