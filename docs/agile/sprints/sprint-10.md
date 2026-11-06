# Sprint 10 — 7 – 9 novembre 2026

**Stato:** 🔨 in corso. **Planning:** 06/11, nella [review dello Sprint 9](sprint-09-review.md) (§9); proposta del team
confermata dal Product Owner.

## Obiettivo

> NEXUS è più sicuro per i dati reali: il deploy si ripristina da solo se una release non parte, chi dimentica la
> password la reimposta da solo, gli accessi sono registrati, i dati si anonimizzano alla scadenza e un avviso dice se
> l'app non risponde.

## Impegno (19 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| A16 | Ripristino del deploy, provato di proposito (azione, senza punti) | — | 🎉 Done (06/11) | — | [#110](https://github.com/maubernardi/nexus/pull/110), [#111](https://github.com/maubernardi/nexus/pull/111) |
| US-1308 | Recupero password | 2 | ✅ Ready | — | — |
| US-113 | Registro degli accessi, con la pagina dei tentativi falliti | 7 | ✅ Ready | — | — |
| US-1303 | Anonimizzazione a scadenza | 5 | ✅ Ready | — | — |
| US-1307 | Monitoraggio (8–20) e revisione di sicurezza | 5 | ✅ Ready | — | — |

**Extra:** US-205 *Curriculum del beneficiario* (5).

Ordine: A16 → US-1308 → US-113 → US-1303 → US-1307. Una PR per storia; gli stati del backlog si aggiornano nella PR di
documentazione dopo i merge. US-113 richiede un passo sul server (lista completa nella PR, A15). US-1302 *Backup* resta
nel backlog (review dello Sprint 9, §8.6). Prossima migrazione: **V31** (la V30 è quella vuota della prova di A16).

## Decisioni del planning (review dello Sprint 9)

- **US-112** (§8.1): confermate le scelte del team sul Coordinatore.
- **A16** (§8.3): prova del ripristino sulla demo a qualunque ora; fatta il 06/11, riuscita.
- **Registro degli accessi** (§8.4): registra Keycloak, NEXUS mostra; con la pagina dell'ADMIN di tutti i tentativi
  falliti (US-113 da 5 a 7 punti); conservazione 12 mesi.
- **Collaudo** (§8.5): prima la sezione 20, poi le sezioni 16–17.
- **Monitoraggio** (§8.7): controllo dalla CI di GitHub ogni 10 minuti, solo dalle 8 alle 20.

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (06/11, sera; A16 già fatta) | 19 |

## Per il PO durante lo sprint

- collaudo: sezione 20 (T-20.2–T-20.8), poi T-13.4, sezioni 16–17, T-19.2;
- US-1301 (PWA su smartphone, T-10.1).
