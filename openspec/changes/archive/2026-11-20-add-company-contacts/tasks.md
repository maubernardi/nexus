## 1. Database e dominio

- [x] 1.1 Migrazione V47: tabella `contact_role` con i 12 ruoli (D1), tabella `company_contact` con il referente di
  oggi come primo (D2), colonne nuove di `company` (D3); test per nome in `DomainConstraintsIT`
- [x] 1.2 Entità `ContactRole` e `CompanyContact`, repository; campi e referenti su `Company`

## 2. Backend

- [x] 2.1 Catalogo `contact-roles` per l'ADMIN e `GET /reference/contact-roles` per Call Center e ADMIN (D1, D4); test
  di integrazione con le voci iniziali e il `403` del Tutor
- [x] 2.2 Referenti, recapiti e sede operativa nel modulo dell'azienda, con la validazione, la sostituzione in blocco e
  l'audit (D2–D5); test di integrazione: nome o ruolo, ruolo disattivato, 11 referenti, `409` sui referenti, audit
  senza valori, referente copiato da V47
- [x] 2.3 Referenti e sede operativa delle aziende dimostrative (D7)

## 3. Frontend

- [x] 3.1 Sezione «Ruoli dei referenti» nei Cataloghi; test
- [x] 3.2 Modulo dell'azienda con i gruppi, i referenti (aggiunta, rimozione, focus), i recapiti e la sede operativa
  (D6); test con axe
- [x] 3.3 Colonna «Referente» nell'elenco e dettaglio dell'abbinamento (D6); test

## 4. Documentazione e verifica

- [x] 4.1 Collaudo (T-25.3) e Sprint 15
- [x] 4.2 `./mvnw verify` e controlli del frontend verdi; controllo in Chrome (tastiera, focus, axe, tema scuro,
  390 px)
- [x] 4.3 `openspec validate add-company-contacts` senza errori
