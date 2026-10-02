# Sprint 2 — 2 – 8 ottobre 2026

**Stato:** 🔨 in corso. **Planning:** 01/10, proposta del team confermata dal Product Owner.

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
| US-502 | Mansioni dell'azienda | 3 | 👀 In review | `add-company-job-slots` | [#25](https://github.com/maubernardi/nexus/pull/25) |
| US-601 | Ricerca mansioni compatibili | 5 | ✅ Ready | `add-job-matching` | — |
| US-602 | Abbinamento e proposta | 5 | ✅ Ready | `add-job-matching` | — |

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
| Fine sprint | — |

## Sprint Review

—

## Retrospettiva

—
