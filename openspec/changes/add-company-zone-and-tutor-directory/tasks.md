## 1. Database e backend

- [x] 1.1 `V53__company_zone.sql`: `company.zone_id`, `fk_company_zone`, `ix_company_zone`; `DomainConstraintsIT`
- [x] 1.2 `Company.zone`, `CompanyDTO.zone`, `CompanyFormDTO.zoneId`; `CompanyServiceImpl` con la zona attiva o già
      dell'azienda e l'audit; filtro `zoneId` nella ricerca del Call Center
- [x] 1.3 `GET /api/v1/companies/directory` (Tutor e ADMIN): `CompanyDirectoryItemDTO` e pagina, sole aziende attive,
      ricerca sul solo nome, filtri per settore e zona, indirizzo della sede operativa o legale
- [x] 1.4 `CompanyResourceIT`: zona (salvataggio, audit, `400`, filtro); elenco del Tutor (campi, ordine, sede
      operativa, ricerca senza partita IVA, filtri, disattivate escluse, `403` sull'API completa)

## 2. Frontend

- [x] 2.1 `companyApi`: zona, filtro e API dell'elenco del Tutor; hook
- [x] 2.2 `CompanyForm` con la zona; `CompaniesPage` con la colonna e il filtro della zona; test
- [x] 2.3 `TutorCompaniesPage` su `/aziende` per il Tutor (scelta in base al ruolo), voce «Aziende» nel suo menu;
      testi in `it.ts`; test con axe

## 3. Dati dimostrativi e prove

- [x] 3.1 Seed: zona di alcune aziende dimostrative, solo se senza
- [x] 3.2 Prova in Chrome in locale (A26), il 26/11, con Chrome senza finestra, come Tutor e come operatore, nei due
      temi. Trovato e corretto: il catalogo delle categorie era solo per Call Center e ADMIN, e per il Tutor il filtro
      restava disattivato; ora lo legge anche il Tutor (`CatalogResourceIT`)
- [x] 3.3 Collaudo T-37 in `docs/test/collaudo.md`
- [x] 3.4 `./mvnw verify` (249 test unitari, 586 di integrazione); `pnpm lint`, `format:check`, `test --run` (346),
      `build`
