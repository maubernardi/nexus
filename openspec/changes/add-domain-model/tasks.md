## 1. Infrastruttura di persistenza

- [x] 1.1 Aggiungere `io.hypersistence:hypersistence-tsid` 2.1.4 al `pom.xml` (versione esatta)
- [x] 1.2 Implementare `@TsidId` (generatore Hibernate 7) e `AbstractAuditingEntity` con Spring Data JPA Auditing e `AuditorAware` (username o `system`)
- [x] 1.3 Test unitari del generatore (ID univoci e crescenti) e dell'`AuditorAware`

## 2. Migrazioni Flyway

- [ ] 2.1 `V2__create_reference_tables.sql`: `project`, `zone`, `job_category`, `stored_file`
- [ ] 2.2 `V3__create_users.sql`: `app_user`, `user_project`
- [ ] 2.3 `V4__create_companies_and_job_slots.sql`: `company`, `job_slot` (senza la FK verso `ticket`)
- [ ] 2.4 `V5__create_candidates.sql`: `candidate`, `candidate_language`
- [ ] 2.5 `V6__create_tickets_and_board.sql`: sequenze, `ticket`, `board_post`, `ticket_status_history`, `ticket_company_blacklist`, FK cicliche e indice unico parziale
- [ ] 2.6 `V7__create_company_audit_event.sql`: tabella e trigger di immutabilità
- [ ] 2.7 Verificare `./mvnw verify` con `ddl-auto: validate` (entità e schema allineati)

## 3. Entità e repository

- [ ] 3.1 Enum di dominio (`Role`, stati, tipi, genere, titolo di studio, patenti, mezzo di trasporto, livello linguistico)
- [ ] 3.2 Entità e repository: `Project`, `AppUser`, `Zone`, `JobCategory`, `StoredFile`
- [ ] 3.3 Entità e repository: `Candidate`, `CandidateLanguage`, `Company`, `JobSlot`
- [ ] 3.4 Entità e repository: `Ticket`, `TicketStatusHistory`, `TicketCompanyBlacklist`, `BoardPost`
- [ ] 3.5 `CompanyAuditEvent` `@Immutable` con repository di sola aggiunta e lettura
- [ ] 3.6 `toString()` delle entità senza dati personali (D9)

## 4. Accesso riservato agli utenti censiti

- [ ] 4.1 Utenti mock con `id` fisso e realm Keycloak di sviluppo con `id` utente fissi e allineati
- [ ] 4.2 Filtro post-autenticazione: 403 per utente assente o disattivato, riallineamento del ruolo in copia
- [ ] 4.3 Adeguare `CurrentUserResourceIT` (utenti inseriti nel test) e aggiungere i casi: non censito, disattivato, ruolo cambiato

## 5. Seed locale

- [ ] 5.1 Sotto-profilo `seed` nel gruppo `local` (`application-seed.yml` con `classpath:db/seed`)
- [ ] 5.2 `R__seed_*.sql` idempotenti: progetti, utenti e assegnazioni, zone e tipologie segnaposto, aziende, mansioni, candidati fittizi, ticket in più stati (anche speciale), post pubblico, riservato e bozza
- [ ] 5.3 Verificare avvio `local` su database vuoto, riavvio senza duplicati, profilo `test` senza seed

## 6. Test d'integrazione dei vincoli (Testcontainers)

- [ ] 6.1 Numerazione progressiva di ticket e post; unicità (codici, `vat_code`, `external_id`, blacklist, lingua)
- [ ] 6.2 Vincoli del ticket (categoria XOR testo libero, `SPECIAL` ⇒ post, fast-track ⇒ `SPECIAL`, timer) e stati non ammessi
- [ ] 6.3 Mansione: `BLOCCATA` ⇔ ticket; un ticket per mansione; conflitto di versione tra due modifiche
- [ ] 6.4 Bacheca: un post attivo per mansione, `PUBLISHED` ⇒ data, età e valori positivi
- [ ] 6.5 Candidato: patente coerente, valori ammessi; cancellazione di un tutor con ticket rifiutata
- [ ] 6.6 Audit trail: UPDATE, DELETE e TRUNCATE rifiutati dal database

## 7. Chiusura

- [ ] 7.1 Aggiornare README (modello dati, seed) e `openspec validate --all --strict`
- [ ] 7.2 PR con CI verde (il merge lo esegue l'utente)
