## Why

Storia **US-1308** (Sprint 10). Chi dimentica la password oggi dipende dall'ADMIN. Nella review dello Sprint 8 (§8.7)
il PO ha deciso che le email di password le manda **Keycloak**, con lo stesso SMTP di IONOS delle notifiche: dopo
US-103 la posta del realm è già impostata, quindi il recupero è soprattutto configurazione.

## What Changes

- Nella pagina di accesso compare **«Password dimenticata?»**: l'utente scrive username o email e riceve un collegamento
  per impostare una nuova password.
- Il collegamento vale **30 minuti** (come il codice del cambio di indirizzo, US-110); il valore predefinito di
  Keycloak, 5 minuti, è troppo stretto per un'email.
- Pagina di recupero ed email in italiano; la pagina usa il tema di NEXUS.
- **Email di sicurezza** quando la password cambia (richiesta del PO del 07/11): l'ascoltatore «email» degli eventi
  di Keycloak, limitato al solo cambio di password (di base scriverebbe anche a ogni password sbagliata).
- La protezione dai tentativi ripetuti del realm resta: dopo 5 password sbagliate l'accesso si blocca per qualche minuto.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `login-page`: requisito aggiunto per il recupero della password.

## Impact

- Realm di sviluppo e della demo: `resetPasswordAllowed` e `actionTokenGeneratedByUserLifespan` (1800 s).
- Sul server già installato il realm non si reimporta: script una tantum `infra/deploy/keycloak-password-reset.sh`,
  senza segreti (README).
- CI: il test del tema verifica il collegamento e la pagina di recupero con il tema.
- Nessuna modifica a backend e frontend; nessuna migrazione.
