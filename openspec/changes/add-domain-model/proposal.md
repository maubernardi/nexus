## Why

Tutte le funzionalità di NEXUS (segnalazioni, coda FIFO, matching, bacheca, tirocini) si appoggiano sul modello dati
del brief (`CLAUDE_CODE_PROMPT.md`, §2). Oggi il database contiene solo lo schema vuoto. Questa change introduce il
modello ER, rivisto rispetto al brief in un confronto passo passo con il committente, così che le change successive
(macchina a stati, API, interfacce) partano da una base solida e già protetta da vincoli.

## What Changes

- Tabelle e vincoli (migrazioni Flyway) per:
  - `Project`, `User` (`app_user`), assegnazione utente–progetto;
  - `Candidate` con le lingue conosciute;
  - `Ticket` con storico degli stati e blacklist delle aziende;
  - `Company` con audit trail immodificabile;
  - `JobSlot`, `BoardPost`;
  - tabelle di riferimento `zone` e `job_category`;
  - metadati dei file (`stored_file`).
- Entità JPA e repository Spring Data per ogni tabella.
- Identificativi:
  - chiave primaria **TSID** (64 bit, esposta come stringa di 13 caratteri);
  - numero progressivo leggibile su Ticket (1, 2, 3…) e BoardPost (#1, #2…).
- Colonne di audit su tutte le tabelle (`created_at/by`, `updated_at/by`) e blocco ottimistico (`version`) su Ticket, JobSlot e BoardPost.
- Revisioni rispetto al brief, decise con il committente:
  - catalogo `job_category`: il tutor sceglie dal catalogo, il testo libero è l'eccezione;
  - collegamento del ticket speciale al post `#N` che l'ha generato;
  - blacklist come tabella invece di un array;
  - rimosso il campo `priority`: l'ordine della coda è fast-track + data di creazione;
  - timer con tre date: avvio, promemoria, scadenza;
  - `job_slot.blocked_by_ticket_id` con vincoli che impediscono la doppia assegnazione;
  - zone come tabella condivisa;
  - azienda e zona di post e ticket ricavate dalla mansione, senza duplicarle;
  - età dell'annuncio come *requisito del programma* (`age_min`/`age_max`), non come preferenza;
  - orario e durata numerici;
  - Company con email e flag `active`;
  - utenti e aziende disattivabili, mai cancellati.
- Candidate e GDPR:
  - tutor proprietario;
  - valori codificati: genere, ISO 3166, titolo di studio, patenti;
  - `anonymized_at` per la futura anonimizzazione a scadenza;
  - protezione affidata al controllo degli accessi (nessuna cifratura applicativa, scelta del committente).
- **Accesso riservato agli utenti censiti**: un utente autenticato ma assente o disattivato in `app_user` riceve 403.
  La copia del ruolo nel database si riallinea a quella di Keycloak.
- Dati di seed per il profilo `local`: progetti, utenti, zone, tipologie, aziende, mansioni, candidati fittizi, ticket in più stati e post di bacheca.

## Capabilities

### New Capabilities
- `domain-model`: entità del dominio, identificativi e vincoli di integrità che il sistema garantisce.
- `company-audit-trail`: registro degli eventi per azienda, in sola aggiunta e non modificabile.
- `dev-seed-data`: dati dimostrativi disponibili solo nell'ambiente di sviluppo locale.

### Modified Capabilities
- `api-platform`: accesso consentito solo agli utenti censiti e attivi in NEXUS; ruolo in copia allineato a Keycloak.

## Non-goals

- Macchina a stati del Ticket (transizioni, blocco e sblocco delle mansioni, timer): change successiva. Qui ci sono solo gli stati ammessi.
- API REST di dominio, filtri di visibilità per ruolo e progetto, interfacce.
- Creazione e disattivazione degli utenti tramite Admin API di Keycloak: change "gestione utenti".
- Strutture dei dati di contratto (fase 7) e del report di restituzione: nelle change delle rispettive fasi.
- Job di anonimizzazione e durata di conservazione: da definire con la cooperativa o il DPO.
- Archiviazione fisica dei file (upload del CV, `.docx`): change degli upload.
- Nessun impatto su frontend, PWA e accessibilità.

## Impact

- Backend: nuove migrazioni `V2+`, entità, repository e un generatore di ID TSID; nuova dipendenza `io.hypersistence:hypersistence-tsid` 2.1.4 (a versione esatta, come da politica delle versioni congelate).
- Sicurezza: il filtro di autenticazione consulta `app_user` a ogni richiesta, con una query indicizzata.
- Utenti mock e realm Keycloak di sviluppo: identificativi fissi, allineati al seed.
- Profilo `local`: aggiunge il sotto-profilo `seed`.
