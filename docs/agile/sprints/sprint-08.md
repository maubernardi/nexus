# Sprint 8 — 30 ottobre – 1 novembre 2026

**Stato:** 📋 pronto a partire. **Planning:** 29/10, nella [review dello Sprint 7](sprint-07-review.md) (§9); proposta
del team confermata dal Product Owner, con le email nell'impegno.

## Obiettivo

> Il percorso arriva in fondo: il Tutor avvia il tirocinio, scarica Convenzione e Progetto Formativo compilati e chiude
> con il report di restituzione; la mansione torna disponibile se l'azienda lo dice. Le notifiche importanti arrivano
> anche per email.

## Impegno (23 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| US-1003 | Avvio del tirocinio (scheda completa) | 1 | ✅ Ready | | |
| US-1004 | Convenzione in Word (modelli segnaposto per tipo) | 5 | ✅ Ready | | |
| US-1005 | Progetto Formativo in Word | 3 | ✅ Ready | | |
| US-109 | Cataloghi del report di restituzione | 2 | ✅ Ready | | |
| US-1101 | Report di restituzione | 5 | ✅ Ready | | |
| US-1102 | Esito sulla mansione e audit trail | 2 | ✅ Ready | | |
| US-1202 | Email transazionali (SMTP IONOS) | 5 | ✅ Ready | | |

**Extra**, nell'ordine: US-205 *Curriculum del beneficiario* (5), US-102 *Gestione progetti* (3). I punti non sono un
tetto: «preferisco di più che di meno» (PO, 28/10).

Ordine: azione A13 (test lenti) → US-1003 → US-1004 → US-1005 → US-109 → US-1101 → US-1102 → US-1202. Una PR per
storia; gli stati del backlog si aggiornano nella PR di documentazione dopo i merge. Per il PO: collaudo (sezioni
12–15), US-1301 e i dati SMTP.

## Decisioni del planning (review dello Sprint 7)

- **Scheda completa per l'avvio** (§8.2): tutto tranne sospensioni e note, legale rappresentante con i dati completi
  compreso; se manca qualcosa la pagina dice cosa e porta al campo. La scheda resta modificabile dopo l'avvio.
- **Modelli Word** (§8.3): segnaposto nel codice, uno per tipo di tirocinio; la modifica dei modelli in una storia a
  parte, quando il PO avrà deciso come.
- **Voci del report** (§8.4): formule di assunzione e motivi della mancata assunzione come cataloghi dell'ADMIN (US-109).
- **Email**: i dati SMTP arrivano dal PO. Server, porta, utente e mittente vanno nella configurazione; la password solo
  nel `.env` del server, mai nel repository né in chat.

## Decisioni durante lo sprint

—

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (30/10) | 23 |
| Fine sprint | — |

## Review, retrospettiva e planning successivo

In un unico documento asincrono a fine sprint.
