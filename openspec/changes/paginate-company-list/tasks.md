## 1. Backend

- [x] 1.1 `CompanyRepository.search` con `Pageable` e query di conteggio; `CompanyPageDTO` (D1)
- [x] 1.2 `CompanyService.search` e `CompanyResource` con `page` e `size`, solo 10/25/50/100, `400` altrimenti (D2)
- [x] 1.3 `CompanyResourceIT`: totale, ordine tra le pagine, pagina oltre l'ultima, predefiniti, `400`; test esistenti
      sulla risposta a pagine

## 2. Frontend

- [x] 2.1 `companyApi` e `useCompanySearch` con la pagina
- [x] 2.2 Molecola `Pagination` con test e axe (D4)
- [x] 2.3 `CompaniesPage`: pagina e aziende per pagina nell'indirizzo, stato con l'intervallo, focus alla tabella,
      pagina oltre l'ultima (D3); testi in `it.ts`; test

## 3. Documenti e prove

- [x] 3.1 Backlog (US-513), sprint 17, collaudo T-27.2 e T-32
- [x] 3.2 Prova in Chrome in locale (A26), il 24/11, con 13.799 aziende finte su PostgreSQL 19, senza i dati veri del
      database attuale: pagina da 25 in circa 9 KB e poche decine di millisecondi (prima l'elenco intero pesava circa
      4,8 MB); Successiva, Ultima e menu delle aziende per pagina. Corretto: dopo «Successiva» la vista non tornava in
      cima alla tabella
