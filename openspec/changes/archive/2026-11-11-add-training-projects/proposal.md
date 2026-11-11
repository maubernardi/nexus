## Why

Storia **US-1008** (Sprint 13): nei documenti reali di GOL (US-1011) la convenzione si fa una volta per tirocinio,
mentre il progetto formativo si può riformulare durante il tirocinio. Oggi la scheda non ha la convenzione e il
progetto formativo è uno solo, con le date del tirocinio (planning del 09/11).

## What Changes

- **Convenzione** nella scheda del tirocinio, una per segnalazione: numero, data e data di stipula, inseriti dal Tutor.
  La durata è quella del tirocinio; la convenzione è per «n. 1 tirocini».
- **Progetti formativi**: uno o più, numerati 1, 2, 3… nell'ordine delle date. Ognuno ha data di inizio, data di fine,
  mansione e dettaglio della mansione (testo); tutto il resto (orario compreso) viene dalla convenzione e dal tirocinio
  (PO, 09/11).
- **Controlli**: le date di un progetto stanno dentro quelle del tirocinio, e ogni progetto inizia dopo la fine del
  precedente, senza sovrapporsi neanche di un giorno. Altrimenti il salvataggio è rifiutato con la spiegazione. Il
  database impedisce le sovrapposizioni.
- **Durata in mesi e monte ore** di ogni progetto, calcolati sulle sue date con la griglia, le sospensioni e le
  festività della scheda. I mesi sono arrotondati al mese più vicino: 15 giorni o più contano come un mese, le
  sospensioni comprese.
- **Audit** (EN-2):
  - convenzione e progetti (date e mansione) si registrano con i valori;
  - il dettaglio della mansione, testo libero, si registra solo come «cambiato»;
  - il motivo della riformulazione non si registra (PO, 09/11).
- **Appuntamento fallito**: convenzione e progetti riguardavano quell'azienda e si azzerano, come gli altri dati del
  tirocinio.
- **Migrazione V39** solo additiva; vincoli con nome, testati.

## Non-goals

- **Documenti** (convenzione e un progetto formativo per ogni progetto) sui modelli reali: arrivano con US-1011. Il
  «Progetto Formativo» scaricabile oggi resta quello della scheda.
- **Avvio del tirocinio:** convenzione e progetti formativi non sono tra i dati richiesti, perché i criteri non lo
  chiedono.
- **Motivo della riformulazione:** non si registra (PO, 09/11).
- **Orario diverso per progetto:** resta quello del tirocinio (PO, 09/11).
- **Più convenzioni o più tirocini** per segnalazione.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `internship-record`: requisiti aggiunti per la convenzione, per i progetti formativi (controlli, durata e monte ore,
  audit) e per il loro azzeramento con l'appuntamento fallito.

## Impact

- Migrazione **V39**; nessun passo sul server.
- API: `InternshipFormDTO` e `InternshipDTO` con i dati della convenzione e `trainingProjects` (in uscita con numero,
  mesi e monte ore); `InternshipDTO.jobSlotTitle` per proporre la mansione.
- Frontend: due sezioni nuove nella scheda del tirocinio, «Convenzione» e «Progetti formativi», con righe ripetibili.
  Accessibilità: WCAG 2.2 AA (1.3.1 informazioni e relazioni, 3.3.1 identificazione degli errori, 3.3.2 etichette o
  istruzioni, 2.4.6 intestazioni ed etichette); test axe. PWA: nessun impatto (le API non vanno in cache).
- Collaudo: nuova prova T-23.3.
