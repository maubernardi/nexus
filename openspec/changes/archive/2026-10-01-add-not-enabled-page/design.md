## Context

Motivazione: proposal.md (US-101). Il backend risponde già 403 "Utente non abilitato a NEXUS" dal `RegisteredUserFilter`
(change `add-domain-model`); il frontend lo tratta come errore generico di caricamento del profilo.

## Decisions

### D1 — Codice di errore invece del testo
`ApiErrorResponseDTO` acquisisce un campo facoltativo `code` (omesso quando assente). `UserNotEnabledException.CODE =
"USER_NOT_ENABLED"` è scritto dall'`AccessDeniedHandler` e da un gestore dedicato nel `GlobalExceptionHandler` (prima
un `UserNotEnabledException` lanciato da un service diventava un generico "Accesso negato"). I 403 di ruolo restano
senza codice. Confrontare il messaggio sarebbe fragile (testi modificabili, traduzioni).

### D2 — Pagina fuori dalla shell
`ProtectedPage` mostra `NotEnabledPage` quando il profilo fallisce con quel codice: senza profilo non ci sono menu né
ruoli da mostrare. La pagina ha un proprio `h1` focalizzato, titolo del documento, landmark `main`, e il solo pulsante
Esci (logout Keycloak, o ritorno all'accesso di sviluppo in modalità mock). `useCurrentUser` non riprova questo errore.

### D3 — Disattivazione durante la sessione
L'interceptor di risposta, ricevuto il codice, invalida la query del profilo: il ricaricamento fallisce con lo stesso
codice e `ProtectedPage` mostra la pagina dedicata. Per farlo il `QueryClient` è estratto in un modulo condiviso.
