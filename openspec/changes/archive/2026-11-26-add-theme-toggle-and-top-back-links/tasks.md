## 1. Tema a scelta

- [x] 1.1 `index.css`: colori scuri anche su `:root[data-theme='dark']`; la preferenza del sistema solo senza scelta
      (`:root:not([data-theme='light'])`); `color-scheme` per i controlli nativi
- [x] 1.2 `public/theme-init.js` in `index.html`, prima del primo disegno: legge `nexus-theme` dal browser
- [x] 1.3 `useTheme` (tema in uso, scelta, ascolto del sistema) e molecola `ThemeToggle` nella barra in alto, accanto
      all'utente; testi in `it.ts`; test con axe

## 2. Intestazione fissa delle schede

- [x] 2.1 Molecola `BackLink` con la freccia; test con axe
- [x] 2.2 Molecola `PageHeader` (ritorno e titolo), fissa sotto la barra in alto, che scorre se la finestra è bassa;
      `useHeightVariable` scrive l'altezza della barra (`--header-height`) e dell'intestazione
      (`--page-header-height`), usate da `scroll-padding-top` (WCAG 2.4.11); test con axe
- [x] 2.3 Nelle 14 schede (azienda, segnalazione, beneficiario e modifica, annuncio della bacheca, tirocinio, report
      di restituzione, pagine degli utenti) l'intestazione fissa, senza il ritorno in fondo; test delle pagine

## 3. Prove

- [x] 3.1 Prova in Chrome in locale (A26), il 26/11, con Chrome senza finestra:
      - pulsante: luna nel tema chiaro, sole dopo il clic, tema scuro salvato già all'apertura;
      - ritorno in cima sopra il titolo;
      - barra in alto a 320, 360, 375 e 390 px. A 320 px menu e campanella si sovrapponevano: sotto i 375 px resta solo
        il logo, senza la scritta «NEXUS»;
      - intestazione fissa: ferma mentre la scheda scorre, anche a 390 px; il campo con il focus compare sotto di lei;
        con la finestra alta 440 px scorre con la pagina; «Nuovo utente» con il solo pulsante in fondo.
- [x] 3.2 Collaudo T-36 in `docs/test/collaudo.md`; T-35.3 con il pulsante invece di F12
- [x] 3.3 `pnpm lint`, `format:check`, `test --run` (339), `build`
