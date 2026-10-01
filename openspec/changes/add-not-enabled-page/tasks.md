## 1. Backend

- [x] 1.1 Campo `code` in `ApiErrorResponseDTO`; `USER_NOT_ENABLED` dal gestore di sicurezza e dal gestore globale
- [x] 1.2 IT: codice presente per non censito e disattivato, assente per 403 di ruolo

## 2. Frontend

- [x] 2.1 `code` nel tipo degli errori, `isUserNotEnabled`, `QueryClient` condiviso, invalidazione del profilo dall'interceptor, niente retry
- [x] 2.2 `NotEnabledPage` (titolo, focus, Esci) mostrata da `ProtectedPage`
- [x] 2.3 Test: pagina dedicata senza Riprova e con una sola richiesta, Esci, errore generico invariato, axe

## 3. Verifica e consegna

- [x] 3.1 Verifica in browser reale: 320–1280 px, tema chiaro e scuro, axe con contrasto, solo tastiera
- [x] 3.2 Backlog e sprint aggiornati; PR con CI verde
