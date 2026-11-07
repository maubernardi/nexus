## Why

Richiesta del PO del 07/11, dopo la prova del recupero della password (US-1308):
- l'avviso di cambio password di Keycloak era poco chiaro: «Le tue credenziali password sono state cambiate su
  07/11/26, 11:46 da …», con l'ora UTC;
- **tutte** le email devono chiudersi con la firma «Portale Nexus».

## What Changes

- **Tema delle email di Keycloak `nexus`** (base: testi italiani di Keycloak):
  - avviso di cambio password: «<username>, le tue credenziali sono state cambiate il <gg/mm/aaaa> alle <hh:mm> da
    questo indirizzo IP: <ip>. Se non sei stato tu, per favore contatta l'amministratore.», con l'ora di Roma;
  - firma «Portale Nexus» in fondo a ogni email di Keycloak: invito, recupero della password, avviso di cambio, prova
    SMTP; in testo semplice e in HTML.
- **Email del backend** (notifiche, codice e avviso del cambio di indirizzo): firma «Portale Nexus» in fondo.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `notifications`: requisito aggiunto per la firma di tutte le email.

## Impact

- Keycloak: tema `keycloak/themes/nexus/email`, `emailTheme` nei realm e in `keycloak-password-reset.sh` (per il
  server già installato, dopo il deploy).
- Backend: `EmailSignature`, usata da `NotificationEmail` ed `EmailChangeMailer`.
- CI: la prova del recupero controlla il testo dell'avviso, l'ora di Roma e la firma.
- Nessuna migrazione.
