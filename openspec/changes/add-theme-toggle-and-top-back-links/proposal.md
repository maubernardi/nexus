## Why

Storia **US-1312** (Sprint 18), chiesta dal PO il 26/11 durante il collaudo di US-514:
- **il tema scuro non si può scegliere**: segue solo l'impostazione del sistema operativo, e con F12 non si cambia;
- **nelle schede lunghe** (azienda, segnalazione, beneficiario) il collegamento per tornare all'elenco sta solo in fondo:
  per tornare indietro bisogna scorrere tutta la pagina.

## What Changes

- **Pulsante 🌙/☀️ nella barra in alto**, accanto all'utente:
  - con il tema chiaro mostra la luna e passa al tema scuro; con quello scuro mostra il sole e passa al chiaro;
  - la scelta resta nel browser, anche dopo la chiusura; finché non si sceglie vale il tema del sistema operativo;
  - all'apertura la pagina non lampeggia nel tema sbagliato.
- **«Torna all'elenco» in cima e in fondo** a tutte le schede che lo avevano in fondo: aziende, segnalazioni,
  beneficiari (anche nella modifica), annunci della bacheca, tirocinio, report di restituzione, pagine degli utenti.
  In cima sta sopra il titolo. Ha una freccia, nascosta al lettore di schermo.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `pwa-shell`: tema chiaro o scuro a scelta; ritorno all'elenco in cima e in fondo alle schede.

## Non-goals

- Il tema delle pagine di accesso di Keycloak, che seguono ancora il sistema operativo.
- Una terza scelta «come il sistema» nel pulsante: per tornarci basta cancellare i dati del sito.
- Le pagine di errore (scheda non trovata), che hanno già un solo pulsante per tornare indietro.

## Impact

- Frontend:
  - `index.css`, con i colori scuri anche su `data-theme='dark'` e il sistema solo senza scelta;
  - `public/theme-init.js`, caricato da `index.html` prima del primo disegno: la CSP ammette solo script esterni;
  - `useTheme`;
  - molecole `ThemeToggle` e `BackLink`;
  - `AppHeader` e 14 pagine;
  - testi in `it.ts`;
  - test.
- Accessibilità (WCAG 2.2 AA):
  - il pulsante del tema ha un nome che dice che cosa fa, e un'area di 44 px;
  - i colori dei due temi sono quelli già verificati;
  - lo stesso collegamento in cima e in fondo ha lo stesso nome e la stessa destinazione (2.4.4, 3.2.4).
- PWA: `theme-init.js` entra nella cache della shell come gli altri file; nessuna API in cache.
- Collaudo: prove T-36 sul PC del PO prima del merge (EN-11).
