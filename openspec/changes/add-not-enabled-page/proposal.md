## Why

Storia **US-101** (Sprint 1): chi si autentica ma non è censito in NEXUS, o è stato disattivato, oggi vede "Impossibile
caricare il profilo utente" con un pulsante "Riprova" che non può funzionare. Serve un messaggio chiaro che indichi di
rivolgersi all'amministratore.

## What Changes

- Il 403 "Utente non abilitato a NEXUS" porta un codice stabile `code: "USER_NOT_ENABLED"` nel formato errori uniforme,
  sia quando nasce nel filtro di sicurezza sia quando nasce in un service. Il client non dipende dal testo del messaggio.
- Pagina **Account non abilitato**: spiegazione, indicazione di contattare l'amministratore e pulsante **Esci**; nessun
  "Riprova". Il profilo non viene richiesto di nuovo in automatico per questo errore.
- Se l'utente viene disattivato durante la sessione, la prima risposta con quel codice ricarica il profilo e l'app mostra
  la stessa pagina.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `api-platform`: il 403 per utente non censito o disattivato ha un codice dedicato.
- `pwa-shell`: la shell mostra una pagina dedicata all'utente non abilitato.

## Non-goals

- Richiesta di abilitazione dall'app o notifica all'amministratore.
- Pagina di accesso di Keycloak (tema e testi): revisione UX successiva.
