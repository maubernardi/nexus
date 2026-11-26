## 1. Tema a scelta

- [x] 1.1 `index.css`: colori scuri anche su `:root[data-theme='dark']`; la preferenza del sistema solo senza scelta
      (`:root:not([data-theme='light'])`); `color-scheme` per i controlli nativi
- [x] 1.2 `public/theme-init.js` in `index.html`, prima del primo disegno: legge `nexus-theme` dal browser
- [x] 1.3 `useTheme` (tema in uso, scelta, ascolto del sistema) e molecola `ThemeToggle` nella barra in alto, accanto
      all'utente; testi in `it.ts`; test con axe

## 2. Ritorno all'elenco

- [x] 2.1 Molecola `BackLink` con la freccia; test con axe
- [x] 2.2 In cima e in fondo alle schede: azienda, segnalazione, beneficiario e modifica, annuncio della bacheca,
      tirocinio, report di restituzione, pagine degli utenti; test delle pagine con i due collegamenti

## 3. Prove

- [x] 3.1 Prova in Chrome in locale (A26), il 26/11, con Chrome senza finestra:
      - pulsante: luna nel tema chiaro, sole dopo il clic, tema scuro salvato già all'apertura;
      - ritorno in cima sopra il titolo;
      - barra in alto a 320, 360, 375 e 390 px. A 320 px menu e campanella si sovrapponevano: sotto i 375 px resta solo
        il logo, senza la scritta «NEXUS».
- [x] 3.2 Collaudo T-36 in `docs/test/collaudo.md`; T-35.3 con il pulsante invece di F12
- [x] 3.3 `pnpm lint`, `format:check`, `test --run` (338), `build`
