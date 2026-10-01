# Sprint 1 — 30 settembre – 1 ottobre 2026

**Stato:** ✅ chiuso il 01/10 (in anticipo sul 06/10, decisione del PO). **Planning:** 30/09, proposta del team confermata dal Product Owner.

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

Le PR erano in cascata (ognuna costruita sulla precedente) e sono state unite nell'ordine della tabella.
Per il PO, fuori dallo sviluppo: US-1301 (verifica PWA su smartphone) e le domande aperte Q1–Q4, Q8, Q9 (Q12 risolta il 01/10).

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (30/09) | 21 |
| 01/10 (merge e deploy di #12–#16) | 0 |
| Fine sprint (01/10) | 0 |

## Sprint Review

**Obiettivo raggiunto**: sulla demo il Tutor registra un beneficiario e lo segnala, e la segnalazione compare nella coda
del Call Center. **Velocity: 21 punti** (5 storie Done), più la correzione non pianificata del PO.

- Consegnato e online: registrazione del beneficiario, motore delle transizioni con cronologia e 409 sui conflitti,
  invio della segnalazione, coda FIFO con fast-track e filtri, pagina "Account non abilitato" (PR #12–#16, deploy 01/10).
- Correzione del PO durante lo sprint: "beneficiario" al posto di "candidato" ovunque (codice, API, database) e una sola
  segnalazione aperta per beneficiario (Q12).
- Decisioni del PO raccolte: Q1–Q13 (Q5 in parte aperta); nuove voci EN-2 (audit), EN-3 (zone di Firenze), US-107
  (calendario del progetto); US-106 anticipata a R2.
- Non fatto: US-1301 (verifica della PWA su smartphone, a cura del PO), rinviata.

## Retrospettiva

**Bene**
- La verifica in browser reale ha trovato due difetti che i test unitari non vedevano (focus sul riepilogo errori dopo
  un clic; ciclo infinito di richieste a `/me` per l'utente disattivato).
- Le domande aperte, con una proposta per ciascuna, hanno permesso al PO di decidere in fretta.

**Da migliorare**
- Terminologia: "candidato" era un'interpretazione nostra del brief. Azione: glossario in `CLAUDE.md` e conferma dei
  termini nuovi con il PO prima di usarli nel codice.
- PR in cascata: hanno confuso (proposta di "stack" da GitHub). Azione: PR impilate solo se una storia dipende da una
  non ancora unita, spiegandolo nella PR; altrimenti tutte su `main`.
- Ogni PR riporta il link completo (richiesta del PO).
