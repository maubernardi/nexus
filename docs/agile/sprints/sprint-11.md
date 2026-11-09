# Sprint 11 — 9 – 11 novembre 2026

**Stato:** 🎉 concluso il 09/11, in un giorno: impegno di 15 punti completato, demo a `34d1301`, collaudo superato
(T-21.7, T-21.8). **Planning:** 08/11, nella [review dello Sprint 10](sprint-10-review.md) (§9); proposta del team
confermata dal Product Owner.

## Obiettivo

> NEXUS è aggiornato e senza vulnerabilità note, resta aggiornato con le PR di Dependabot, le password comuni sono
> rifiutate e il Tutor allega il curriculum del beneficiario.

## Impegno (15 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| EN-6 | Dipendenze aggiornate e senza vulnerabilità note; Dependabot configurato | 8 | 🎉 Done | `update-dependencies` | [#125](https://github.com/maubernardi/nexus/pull/125)–[#133](https://github.com/maubernardi/nexus/pull/133) (9 PR) |
| EN-7 | Password comuni rifiutate (L1 della revisione ASVS) | 2 | 🎉 Done | `add-common-password-blacklist` | [#134](https://github.com/maubernardi/nexus/pull/134) |
| US-205 | Curriculum del beneficiario | 5 | 🎉 Done | `add-beneficiary-cv` | [#135](https://github.com/maubernardi/nexus/pull/135) |

Ordine: EN-6 → EN-7 → US-205. EN-6 per primo (decisione del PO): prima le vulnerabilità, poi le versioni principali,
**ognuna in una PR separata** con la CI verde e la demo deployata e controllata. Una PR per storia; gli stati del backlog
si aggiornano nella PR di documentazione dopo i merge. Prossima migrazione: **V32**.

**Velocità:** 15 punti. Migrazione V32. 9 deploy, tutti riusciti, ognuno controllato (con Chrome quando cambiava
l'interfaccia). Nessun extra pronto: dopo lo sprint restano per R4 il backup (US-1302), la revisione UX/UI (US-1305) e
l'integrazione dei modelli Word (US-1006), questi due in sprint dedicati, e il passaggio in produzione (US-1306).

## Decisioni durante lo sprint

- **EN-6**, aggiornamenti in 9 PR, ciascuna con il suo deploy:
  - Keycloak 26.8 vuole lo scope `kc-admin-api` per il client tecnico: la CI l'ha scoperto prima del deploy;
    `keycloak-admin-api-scope.sh` eseguito dal PO;
  - TypeScript 7 senza API: `tsc` alla 7.0.2, l'API 6 a typescript-eslint (soluzione ufficiale «side by side»);
  - Node 26 LTS e pnpm 12.18; vite-plugin-pwa 2, i18next 27, jsdom 31;
  - dipendenze indirette vulnerabili forzate solo dove serve; `deepmerge` (nessuna correzione, solo build) archiviato
    con l'ok del PO; Dependabot: 0 avvisi;
  - Dependabot configurato (PR ogni lunedì; minori raggruppate, principali una per PR; Java e tipi di Node sulla LTS);
    in CI le immagini dei test devono coincidere con la produzione.
- **EN-7**: invece delle 100 000 password più usate (solo 488 da 12 caratteri in su) le 46 146 da 12 caratteri in su
  tra il milione più usate; messaggio «Password troppo comune».
- **US-205**: il contenuto dei file nel database (scelta del team: il backup lo copre, nessuno spazio su disco da
  configurare).
- **Link doppi nel terminale**: `experimental.detectURLs: false` in Windows Terminal (impostazione del PO).

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
| EN-6 unita (09/11) | 7 |
| EN-7 unita (09/11) | 5 |
| US-205 unita (09/11) | 0 |

## Per il PO durante lo sprint

- T-21.6: il lunedì successivo, le prime PR di Dependabot (da unire una alla volta, con il deploy);
- US-1301 (PWA su smartphone, T-10.1).
