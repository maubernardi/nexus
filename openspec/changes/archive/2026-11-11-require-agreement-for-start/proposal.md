## Why

Decisione del PO dell'11/11, dopo US-1008: convenzione e progetti formativi sono **obbligatori per avviare il
tirocinio**. Senza di loro i documenti reali (US-1011) non si possono compilare, e il tirocinio non parte senza
convenzione e progetto formativo firmati.

## What Changes

- Tra i dati richiesti per l'avvio (US-1003) entrano:
  - la **convenzione** completa: numero, data e data di stipula;
  - **almeno un progetto formativo**.
- La scheda li elenca tra i dati mancanti, con il collegamento alla sezione «Convenzione» o «Progetti formativi».
  L'avvio con la scheda incompleta resta rifiutato con l'elenco di ciò che manca.

## Non-goals

- Progetti che coprano tutto il periodo del tirocinio: basta un progetto. Le date restano controllate come in US-1008.
- Nessun cambio ai tirocini già avviati: il controllo vale all'avvio.
- Nessuna migrazione: i dati ci sono già (V39).

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `internship-record`: requisito aggiunto per convenzione e progetti formativi tra i dati richiesti per l'avvio.

## Impact

- Backend: dati mancanti per l'avvio (`missingForStart` e il messaggio di rifiuto) con `agreement` e
  `trainingProjects`.
- Frontend: due voci nell'elenco di ciò che manca, con i collegamenti alle sezioni; i titoli delle sezioni prendono
  il focus. Accessibilità: WCAG 2.2 AA (2.4.3 ordine del focus, 3.3.1); test axe. PWA: nessun impatto.
- Collaudo: un passo in più in T-23.3.
