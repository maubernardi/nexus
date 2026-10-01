# Sprint 1 — 30 settembre – 6 ottobre 2026

**Stato:** 🔨 in corso. **Planning:** 30/09, proposta del team confermata dal Product Owner.

## Obiettivo

> Il Tutor registra un beneficiario e lo segnala; la segnalazione compare nella coda del Call Center.

## Impegno (21 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| US-201 | Inserimento beneficiario | 5 | 🎉 Done | `add-beneficiary-registration` | #12 |
| EN-1 | Motore delle transizioni del ticket | 5 | 🎉 Done | `add-ticket-submission` | #13 |
| US-301 | Nuova segnalazione | 5 | 🎉 Done | `add-ticket-submission` | #13 |
| US-401 | Coda FIFO | 5 | 🎉 Done | `add-cc-queue` | #14 |
| US-101 | Messaggio "utente non abilitato" | 1 | 🎉 Done | `add-not-enabled-page` | #15 |

**Correzione del PO (01/10)**: "beneficiario" al posto di "candidato" ovunque e una sola segnalazione aperta per beneficiario (Q12) → change `adopt-beneficiary-single-open-ticket`, PR #16, in coda alle altre.

Le PR sono in cascata (ognuna costruita sulla precedente) e vanno unite nell'ordine della tabella.
Per il PO, fuori dallo sviluppo: US-1301 (verifica PWA su smartphone) e le domande aperte Q1–Q4, Q8, Q9 (Q12 risolta il 01/10).

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (30/09) | 21 |
| 01/10 (merge e deploy di #12–#16) | 0 |
| Fine sprint | — |

## Sprint Review

—

## Retrospettiva

—
