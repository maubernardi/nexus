# Sprint 3 — 24 – 26 ottobre 2026

**Stato:** 🏁 chiuso il 24/10 con la [review](sprint-03-review.md) (21/21 punti, velocity 21).
**Planning:** 24/10, a chiusura della review dello Sprint 2; proposta del team confermata dal
Product Owner ("teniamo tutto"). Primo sprint corto (3 giorni di calendario).

## Obiettivo

> Il Tutor segue le proprie segnalazioni e i propri beneficiari, con la cronologia completa; il Call Center lavora in
> modo condiviso. Il rilascio R1 è chiuso.

## Impegno (21 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| EN-5 | Controllo della numerazione delle migrazioni nella CI | 1 | 🎉 Done | `add-migration-check` | [#29](https://github.com/maubernardi/nexus/pull/29) |
| EN-4 | Contatti dei referenti aziendali nell'audit senza valori | 1 | 🎉 Done | `redact-company-contacts` | [#30](https://github.com/maubernardi/nexus/pull/30) |
| US-504 | Disattivazione impedita con mansioni bloccate | 1 | 🎉 Done | `guard-company-deactivation` | [#32](https://github.com/maubernardi/nexus/pull/32) |
| US-405 | Lavorazione condivisa tra operatori | 2 | 🎉 Done | `share-ticket-work` | [#33](https://github.com/maubernardi/nexus/pull/33) |
| US-303 | Elenco delle segnalazioni (Home) — riscritta con le note operative | 5 | 🎉 Done | `add-tickets-home` | [#35](https://github.com/maubernardi/nexus/pull/35) |
| US-305 | Dettaglio e cronologia del ticket | 3 | 🎉 Done | `add-ticket-detail` | [#36](https://github.com/maubernardi/nexus/pull/36) |
| US-202 | I miei beneficiari | 3 | 🎉 Done | `manage-my-beneficiaries` | [#37](https://github.com/maubernardi/nexus/pull/37) |
| US-203 | Modifica beneficiario | 2 | 🎉 Done | `manage-my-beneficiaries` | [#37](https://github.com/maubernardi/nexus/pull/37) |
| US-204 | Visibilità dei beneficiari per progetto | 3 | 🎉 Done | `manage-my-beneficiaries` | [#37](https://github.com/maubernardi/nexus/pull/37) |
| US-304 | Ticket dei miei progetti — ↪️ assorbita in US-303 | 0 | — | — | — |

Ordine di lavoro proposto: prima le voci piccole che riducono rischio (EN-5, EN-4, US-504, US-405), poi il lato Tutor.
PR una per storia (o piccolo gruppo coeso) su `main`. Per il PO, fuori dallo sviluppo: US-1301 (PWA su smartphone).

Fuori piano: suggerimenti dei campi in un'icona "i" con tooltip, al posto del testo sotto l'etichetta che disallineava
i campi (richiesta del PO, change `field-hint-tooltips`, [#34](https://github.com/maubernardi/nexus/pull/34)).

## Note operative del PO

Discusse il 24/10: decisioni in [sprint-03-note-operative.md](sprint-03-note-operative.md).

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (24/10) | 22 |
| 24/10 (note operative: US-303 a 5 punti, US-304 assorbita) | 21 |
| 24/10 sera (#29–#37 unite) | 0 |
| Fine sprint (24/10, chiuso in review) | 0 |

## Obiettivo e rilascio R1

- Raggiunto: il Tutor ha la Home con le proprie segnalazioni e quelle dei suoi progetti, il dettaglio con la cronologia,
  l'elenco, la scheda e la modifica dei propri beneficiari; il Call Center lavora in modo condiviso.
- **R1 non ancora chiuso**: le note operative hanno aggiunto a R1 US-503, US-505 e US-604 (10 punti), che si sommano a
  US-1301 (1 punto, verifica della PWA a cura del PO). Restano 11 punti, candidati naturali dello Sprint 4.

## Review, retrospettiva e planning successivo

[sprint-03-review.md](sprint-03-review.md) (documento unico asincrono, metodo dal 05/10).
