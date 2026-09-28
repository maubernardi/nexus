## Why

Prima di un lavoro più approfondito su UX e UI, il committente vuole dare a NEXUS un'identità visiva: una palette
irlandese verde e oro (verdi smeraldo intensi, oro caldo per i dettagli, base crema). Il requisito di accessibilità
(WCAG 2.2 AA, spec `accessibility-baseline`) resta vincolante, quindi i colori vanno assegnati ai ruoli in base al
contrasto misurato.

## What Changes

- Nuovi token di colore (tema chiaro e scuro) basati sulla palette: Irish Green #019529, Deep Emerald #014C17,
  Soft Sage #CAE5D1, Classic Gold #D4AF37, Cream #FDFBF7. Nuovi token `brand` (intestazione) e `gold` (accenti).
- Intestazione in Deep Emerald con filetto oro; voce di menu attiva e ruolo utente in oro; focus in oro dentro
  l'intestazione.
- Pagina iniziale con un semplice riquadro di benvenuto e dettagli oro.
- PWA: colori del manifest, `theme-color` e icone (smeraldo con monogramma crema e bordo oro).

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
<!-- nessuna: cambia solo l'aspetto; i requisiti di contrasto di accessibility-baseline restano invariati (skip_specs) -->

## Non-goals

- Revisione di UX e layout (change successiva più approfondita).
- Tema della pagina di login di Keycloak (follow-up: richiede un tema Keycloak dedicato).

## Impact

- Frontend: `index.css` (token), intestazione, navigazione, menu utente, pagina iniziale, pulsante (variante per
  l'intestazione), icone e manifest PWA.
- Accessibilità: tutte le combinazioni verificate ≥ 4,5:1 per il testo e ≥ 3:1 per bordi e focus (vedi design).
  L'oro non è mai testo su sfondo chiaro (2,0:1); Irish Green solo per indicatori ed elementi grafici (3,8:1).
