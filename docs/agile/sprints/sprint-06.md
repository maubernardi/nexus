# Sprint 6 — 28 – 30 ottobre 2026

**Stato:** 🔨 in corso. **Planning:** 28/10, nella [review dello Sprint 5](sprint-05-review.md) (§9); proposta del team
confermata dal Product Owner.

## Obiettivo

> La bacheca è operativa: il Call Center pubblica annunci (da una mansione libera o dal rilancio dopo un appuntamento
> fallito) e il Tutor li consulta senza vedere l'azienda.

## Impegno (20 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| US-307 | Scadenza nella Home del Tutor | 2 | ✅ Ready | | |
| US-902 | Gestione della bacheca (bozze, pubblicazione, archiviazione) | 5 | ✅ Ready | | |
| US-904 | Annuncio da una mansione libera | 3 | ✅ Ready | | |
| US-901 | Bacheca per il Tutor | 3 | ✅ Ready | | |
| US-803 | Rilancio in bacheca dopo l'appuntamento fallito | 5 | ✅ Ready | | |
| US-903 | Sblocco a tutti dopo 7 giorni | 2 | ✅ Ready | | |

**Extra**, se avanza tempo: US-306 *Segnalazione speciale dalla bacheca* (3 punti).

Ordine: US-307 → US-902 → US-904 → US-901 → US-803 → US-903. Una PR per storia;
gli stati del backlog si aggiornano nella PR di documentazione dopo i merge. Per il PO: US-1301 (PWA su smartphone,
collaudo T-10.1).

## Decisioni del planning (review dello Sprint 5)

- Rilancio: casella "Chiedi il rilancio in bacheca" nel modulo dell'appuntamento fallito; crea una bozza precompilata
  dalla mansione, riservata al progetto della segnalazione e collegata a essa; il Call Center riceve una notifica e
  decide se pubblicarla.
- Il Tutor non riceve mai il nome dell'azienda, né il **titolo** e le **note** dell'annuncio (possono contenerlo): solo
  numero (#N), tipologia, zona, fascia d'età, orario, durata e "solo per il tuo progetto" per i post riservati.
- Dalla nota operativa del PO (§8.3, 28/10): nuova storia US-307, scadenza del timer nella Home del Tutor (2 punti), aggiunta all'impegno come primo lavoro.

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (28/10) | 20 |
| Fine sprint | — |

## Review, retrospettiva e planning successivo

In un unico documento asincrono a fine sprint.
