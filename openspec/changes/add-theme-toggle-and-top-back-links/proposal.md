## Why

Storia **US-1312** (Sprint 18), chiesta dal PO il 26/11 durante il collaudo di US-514:
- **il tema scuro non si può scegliere**: segue solo l'impostazione del sistema operativo, e con F12 non si cambia;
- **nelle schede lunghe** (azienda, segnalazione, beneficiario) il collegamento per tornare all'elenco sta solo in fondo:
  per tornare indietro bisogna scorrere tutta la pagina. Nelle pagine degli utenti sta accanto al pulsante del modulo,
  e si vede male.

## What Changes

- **Pulsante 🌙/☀️ nella barra in alto**, accanto all'utente:
  - con il tema chiaro mostra la luna e passa al tema scuro; con quello scuro mostra il sole e passa al chiaro;
  - la scelta resta nel browser, anche dopo la chiusura; finché non si sceglie vale il tema del sistema operativo;
  - all'apertura la pagina non lampeggia nel tema sbagliato.
- **Intestazione fissa delle schede** (PO, 26/11): ritorno all'elenco e titolo stanno in cima e restano fermi sotto la
  barra in alto mentre la scheda scorre; il ritorno in fondo non c'è più. Vale per tutte le schede che lo avevano in
  fondo: aziende, segnalazioni, beneficiari (anche nella modifica), annunci della bacheca, tirocinio, report di
  restituzione, pagine degli utenti. Il ritorno ha una freccia, nascosta al lettore di schermo.
- Sugli schermi bassi l'intestazione della scheda scorre con la pagina, per non coprirla. Lo spazio di scorrimento
  tiene conto della barra e dell'intestazione: l'elemento con il focus non ci finisce sotto (WCAG 2.4.11).

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `pwa-shell`: tema chiaro o scuro a scelta; intestazione fissa delle schede, con il ritorno all'elenco.

## Non-goals

- Il tema delle pagine di accesso di Keycloak, che seguono ancora il sistema operativo.
- Una terza scelta «come il sistema» nel pulsante: per tornarci basta cancellare i dati del sito.
- Le pagine di errore (scheda non trovata), che hanno già un solo pulsante per tornare indietro.

## Impact

- Frontend:
  - `index.css`, con i colori scuri anche su `data-theme='dark'` e il sistema solo senza scelta;
  - `public/theme-init.js`, caricato da `index.html` prima del primo disegno: la CSP ammette solo script esterni;
  - `useTheme`;
  - molecole `ThemeToggle`, `BackLink` e `PageHeader`; `useHeightVariable` per le altezze delle parti fisse;
  - `AppHeader` e 14 pagine;
  - testi in `it.ts`;
  - test.
- Accessibilità (WCAG 2.2 AA):
  - il pulsante del tema ha un nome che dice che cosa fa, e un'area di 44 px;
  - i colori dei due temi sono quelli già verificati;
  - le parti fisse non coprono l'elemento con il focus (2.4.11), e sugli schermi bassi l'intestazione scorre (1.4.10).
- PWA: `theme-init.js` entra nella cache della shell come gli altri file; nessuna API in cache.
- Collaudo: prove T-36 sul PC del PO prima del merge (EN-11).
