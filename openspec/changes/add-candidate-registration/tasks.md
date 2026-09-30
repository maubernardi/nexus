## 1. Backend

- [x] 1.1 `TsidMapper`, DTO (`CandidateCreateDTO`, `CandidateDTO`, `CandidateSummaryDTO`, `ReferenceItemDTO`) e validatori (`@BirthYear`, `@IsoCountry`, `@IsoLanguage`)
- [x] 1.2 `CurrentAppUserService`, `CandidateService` (registrazione, elenco dei propri, dettaglio con controllo d'accesso), `ReferenceDataService`
- [x] 1.3 `CandidateResource` e `ReferenceDataResource` con `@PreAuthorize`
- [x] 1.4 Test: service (Mockito) e IT (201, 400 con fieldErrors, 403 per ruolo, 404 su candidato altrui, elenco dei propri, riferimenti attivi)

## 2. Frontend

- [x] 2.1 Componenti di form accessibili: `FormField`, `CheckboxGroup`, `ErrorSummary`
- [x] 2.2 API e hook (riferimenti, creazione candidato), costanti dei valori codificati, paesi e lingue in italiano
- [x] 2.3 Pagina "Nuovo candidato" (quattro sezioni, lingue ripetibili, conferma), `RoleRoute` e voce di menu del Tutor
- [x] 2.4 Test: validazione, riepilogo con focus, invio con payload corretto, errori del server sui campi, axe

## 3. Verifica e consegna

- [x] 3.1 Verifica in browser reale: 320–1280 px, tema chiaro e scuro, axe con contrasto, solo tastiera
- [x] 3.2 Backlog e sprint aggiornati; PR con CI verde
