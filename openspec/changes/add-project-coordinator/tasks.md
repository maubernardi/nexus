## 1. Backend

- [x] 1.1 Migrazione V29 (`user_project.coordinator`); progetti coordinati in `PUT /users/{id}/projects` e in `UserDTO`, con audit; decadenza al cambio di ruolo
- [x] 1.2 `GET /me/coordination`, `coordinator` in `/me`, `POST /tickets/transfer` per il Coordinatore con il controllo del progetto; test di integrazione

## 2. Frontend

- [x] 2.1 Casella «Coordinatore» nella pagina dei progetti, «(Coordinatore)» nell'elenco degli utenti
- [x] 2.2 Pagina «Coordinamento» e voce di menu; modulo di spostamento condiviso; test con axe

## 3. Consegna

- [x] 3.1 Collaudo
- [ ] 3.2 PR con CI verde
