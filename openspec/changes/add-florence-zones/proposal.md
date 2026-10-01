## Why

Storia **EN-3** (Sprint 2), decisione del PO del 01/10 (Q10): in attesa delle maschere dell'ADMIN (US-106), le zone
devono essere quelle reali del territorio, non segnaposto.

## What Changes

- Migrazione `V11`: 40 zone iniziali in ogni ambiente (anche produzione): i 30 quartieri di Firenze indicati dal PO e i
  10 comuni dell'hinterland fiorentino. Codici in `MAIUSCOLO_CON_UNDERSCORE` senza accenti (es. `SAN_NICCOLO`), nomi come
  scritti dal PO. Inserimento idempotente sul codice.
- Dati demo: le zone segnaposto (Zona Nord, …, Centro città) non sono più create; sui database demo esistenti vengono
  disattivate e beneficiari e mansioni spostati su zone reali (Rifredi, Galluzzo, Campo di Marte, Isolotto, San Lorenzo).

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `reference-data`: requisito delle zone iniziali.

## Non-goals

- Maschere di gestione per l'ADMIN (US-106); tipologie di mansione reali (ancora segnaposto).
