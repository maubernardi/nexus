# Sprint 8 — 30 ottobre – 1 novembre 2026

**Stato:** 🎉 concluso il 04/11: impegno di 23 punti completato, tutte le storie Must di R3 fatte. Oltre la data
prevista (01/11) per la pausa del cambio di PC del PO; nessun extra. **Planning:** 29/10, nella [review dello
Sprint 7](sprint-07-review.md) (§9); proposta del team confermata dal Product Owner, con le email nell'impegno.

## Obiettivo

> Il percorso arriva in fondo: il Tutor avvia il tirocinio, scarica Convenzione e Progetto Formativo compilati e chiude
> con il report di restituzione; la mansione torna disponibile se l'azienda lo dice. Le notifiche importanti arrivano
> anche per email.

## Impegno (23 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| US-1003 | Avvio del tirocinio (scheda completa) | 1 | 🎉 Done | `add-internship-start` | [#88](https://github.com/maubernardi/nexus/pull/88) |
| US-1004 | Convenzione in Word (modelli segnaposto per tipo) | 5 | 🎉 Done | `add-internship-documents` | [#89](https://github.com/maubernardi/nexus/pull/89) |
| US-1005 | Progetto Formativo in Word | 3 | 🎉 Done | `add-progetto-formativo` | [#90](https://github.com/maubernardi/nexus/pull/90) |
| US-109 | Cataloghi del report di restituzione | 2 | 🎉 Done | `add-report-catalogs` | [#91](https://github.com/maubernardi/nexus/pull/91) |
| US-1101 | Report di restituzione | 5 | 🎉 Done | `add-closure-report` | [#92](https://github.com/maubernardi/nexus/pull/92) |
| US-1102 | Esito sulla mansione e audit trail | 2 | 🎉 Done | `add-closure-outcome` | [#93](https://github.com/maubernardi/nexus/pull/93) |
| US-1202 | Email transazionali (SMTP IONOS) | 5 | 🎉 Done | `add-notification-emails` | [#94](https://github.com/maubernardi/nexus/pull/94) |

**Velocità:** 23 punti. Azione A13 (test lenti con axe) fatta per prima ([#87](https://github.com/maubernardi/nexus/pull/87)).
Gli extra (US-205, US-102) non sono iniziati e restano nel backlog.

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

- **Report di restituzione** (US-1101), scelte del team che non limitano il Tutor (azione A14), da confermare nella
  review: invio in qualunque momento del tirocinio, obiettivi e difficoltà facoltativi, niente bozza (un solo invio
  con conferma). Il report non si modifica, nemmeno nel database (trigger, come le note dell'azienda).
- **Mansione non più disponibile** (US-1102): resta bloccata dalla segnalazione conclusa, senza ritiro automatico;
  decide il Call Center.
- **Email** (US-1202): stesso testo delle notifiche in app, solo il numero della segnalazione; la password SMTP
  (`NEXUS_SMTP_PASSWORD`) la aggiunge il PO nel `.env` del server, senza la quale le email restano spente.
- **Cambio di PC del PO** (29/10–04/11): backup dei file di Claude e nuova chiave SSH `portalenexus` per il server.

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (29/10, il planning ha anticipato la partenza) | 23 |
| 29/10, sera (US-1003, US-1004, US-1005, US-109, US-1101) | 7 |
| 04/11 (US-1102, US-1202) | 0 |

## Review, retrospettiva e planning successivo

In un unico documento asincrono, `sprint-08-review.md`, da preparare dopo il deploy dello Sprint 8. Per il PO prima
della review:
- collaudo: T-13.4 e sezioni 16–18;
- password SMTP nel `.env` del server e prova di un'email reale (T-18.1).
