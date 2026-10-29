## Why

Storia **US-1002** (Sprint 7, extra): il monte ore del tirocinio va per i documenti (`{MONTE_ORE_TOTALE}`,
`{GRIGLIA_ORARIA}`, US-1004). Si calcola dalla griglia oraria, dalle date e dai periodi di sospensione, e si ricalcola
quando cambiano. Il PO ha precisato la griglia il 29/10:
- ogni giorno si sceglie una o due fasce orarie;
- per ogni giorno si possono indicare anche le ore: se non si scrive nulla vale la fascia intera. Per esempio fasce
  9–13 e 14–18, ma al massimo 3 ore al giorno;
- le fasce di default si possono modificare;
- le festività nazionali si escludono dal monte ore con una scelta nella scheda, attiva di default.

## What Changes

- **Migrazione V24**:
  - `internship`: due fasce orarie (default 9:00–13:00 e 14:00–18:00) e "escludi le festività nazionali" (default sì);
    tolta la colonna `weekly_hours` di V23, sostituita dal totale della griglia;
  - `internship_schedule_day`: un giorno della settimana per riga, con le fasce scelte e le ore, se diverse dalle fasce
    intere.
- **Calcolo** (`InternshipHours`): totale settimanale e monte ore complessivo, dall'inizio alla fine prevista. Si
  saltano i giorni di sospensione e, se richiesto, le festività nazionali. Si calcola a ogni lettura della scheda:
  sempre allineato a griglia, date e sospensioni.
- **API della scheda**: fasce, giorni e festività nel modulo; nella risposta anche totale settimanale e monte ore.
  Controlli:
  - fasce in ordine;
  - almeno una fascia per giorno;
  - ore del giorno non oltre le fasce scelte;
  - totale settimanale entro le 40 ore (segnaposto di US-1001).
- **Frontend**, nella scheda una sezione "Griglia oraria e monte ore":
  - le fasce modificabili;
  - una tabella dei giorni, con due caselle e le ore per giorno;
  - il totale settimanale, che si aggiorna mentre si compila;
  - la casella delle festività;
  - il monte ore, calcolato sui dati salvati.

  Sparisce il campo "Ore settimanali".
- **Audit `INTERNSHIP_SAVE`**: fasce, giorni e festività con i valori (non sono dati personali).

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `internship-record`: griglia oraria e monte ore (requisiti aggiunti alla capability nata con `add-internship-record`).
