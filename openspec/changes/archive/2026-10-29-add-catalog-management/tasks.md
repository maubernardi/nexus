## 1. Backend

- [x] 1.1 Migrazione V20: nome non vuoto e unico tra le voci attive, test dei vincoli per nome
- [x] 1.2 `ReferenceCodes` (codice dal nome) comune a US-302 e ai cataloghi, con test unitario
- [x] 1.3 `CatalogService` e `/catalogs/{zones|job-categories}`: elenco, aggiunta, nuovo nome, disattivazione, riattivazione, audit
- [x] 1.4 IT (`CatalogResourceIT`): codice e audit, nome già attivo, nuovo nome, disattivata non selezionabile ma sui dati, riattivazione rifiutata, ruolo, catalogo sconosciuto

## 2. Frontend

- [x] 2.1 Voce di menu e pagina `/cataloghi` con le due sezioni
- [x] 2.2 Test dei componenti con axe

## 3. Consegna

- [x] 3.1 PR con CI verde (stati del backlog e collaudo aggiornati dopo il merge)
