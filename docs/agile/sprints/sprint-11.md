# Sprint 11 — 9 – 11 novembre 2026

**Stato:** 🔨 in corso. **Planning:** 08/11, nella [review dello Sprint 10](sprint-10-review.md) (§9); proposta del team
confermata dal Product Owner.

## Obiettivo

> NEXUS è aggiornato e senza vulnerabilità note, resta aggiornato con le PR di Dependabot, le password comuni sono
> rifiutate e il Tutor allega il curriculum del beneficiario.

## Impegno (15 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| EN-6 | Dipendenze aggiornate e senza vulnerabilità note; Dependabot configurato | 8 | ✅ Ready | — | — |
| EN-7 | Password comuni rifiutate (L1 della revisione ASVS) | 2 | ✅ Ready | — | — |
| US-205 | Curriculum del beneficiario | 5 | ✅ Ready | — | — |

Ordine: EN-6 → EN-7 → US-205. EN-6 per primo (decisione del PO): prima le vulnerabilità, poi le versioni principali,
**ognuna in una PR separata** con la CI verde e la demo deployata e controllata. Una PR per storia; gli stati del backlog
si aggiornano nella PR di documentazione dopo i merge. Prossima migrazione: **V32**.

## Decisioni del planning (review dello Sprint 10)

- **Versioni sempre aggiornate** (§8.2): ultima stabile; per Java e Node l'ultima LTS (Node 26, Java 25); PR di
  Dependabot da unire al PO; versioni principali in PR separate. Regola in `CLAUDE.md`.
- **Password comuni** (§8.3): policy `passwordBlacklist` di Keycloak con le 100 000 password più usate.
- **Backup** (§8.4): US-1302 resta nel backlog.
- **Sprint dedicati più avanti**: revisione UX/UI (US-1305) e integrazione dei modelli Word reali (US-1006).
- **Interfaccia** (§8.6): controllo con l'estensione di Chrome prima di consegnare (A20); nella demo niente password.

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (08/11) | 15 |

## Per il PO durante lo sprint

- unire le PR di EN-6 una alla volta (ognuna con il suo deploy) e poi quelle di Dependabot;
- US-1301 (PWA su smartphone, T-10.1).
