# Sprint 18 — 26 – 28 novembre 2026

**Stato:** 🎉 concluso il 26/11: 23 punti su 23 completati (US-514, US-1312, US-511, US-1104, US-515), in produzione a
`119cb88`. Chiuso dal PO il 27/11 con velocity 23; [review](sprint-18-review.md).
**Planning:** 25 – 26/11, dalla [review dello Sprint 17](sprint-17-review.md) (§8, §9).

## Obiettivo

> La scheda dell'azienda dice quanto è affidabile e interessata al progetto, come si sono trovati i tirocinanti e se è
> una nave scuola; il Tutor vede le aziende. (Fino al 26/11: «gli avvisi su sito e backup arrivano in tempo», con
> US-1311, ora nello Sprint 19.)

## Impegno (23 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| US-514 | Affidabilità e interesse al progetto dell'azienda | 5 | 🎉 Done | `add-company-reliability-interest` (archiviata) | [#208](https://github.com/maubernardi/nexus/pull/208) |
| US-511 | Zona dell'azienda e aziende per il Tutor | 5 | 🎉 Done | `add-company-zone-and-tutor-directory` (archiviata) | [#212](https://github.com/maubernardi/nexus/pull/212) |
| US-1104 | Valutazione dell'azienda alla fine del tirocinio | 5 | 🎉 Done | `add-company-reviews` (archiviata) | [#214](https://github.com/maubernardi/nexus/pull/214) |
| US-515 | Azienda «nave scuola» | 5 | 🎉 Done | `add-training-ship-company` (archiviata) | [#216](https://github.com/maubernardi/nexus/pull/216) |
| US-1312 | Tema chiaro o scuro a scelta e intestazione fissa delle schede | 3 | 🎉 Done | `add-theme-toggle-and-top-back-links` (archiviata) | [#209](https://github.com/maubernardi/nexus/pull/209), [#210](https://github.com/maubernardi/nexus/pull/210) (la #209 era finita nel branch di US-514) |

**Ordine** (PO, 26/11):
1. **US-514 subito, anche nel collaudo**: i dati finti del collaudo hanno alcune aziende con stelle e pollici.
2. **US-1104, poi US-515**: toccano tutte e due il report di restituzione e la scheda dell'azienda.
3. **US-1312** (PO, 26/11, dal collaudo di US-514): pulsante 🌙/☀️ nella barra in alto e intestazione fissa delle
   schede (ritorno all'elenco e titolo), senza il ritorno in fondo.
4. **US-511 anticipata** (PO, 26/11): il Tutor deve vedere le aziende. Si fa prima di US-1104, che mette le
   valutazioni proprio nella lista del Tutor. **US-1311 passa allo Sprint 19**: il servizio degli avvisi non è ancora
   scelto (UptimeRobot e Healthchecks.io, oppure Better Stack; review, §8.2).

Nello Sprint 19 il resto della richiesta di contatto (US-507, US-512) e US-1311. Nella lista del Tutor US-515 aggiunge
il 🚢 e US-1104 le valutazioni.

## Decisioni del planning (25 – 26/11)

- **Pre-produzione** (review, §8.3 e §8.4): per ora portalenexus.it è una pre-produzione, che più avanti si azzera.
  Niente prove vecchie; i deploy li decide il PO volta per volta.
- **US-514**: i simboli all'inizio dei nomi importati non si cancellano, passano nei campi nuovi: `$` = una stella
  (cinque `$` = 4 stelle), `£` = un pollice di «interesse al progetto». `&` e `^` iniziali sono refusi.
- **US-1104**: tre voti da 1 a 5; la terza domanda è «capacità formativa dell'azienda e del tutor aziendale». La media
  è una faccina; la nota è sempre facoltativa. Nell'elenco delle valutazioni c'è il nome del Tutor che l'ha data.
- **Il Tutor vede** stelle, pollici, 🚢 e valutazioni (con US-511).

## Come li faccio

- Ogni storia: una change OpenSpec, una PR, il collaudo sul PC del PO prima del merge (EN-11).
- Prossima migrazione: **V52**, probabilmente una per ciascuna delle prime tre storie.
- Icone disegnate da NEXUS (lucide), non emoji: stelle e pollici vuoti davvero bianchi, uguali su ogni PC, con il
  testo per il lettore di schermo.

## Per il PO

- [x] Attivare il sottocriterio `PQ` di SSH sul server (review, §8.5): fatto il 26/11, il server offre
  `mlkem768x25519-sha256`.
- [ ] Scegliere il servizio degli avvisi (US-1311, Sprint 19).
- [x] Collaudo di ogni storia sul PC prima del merge.

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (26/11) | 18 |
| US-1312 aggiunta dal PO dal collaudo di US-514 (26/11) | 21 |
| US-514 Done: collaudo T-35.1 … T-35.5 sul PC, deploy `1603fcc` e T-35.6 in produzione (26/11) | 16 |
| US-1312 Done: collaudo T-36.1 … T-36.3 sul PC e deploy `1603fcc` (26/11) | 13 |
| US-511 anticipata, US-1311 allo Sprint 19 (PO, 26/11) | 15 |
| US-511 Done: collaudo T-37.1 … T-37.4 sul PC e deploy `3a64364` (26/11) | 10 |
| US-1104 Done: collaudo T-38.1 … T-38.4 sul PC e deploy `5095004` (26/11) | 5 |
| US-515 Done: collaudo T-39.1 … T-39.7 sul PC e deploy `119cb88` (26/11) | 0 |
