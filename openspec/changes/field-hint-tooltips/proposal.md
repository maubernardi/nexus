## Why

Richiesta del PO (25/10): i suggerimenti sotto l'etichetta di alcuni campi (es. Partita IVA, Anno di nascita)
abbassano il campo e lo disallineano da quelli vicini sulla stessa riga.

## What Changes

- Il suggerimento passa in un'icona "i" accanto all'etichetta che apre un riquadro (tooltip) al passaggio del mouse, al
  focus da tastiera e al tocco; si chiude con Esc, allontanando mouse/focus o toccando altrove. Scelta del simbolo "i"
  (e non "!") per non confonderlo con l'icona degli errori.
- Il testo resta collegato al campo (`aria-describedby`): chi usa un lettore di schermo lo sente anche a riquadro chiuso.
- Il riquadro occupa la larghezza del campo: non esce mai dallo schermo, anche a 320 px.
- Riguarda tutti i campi con suggerimento di `FormField` (beneficiario, segnalazione, azienda); il gruppo di caselle
  "Tipi di patente" occupa una riga intera e mantiene il testo visibile.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `accessibility-baseline`: suggerimenti dei campi.
