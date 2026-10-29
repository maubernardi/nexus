# Sprint 7 — 29 – 31 ottobre 2026

**Stato:** 🎉 concluso il 29/10: impegno di 26 punti completato, più l'extra US-1002 (5). R2 completa il 29/10
(US-306, US-403, US-404, US-106). **Planning:** 28/10, nella [review dello Sprint 6](sprint-06-review.md) (§9);
proposta del team confermata dal Product Owner.

## Obiettivo

> Si chiude R2: il Tutor candida un beneficiario a un annuncio e il Call Center approva o rifiuta la corsia veloce;
> l'ADMIN gestisce zone e tipologie; inizia la scheda del tirocinio.

## Impegno (26 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| US-306 | Segnalazione speciale dalla bacheca | 3 | 🎉 Done | `add-special-ticket` | [#74](https://github.com/maubernardi/nexus/pull/74) |
| US-403 | Approvazione fast-track | 5 | 🎉 Done | `add-fast-track-approve` | [#75](https://github.com/maubernardi/nexus/pull/75) |
| US-404 | Rifiuto fast-track | 3 | 🎉 Done | `add-fast-track-reject` | [#76](https://github.com/maubernardi/nexus/pull/76) |
| US-106 | Cataloghi di zone e tipologie | 3 | 🎉 Done | `add-catalog-management` | [#77](https://github.com/maubernardi/nexus/pull/77) |
| US-108 | Tipi di tirocinio (catalogo) | 1 | 🎉 Done | `add-internship-types` | [#79](https://github.com/maubernardi/nexus/pull/79) |
| US-506 | Legale rappresentante e codici ATECO dell'azienda | 3 | 🎉 Done | `add-company-legal-data` | [#80](https://github.com/maubernardi/nexus/pull/80) |
| US-1001 | Scheda amministrativa del tirocinio (campi segnaposto) | 8 | 🎉 Done | `add-internship-record` | [#81](https://github.com/maubernardi/nexus/pull/81), [#82](https://github.com/maubernardi/nexus/pull/82) |

**Extra**, nell'ordine: US-1002 *Griglia oraria e monte ore* (3), US-1003 *Avvio del tirocinio* (1). I punti non sono
un tetto: «preferisco di più che di meno» (PO, 28/10).

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| US-1002 | Griglia oraria e monte ore | 5 (da 3, PO 29/10) | 🎉 Done | `add-internship-schedule` | [#83](https://github.com/maubernardi/nexus/pull/83) |
| US-1003 | Avvio del tirocinio | 1 | 📋 Non iniziata: torna al backlog | | |

**Velocità:** 31 punti (26 di impegno più 5 di extra).

**Fuori piano:** US-1304 *Tema di Keycloak* (3), anticipata da R4 su richiesta del PO del 29/10
([#85](https://github.com/maubernardi/nexus/pull/85)); non conta nella velocity (review, §8.1).

Ordine: preparazione comune dei test (azione A7) → US-306 → US-403 → US-404 → US-106 → US-108 → US-506 → US-1001. Una PR per storia; gli
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

## Decisioni durante lo sprint

- **Scheda del tirocinio** (PO, 28–29/10): alla proposta iniziale si aggiungono il legale rappresentante dell'azienda
  (può non coincidere con il referente), più tutor aziendali, i periodi di sospensione (dal–al, più righe) e il codice
  ATECO. I codici ATECO, uno o più, stanno nella scheda dell'azienda e nel tirocinio si sceglie quello giusto; il Tutor
  può aggiungerli dalla scheda del tirocinio. Il Tutor può anche inserire il legale rappresentante se manca, non
  correggerlo.
- **Tipo di tirocinio** (PO, 29/10): catalogo gestito dall'ADMIN, con le voci Curriculare, Extracurriculare, Di
  inclusione, Di inserimento socio-terapeutico. Cambia il tipo di documento; altri parametri arriveranno.
- Effetto sul piano: nuove US-108 (1) e US-506 (3); US-1001 da 5 a 8; US-1002 da 5 a 3 (le sospensioni passano a
  US-1001). Tutto nello Sprint 7, che sale a **26 punti** (PO: «mettiamo tutto in questo sprint»). Nessun dato già
  presente: la verifica sul modello attuale l'ha confermato.
- **I documenti sono sempre competenza del Tutor** (PO, 29/10): la scheda del tirocinio la vede solo il Tutor della
  segnalazione, non il Call Center; US-1004 e US-1005 diventano del solo Tutor.
- **La scheda non si blocca all'avvio** (PO, 29/10, dopo il merge della #81): il Tutor la corregge in qualunque
  momento, anche a tirocinio avviato. Corretto nella [#82](https://github.com/maubernardi/nexus/pull/82).
- **Griglia oraria** (PO, 29/10): fasce modificabili per tirocinio; per ogni giorno una o due fasce con le ore del
  giorno (vuote = fasce intere); festività nazionali escluse con una scelta nella scheda, attiva di default. Ore
  **per giorno**, non per fascia, e massimo di 40 ore settimanali confermati. US-1002 da 3 a 5 punti.
- Scelte del team presentate al PO, senza obiezioni finora (da riprendere nella review se serve): controllo del codice fiscale con la data di nascita; sospensioni dentro
  il periodo del tirocinio; con l'appuntamento fallito della scheda restano solo i dati del beneficiario; un codice
  ATECO scelto in una scheda non si toglie dall'azienda.
- Trovato nella prova sull'app reale e corretto prima della PR: le colonne `time` subivano il fuso di
  `hibernate.jdbc.time_zone` (9:00 → 10:00); le fasce si salvano in minuti dalla mezzanotte.

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (29/10, lavoro iniziato la sera del 28/10) | 19 |
| 28/10 (US-306, #74) | 16 |
| 28/10 (US-403, #75) | 11 |
| 28/10 (US-404, #76) | 8 |
| 29/10 (US-106, #77) | 5 |
| 29/10, ampliamento: US-108, US-506 e US-1001 da 5 a 8 (+7) | 12 |
| 29/10 (US-108, #79) | 11 |
| 29/10 (US-506, #80) | 8 |
| 29/10 (US-1001, #81; correzione #82) | 0 |
| 29/10, extra (US-1002, #83: +5 completati) | 0 |
| Fine sprint (29/10) | 0 |

## Review, retrospettiva e planning successivo

[Review dello Sprint 7](sprint-07-review.md), con la retrospettiva e la proposta per lo Sprint 8.
