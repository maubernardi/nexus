## 1. Backend

- [x] 1.1 `TicketRepository.findQueue` (JPQL con fetch join, filtri facoltativi), `QueueItemDTO`, mapping
- [x] 1.2 `TicketService.queue`, `GET /tickets/queue` (CALL_CENTER, ADMIN); `GET /reference/projects`
- [x] 1.3 IT: contenuto e ordine (fast-track prima, poi per arrivo), esclusione di assegnati e stati successivi, filtri, 400 su id non validi, 403 per il Tutor

## 2. Frontend

- [x] 2.1 API e hook (coda con aggiornamento periodico, progetti)
- [x] 2.2 Pagina "Coda segnalazioni": filtri nell'indirizzo con annuncio dei risultati, tabella accessibile, badge fast-track, stato vuoto; rotta CC/ADMIN, menu, Home
- [x] 2.3 Test: ordine e badge, filtri e indirizzo, annuncio, stato vuoto, axe

## 3. Verifica e consegna

- [x] 3.1 Verifica in browser reale: 320–1280 px, tema chiaro e scuro, axe con contrasto, solo tastiera
- [x] 3.2 Backlog e sprint aggiornati; PR con CI verde
