## 1. Database

- [x] 1.1 `V52__company_reliability_interest.sql`: colonne `reliability` e `project_interest` con i `CHECK`, conversione
      dei nomi con i simboli (D2, D3), poi `ck_company_name_legacy_marks` (D4)
- [x] 1.2 `LegacyCompanyMarksMigrationIT`: aziende a V51 con gli esempi della spec, migrazione a V52; nomi, stelle,
      pollici e versione attesi; nome fatto solo di simboli; voci di audit; poi il vincolo
- [x] 1.3 `DomainConstraintsIT`: i tre vincoli nuovi, per nome

## 2. Backend

- [x] 2.1 `Company`, `CompanyDTO`, `CompanyFormDTO` (`reliability` 1–4, `projectInterest` 1–2, `@Pattern` sul nome)
- [x] 2.2 `CompanyServiceImpl`: valori dal modulo, audit `CREATE`/`UPDATE` con prima e dopo
- [x] 2.3 Ricerca: `minReliability` e `minProjectInterest` in `CompanyRepository`, `CompanyService` e
      `CompanyResource`, `400` fuori intervallo (D7)
- [x] 2.4 `CompanyResourceIT`: salvataggio, toglierle, `400` fuori intervallo e nome con i simboli, audit, filtri,
      `403` al Tutor
- [x] 2.5 Importazione: `LegacyCompanyMarks` con test unitario sugli esempi della spec; `LegacyImportService` e
      resoconto con i conteggi; `LegacyImportIT`

## 3. Frontend

- [x] 3.1 Atomo `RatingDisplay` e molecola `RatingInput` (D6), con test e `expectNoAxeViolations`
- [x] 3.2 `companyApi`: campi e filtri; `CompanyForm` con le due valutazioni e il controllo sul nome; test
- [x] 3.3 `CompaniesPage`: colonne e filtri nell'indirizzo (`stelle`, `interesse`); test
- [x] 3.4 Testi in `it.ts`. La cronologia dell'azienda mostra solo proposte ed esiti, nessuna pagina mostra i campi
      dell'audit: niente da aggiungere

## 4. Dati dimostrativi e prove

- [x] 4.1 Seed: stelle e pollici a tre aziende dimostrative, solo se senza valutazioni (D8)
- [x] 4.2 Prova in locale sul file Access, solo con i conteggi, con una prova temporanea poi cancellata. La regola Java
      e la V52 su un database a V51 con i 13.799 nomi veri danno gli stessi numeri dell'analisi:
      - stelle: 143 con 1, 107 con 2, 20 con 3, 1 con 4 (i cinque `$`);
      - pollici: 89 con 1, 1 con 2;
      - 343 nomi ripuliti, con 343 voci di audit;
      - nessun nome resta con i simboli.
- [x] 4.3 Prova in Chrome in locale (A26), il 26/11, con i dati dimostrativi su un PostgreSQL 19 usa e getta.
      Provati elenco, filtri nell'indirizzo, scheda (3 stelle e un pollice, salvati; «Togli»), frecce da tastiera con il
      riquadro del focus e nome con `$$` rifiutato con il riepilogo.
      L'aspetto l'ho controllato con Chrome senza finestra: tema chiaro e tema scuro vero, e la scheda a 390 px. Le
      icone vuote sono bianche, con un contorno di contrasto 4,9:1 nel tema chiaro.
- [x] 4.4 Collaudo T-35 in `docs/test/collaudo.md`. Backlog e sprint 18 si aggiornano alla chiusura, dopo il merge della
      PR della review (#207), che li contiene
- [x] 4.5 `./mvnw verify` (249 test unitari, 583 di integrazione); `pnpm lint`, `format:check`, `test --run` (335), `build`
