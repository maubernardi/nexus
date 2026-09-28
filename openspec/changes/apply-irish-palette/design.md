## Context

Motivazione in proposal.md. Il vincolo è la spec `accessibility-baseline` (contrasto testo ≥ 4,5:1, testo grande e
componenti ≥ 3:1). Contrasti della palette originale sul fondo Cream:

| Colore | come testo su Cream | nota |
|---|---|---|
| Deep Emerald #014C17 | 9,92 | superfici con testo, pulsanti, link |
| Irish Green #019529 | 3,81 | solo elementi grandi, grafici e indicatori (focus) |
| Classic Gold #D4AF37 | 2,03 | mai testo sul chiaro; su Deep Emerald 4,88 |
| Soft Sage #CAE5D1 | — | sfondo: Deep Emerald su Sage 7,64 |

## Decisions

### D1 — Ruoli dei colori (token)

| Token | Chiaro | Scuro |
|---|---|---|
| background / foreground | #FDFBF7 / #0F2417 (15,8) | #0B1A10 / #F4F1E8 (15,9) |
| card | #FFFFFF | #12261A |
| brand / brand-foreground (intestazione) | #014C17 / #FDFBF7 (9,9) | #0E3A1C / #FDFBF7 (12,4) |
| gold (accenti; su brand) | #D4AF37 (4,9) | #D4AF37 (6,1) |
| primary / primary-foreground | #014C17 / #FDFBF7 (9,9) | #5CCB7C / #0B1A10 (8,8) |
| secondary, accent / foreground | #CAE5D1 / #014C17 (7,6) | #1E3A27 / #CAE5D1 (9,3) |
| muted / muted-foreground | #EFF5EF / #3F5A47 (6,9) | #16301F / #B7CDBE (8,5) |
| input (bordi dei controlli) | #5B7A63 (4,6) | #7F9F88 (6,2) |
| ring (focus) | #019529 (3,8) | #D4AF37 (8,5) |
| border (separatori decorativi) | #D6E4D9 | #25402D |

Destructive e warning restano quelli della base (già verificati). Tutte le 32 combinazioni sono state calcolate con la
formula WCAG; la verifica finale è un audit axe **con la regola del contrasto** in un browser reale, in entrambi i temi.

### D2 — Intestazione
Fondo `brand`, filetto oro inferiore. Voce di menu attiva in oro con sottolineatura oro, ruolo utente e icone in oro.
Dentro l'intestazione il focus usa l'oro (`--ring` ridefinito localmente): il verde sul verde non sarebbe visibile.
I pulsanti dell'intestazione usano una variante `brand` (bordo oro, testo crema). Il menu mobile a tendina ha lo
stesso fondo dell'intestazione.

### D3 — Icone e PWA
`favicon.svg`: quadrato Deep Emerald con bordo oro e monogramma "N" crema; le icone PWA sono rigenerate in build.
`theme_color` #014C17, `background_color` #FDFBF7; `theme-color` scuro #0E3A1C.

## Risks / Trade-offs

- [Oro poco leggibile se usato come testo sul chiaro] → vietato per design; ammesso solo su Deep Emerald o come decoro.
- [Colori derivati per il tema scuro non presenti nella palette] → derivati dalla stessa famiglia di verdi, verificati.
