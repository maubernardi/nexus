## Why

Storia **US-1310** (Sprint 17), segnalata dal PO il 24/11. Il Call Center tiene NEXUS aperto tutto il giorno, insieme
ad altro, e dopo un po' NEXUS chiede di nuovo la password. La sessione di Keycloak scade dopo 30 minuti senza rinnovo
del token (ASVS 3.3.2). NEXUS rinnova il token solo quando chiede dati al server, e lo fa ogni minuto solo con la
pagina in primo piano. Con la scheda in secondo piano o lo schermo bloccato, il browser mette in pausa le richieste.

In più la sessione scade senza avviso, e le WCAG 2.2 (criterio 2.2.1, livello A) chiedono di avvisare prima della
scadenza.

Il PO ha scelto tra tre strade, presentate con le conseguenze (A40):
1. sessione rinnovata finché la pagina è aperta;
2. inattività di 10 ore in Keycloak;
3. 30 minuti con il solo avviso.

Ha scelto la prima.

## What Changes

- **Rinnovo della sessione finché la pagina è aperta**: ogni minuto NEXUS rinnova il token quando sta per scadere,
  anche con la scheda in secondo piano. Keycloak resta com'è: 30 minuti di inattività, 10 ore al massimo.
- **Sessione già scaduta** (browser chiuso o PC in sospensione per più di 30 minuti): al primo controllo NEXUS torna
  alla pagina di accesso e non lascia dati sullo schermo. Se manca solo la rete, non fa niente.
- **Avviso prima delle 10 ore**: cinque minuti prima, una striscia sotto l'intestazione dice a che ora scade la
  sessione. Offre «Accedi di nuovo adesso», che apre una sessione nuova chiedendo la password, e «Ho capito».
- **ASVS** 3.3.2 aggiornato: la pagina aperta tiene viva la sessione, il PC lasciato solo si affida al blocco dello
  schermo.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `pwa-shell`: sessione di lavoro rinnovata finché l'app è aperta, con l'avviso prima della scadenza.

## Impact

- Frontend:
  - `src/config/auth/authService.ts`: rinnovo, scadenza della sessione, nuovo accesso;
  - nuova striscia `SessionExpiryBanner` in `AppLayout`;
  - testi in `it.ts`.
- `docs/security/asvs-l1.md`, collaudo T-31.
- Nessuna modifica a Keycloak, al backend o al database. Nessuna migrazione.
