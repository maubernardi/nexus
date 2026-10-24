## 1. Backend

- [x] 1.1 `PUT /beneficiaries/{id}` per il proprietario; lingue aggiornate sul posto; zona attuale valida anche se disattivata
- [x] 1.2 Audit `UPDATE` con i soli nomi dei campi cambiati; nessun evento senza modifiche
- [x] 1.3 Visibilità del dettaglio per i Tutor dei progetti delle segnalazioni; `ownerTutorName` ed `editable` nel DTO
- [x] 1.4 IT: modifica, audit, proprietario/collega/Call Center, zona disattivata, visibilità per progetto

## 2. Frontend

- [x] 2.1 Pagina "I miei beneficiari" con ricerca; voce "Beneficiari" nel menu
- [x] 2.2 Scheda del beneficiario; modifica con il modulo della registrazione
- [x] 2.3 Test con axe; verifica in browser reale (Tutor proprietario, collega di progetto, Call Center; 320/1280 px, chiaro/scuro)

## 3. Consegna

- [x] 3.1 PR con CI verde (stati del backlog aggiornati dopo il merge)
