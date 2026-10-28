# Sprint 7 — 29 – 31 ottobre 2026

**Stato:** 🔨 in corso. **Planning:** 28/10, nella [review dello Sprint 6](sprint-06-review.md) (§9); proposta del team
confermata dal Product Owner.

## Obiettivo

> Si chiude R2: il Tutor candida un beneficiario a un annuncio e il Call Center approva o rifiuta la corsia veloce;
> l'ADMIN gestisce zone e tipologie; inizia la scheda del tirocinio.

## Impegno (19 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| US-306 | Segnalazione speciale dalla bacheca | 3 | ✅ Ready | | |
| US-403 | Approvazione fast-track | 5 | ✅ Ready | | |
| US-404 | Rifiuto fast-track | 3 | ✅ Ready | | |
| US-106 | Cataloghi di zone e tipologie | 3 | ✅ Ready | | |
| US-1001 | Scheda amministrativa del tirocinio (campi segnaposto) | 5 | ✅ Ready | | |

**Extra**, nell'ordine: US-1002 *Griglia oraria e sospensioni* (5), US-1003 *Avvio del tirocinio* (1). I punti non sono
un tetto: «preferisco di più che di meno» (PO, 28/10).

Ordine: preparazione comune dei test (azione A7) → US-306 → US-403 → US-404 → US-106 → US-1001. Una PR per storia; gli
stati del backlog si aggiornano nella PR di documentazione dopo i merge. Per il PO: collaudo (T-5.8, T-5.9, sezione 11)
e US-1301.

## Decisioni del planning (review dello Sprint 6)

- **Una sola candidatura per annuncio**: la prima lo blocca finché il Call Center non decide. Se approva, l'annuncio
  si archivia; se rifiuta la corsia veloce, torna candidabile. In bacheca l'annuncio bloccato mostra «Candidatura in
  valutazione».
- Candidatura dalla bacheca: "Candida un beneficiario"; si sceglie tra i beneficiari del Tutor senza segnalazioni
  aperte; la segnalazione ha la tipologia della mansione dell'annuncio.
- Se l'annuncio si archivia mentre la candidatura aspetta, la segnalazione perde la corsia veloce con il motivo e il
  Tutor riceve una notifica (interpretazione del team).
- US-1001 con campi segnaposto (Q5); le email (US-1202) quando ci saranno le credenziali SMTP.

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (29/10) | 19 |
| Fine sprint | — |

## Review, retrospettiva e planning successivo

In un unico documento asincrono a fine sprint.
