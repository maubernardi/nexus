## 1. Database e dominio

- [x] 1.1 Migrazione V46: tabella `business_category` con le 53 voci (D2), colonne `business_category_id` e `activity`
  su `company` (D3); test per nome in `DomainConstraintsIT`
- [x] 1.2 Entità `BusinessCategory` e repository; campi nuovi su `Company`

## 2. Backend

- [x] 2.1 Catalogo `business-categories` per l'ADMIN e `GET /reference/business-categories` per Call Center e ADMIN
  (D1, D4); test di integrazione con le voci iniziali e il `403` del Tutor
- [x] 2.2 Categoria e attività nel modulo dell'azienda, con la validazione e l'audit (D3); test di integrazione,
  categoria disattivata compresa
- [x] 2.3 Filtro `businessCategoryId` nella ricerca delle aziende (D4); test di integrazione
- [x] 2.4 Categoria e attività delle aziende dimostrative nel seed (D6)

## 3. Frontend

- [x] 3.1 Sezione «Categorie merceologiche» nei Cataloghi; test
- [x] 3.2 Modulo dell'azienda con categoria e attività (D5); test con axe
- [x] 3.3 Elenco con la colonna e il filtro per categoria nell'indirizzo (D5); test con axe
- [x] 3.4 Dettaglio dell'abbinamento con categoria e attività; test

## 4. Documentazione e verifica

- [x] 4.1 Collaudo (T-25.2) e Sprint 15 (lo stato nel backlog si aggiorna alla chiusura dello sprint)
- [x] 4.2 `./mvnw verify` e controlli del frontend verdi; controllo in Chrome (tastiera, axe, tema scuro, 390 px)
- [x] 4.3 `openspec validate add-business-categories` senza errori
