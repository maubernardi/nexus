## Why

Richiesta del PO (04/10, durante la review dello Sprint 2): nelle liste di zone i quartieri di Firenze devono
distinguersi a colpo d'occhio dai comuni dell'hinterland (es. "San Lorenzo" quartiere e "Borgo San Lorenzo" comune).

## What Changes

- Migrazione `V14`: i 30 quartieri prendono il prefisso "Firenze " (es. "Firenze Novoli"); i 10 comuni restano invariati.
  I codici non cambiano, quindi nessun dato collegato va toccato. Idempotente.
- Effetto collaterale utile: nei menu ordinati per nome i quartieri risultano raggruppati.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `reference-data`: nomi delle zone iniziali.
