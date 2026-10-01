## 1. Motore delle transizioni (EN-1)

- [x] 1.1 `TicketTransition`, `TicketTransitions` (`SUBMIT`), `TicketStateMachine` (+impl) con controlli di ruolo, versione e stato e scrittura della cronologia
- [x] 1.2 Gestore globale: blocco ottimistico → `409`
- [x] 1.3 Test: unit (ordine dei controlli) e IT (transizione riuscita con cronologia, non ammessa → 409 senza effetti, versione superata e gara tra transazioni → 409)

## 2. Invio della segnalazione (US-301)

- [x] 2.1 `GET /me/projects` (progetti attivi assegnati)
- [x] 2.2 `TicketCreateDTO`, `TicketDTO`, `TicketMapper`, `TicketService.submit` con i controlli su beneficiario, progetto e tipologia
- [x] 2.3 `TicketResource` `POST /tickets` (TUTOR)
- [x] 2.4 IT: 201 in NUOVA con numero e cronologia, 400 per progetto/beneficiario/tipologia, 403 per ruolo

## 3. Frontend

- [x] 3.1 API e hook (progetti, invio segnalazione)
- [x] 3.2 Pagina "Nuova segnalazione" (stati vuoti, preselezione da `?beneficiario=`, conferma), rotta TUTOR, menu, Home, collegamento dalla conferma del beneficiario
- [x] 3.3 Test: invio con payload corretto, errori, stati vuoti, preselezione, axe

## 4. Verifica e consegna

- [x] 4.1 Verifica in browser reale: 320–1280 px, tema chiaro e scuro, axe con contrasto, solo tastiera
- [x] 4.2 Backlog (Q12), sprint aggiornati; PR con CI verde
