# Sprint 2 — 2 – 8 ottobre 2026

**Stato:** ✅ chiuso (lavoro completato il 02/10; review con il PO dal 04/10 al 24/10). **Planning:** 01/10, proposta del team confermata dal Product Owner.

## Obiettivo

> L'operatore del Call Center prende in carico una segnalazione dalla coda, trova una mansione compatibile e la propone
> all'azienda; ogni passaggio è registrato nell'audit.

## Impegno (25 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| EN-2 | Audit di ogni cambio di stato e scelta | 5 | 🎉 Done | `add-audit-log` | [#21](https://github.com/maubernardi/nexus/pull/21) |
| EN-3 | Zone iniziali: Firenze e hinterland | 2 | 🎉 Done | `add-florence-zones` | [#22](https://github.com/maubernardi/nexus/pull/22) |
| US-402 | Presa in carico | 2 | 🎉 Done | `add-ticket-take-charge` | [#23](https://github.com/maubernardi/nexus/pull/23) |
| US-501 | Anagrafica aziende | 3 | 🎉 Done | `add-company-registry` | [#24](https://github.com/maubernardi/nexus/pull/24) |
| US-502 | Mansioni dell'azienda | 3 | 🎉 Done | `add-company-job-slots` | [#25](https://github.com/maubernardi/nexus/pull/25) |
| US-601 | Ricerca mansioni compatibili | 5 | 🎉 Done | `add-job-matching` | [#26](https://github.com/maubernardi/nexus/pull/26) |
| US-602 | Abbinamento e proposta | 5 | 🎉 Done | `add-job-matching` | [#26](https://github.com/maubernardi/nexus/pull/26) |

**Extra, se avanza tempo**: US-305 Dettaglio e cronologia del ticket (3), US-303 I miei ticket (3).

## Decisioni del planning

- **Compatibilità (US-601)**: di default stessa zona di residenza del beneficiario e tipologia richiesta; l'operatore
  può allargare a tutte le zone o a tutte le tipologie. Sempre: solo mansioni LIBERE di aziende attive, mai aziende
  escluse per il ticket.
- **Audit e GDPR (EN-2)**: per i beneficiari si registrano i nomi dei campi cambiati, non i valori (l'anonimizzazione a
  2 anni non deve trovare copie nell'audit); per ticket, aziende, mansioni e configurazioni valori prima/dopo completi.
- **PR**: una per storia su `main`; in cascata solo se una storia dipende da una non ancora unita (detto nella PR).

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (02/10) | 25 |
| 02/10 (deploy di #21–#24) | 13 |
| 02/10 (deploy di #25) | 10 |
| Fine lavoro (02/10, deploy di #26) | 0 |

## Sprint Review

Obiettivo **raggiunto**: 25 punti su 25, velocity 25. Review completa, demo guidata e decisioni del PO in
[sprint-02-review.md](sprint-02-review.md).

## Retrospettiva

**Bene**
- Una PR per storia su `main`, senza cascate: più semplice da rivedere e unire.
- La verifica in browser reale ha di nuovo trovato difetti invisibili ai test (scorrimento laterale su mobile, nome
  accessibile "caricola", "S.r.l..").
- Le decisioni del team non previste dalle storie (pagina "Le mie lavorazioni", conferma della proposta) sono state
  dichiarate in review e confermate o corrette dal PO.

**Da migliorare**
- Lo sprint di una settimana non corrispondeva al ritmo reale (lavoro finito il primo giorno, poi attesa). Azione:
  sprint corti di 3 giorni con cerimonie asincrone (deciso, §7.4 della review).
- Migrazioni in PR parallele (V11/V12): rischio di deploy rifiutato. Azione: numero nella PR, rinumerazione dopo ogni
  merge, controllo nella CI (EN-5).
- Una regola decisa dal team (solo l'operatore assegnato può abbinare) era più rigida del lavoro reale. Azione: per le
  regole di accesso e di ruolo chiedere al PO prima di implementarle, non solo dichiararle dopo.
