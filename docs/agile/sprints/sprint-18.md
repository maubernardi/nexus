# Sprint 18 — 26 – 28 novembre 2026

**Stato:** 🔨 in corso dal 26/11.
**Planning:** 25 – 26/11, dalla [review dello Sprint 17](sprint-17-review.md) (§8, §9).

## Obiettivo

> La scheda dell'azienda dice quanto è affidabile e interessata al progetto, come si sono trovati i tirocinanti e se è
> una nave scuola; gli avvisi su sito e backup arrivano in tempo.

## Impegno (18 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| US-514 | Affidabilità e interesse al progetto dell'azienda | 5 | 🔨 In corso | `add-company-reliability-interest` | — |
| US-1104 | Valutazione dell'azienda alla fine del tirocinio | 5 | ✅ Ready | — | — |
| US-515 | Azienda «nave scuola» | 5 | ✅ Ready | — | — |
| US-1311 | Avvisi affidabili su sito e backup | 3 | 📋 Servizio da scegliere | — | — |

**Ordine** (PO, 26/11):
1. **US-514 subito, anche nel collaudo**: i dati finti del collaudo hanno alcune aziende con i vecchi simboli.
2. **US-1104, poi US-515**: toccano tutte e due il report di restituzione e la scheda dell'azienda.
3. **US-1311** dopo la scelta del servizio: Better Stack o Healthchecks.io, da approfondire (review, §8.2).

Il gruppo della richiesta di contatto (US-511, US-507, US-512) va nello Sprint 19. Con US-511 il Tutor vedrà nella sua
lista delle aziende stelle, pollici, 🚢 e valutazioni.

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

- [ ] Attivare il sottocriterio `PQ` di SSH sul server (review, §8.5).
- [ ] Scegliere il servizio degli avvisi (US-1311).
- [ ] Collaudo di ogni storia sul PC prima del merge.

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (26/11) | 18 |
