## Why

Storia **US-703** (Sprint 4): il timer della proposta accettata (US-701) scade dopo **7 giorni lavorativi**, con un
promemoria al 4°. Il calcolo deve saltare sabati, domeniche, festività nazionali, il patrono e le chiusure del
calendario unico (US-107).

## What Changes

- `ItalianHolidays`: festività nazionali calcolate per ogni anno, con Pasqua (algoritmo gregoriano) e Lunedì
  dell'Angelo; il 4 ottobre non è festivo (PO, 24/10).
- `WorkingDayService`: `isWorkingDay(giorno)` e `plusWorkingDays(da, n)`, l'n-esimo giorno lavorativo dopo la data
  indicata (esclusa), con patrono e chiusure letti dal calendario unico.
- **Prova del calcolo** nella pagina "Calendario" (ADMIN): da una data di accettazione mostra promemoria (4° giorno) e
  scadenza (7°), così il PO verifica il calendario prima di US-701 (`GET /api/v1/calendar/deadline-preview`).

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `work-calendar`: calcolo dei giorni lavorativi.
