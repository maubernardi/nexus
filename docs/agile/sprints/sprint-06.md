# Sprint 6 — 28 – 30 ottobre 2026

**Stato:** ✅ impegno completato il 28/10 (20/20 punti), in attesa della review. **Planning:** 28/10, nella [review dello Sprint 5](sprint-05-review.md) (§9); proposta del team
confermata dal Product Owner.

## Obiettivo

> La bacheca è operativa: il Call Center pubblica annunci (da una mansione libera o dal rilancio dopo un appuntamento
> fallito) e il Tutor li consulta senza vedere l'azienda.

## Impegno (20 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| US-307 | Scadenza nella Home del Tutor | 2 | 🎉 Done | `add-tutor-deadline-column` | [#65](https://github.com/maubernardi/nexus/pull/65) |
| US-902 | Gestione della bacheca (bozze, pubblicazione, archiviazione) | 5 | 🎉 Done | `add-board-management` | [#66](https://github.com/maubernardi/nexus/pull/66) |
| US-904 | Annuncio da una mansione libera | 3 | 🎉 Done | `add-post-from-job-slot` | [#67](https://github.com/maubernardi/nexus/pull/67) |
| US-901 | Bacheca per il Tutor | 3 | 🎉 Done | `add-tutor-board` | [#69](https://github.com/maubernardi/nexus/pull/69) |
| US-803 | Rilancio in bacheca dopo l'appuntamento fallito | 5 | 🎉 Done | `add-board-relaunch` | [#70](https://github.com/maubernardi/nexus/pull/70) |
| US-903 | Sblocco a tutti dopo 7 giorni | 2 | 🎉 Done | `add-board-make-public` | [#71](https://github.com/maubernardi/nexus/pull/71) |

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

## Decisioni durante lo sprint

- **Annuncio legato alla mansione** (PO, 28/10): quando la mansione non è più disponibile il suo annuncio attivo si
  archivia nella stessa operazione, con il motivo nell'audit. Succede in tre casi: abbinamento a una segnalazione,
  ritiro della mansione, disattivazione dell'azienda. Realizzata con US-901 (#69).
- Regole della gestione (team, US-902): il progetto si sceglie solo nella bozza; dopo la pubblicazione si apre a tutti
  solo con lo sblocco (US-903). Si pubblica e si crea un annuncio solo per una mansione libera, a disposizione e di
  un'azienda attiva.

Fuori impegno: correzione [#68](https://github.com/maubernardi/nexus/pull/68). L'API omette i campi vuoti: la Home del
Tutor andava in errore senza timer attivo (US-307) e una segnalazione senza operatore risultava di un collega. Il
difetto è emerso provando US-901 nell'applicazione locale, prima che arrivasse sulla demo.

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (28/10) | 20 |
| 28/10 (US-307, #65) | 18 |
| 28/10 (US-902, #66) | 13 |
| 28/10 (US-904, #67) | 10 |
| 28/10 (US-901, #69) | 7 |
| 28/10 (US-803, #70) | 2 |
| 28/10 (US-903, #71) | 0 |
| Fine sprint | — |

## Review, retrospettiva e planning successivo

In un unico documento asincrono a fine sprint. Demo aggiornata il 28/10 alla versione `b8930e7` (tutto lo Sprint 6);
collaudo: T-5.8, T-5.9 e sezione 11.
