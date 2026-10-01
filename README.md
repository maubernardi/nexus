# NEXUS

Progressive Web App multi-progetto per l'**Area Lavoro** e il **Call Center Sociale** di una Cooperativa
Sociale: segnalazioni dei tirocinanti, coda FIFO, matching con le aziende, bacheca opportunità, avvio dei
tirocini e generazione documentale.

- **Tutor territoriali**: uso mobile-first (PWA installabile).
- **Operatori Call Center**: uso da desktop.

Il brief funzionale completo è in [`CLAUDE_CODE_PROMPT.md`](CLAUDE_CODE_PROMPT.md). Le specifiche vive stanno in
[`openspec/`](openspec/).

## Architettura

```
nexus/
├── backend/    Spring Boot 4.1 · Java 25 LTS · PostgreSQL · Flyway · Spring Security 7 (Keycloak JWT)
├── frontend/   React 19.3 · React Router 8 · Vite 8 · TypeScript 6 · Tailwind v4 + shadcn/ui · PWA (Workbox)
├── infra/      docker-compose: PostgreSQL 18 + Keycloak 26.7 (realm "nexus" importato)
└── openspec/   specifiche e change proposal (spec-driven development)
```

| Area | Scelte principali |
|---|---|
| API | REST `/api/v1`, OpenAPI su `/swagger-ui.html`, errori in formato uniforme (`timestamp, status, error, message, path, fieldErrors`) |
| Sicurezza | Deny by default, ruoli `TUTOR`, `CALL_CENTER`, `ADMIN`; JWT Keycloak oppure header `X-USER-ID` (solo sviluppo) |
| Dati | Schema gestito solo da Flyway (`ddl-auto: validate`) |
| PWA | Installabile, shell offline, aggiornamento su conferma dell'utente, **nessun dato personale in cache** |
| Accessibilità | WCAG 2.2 AA: eslint-plugin-jsx-a11y + axe-core nei test, token colore verificati, skip link, focus management |

## Prerequisiti

- **JDK 25** (LTS, es. Temurin 25.0.4.1). Il wrapper Maven (3.9.16) è incluso e la build rifiuta altre versioni di JDK
- **Node.js 24.21.0** (LTS, vedi `.nvmrc`: `nvm use`) e **pnpm 12.6.0** (`npm i -g pnpm@12.6.0`); pnpm rifiuta altre versioni di Node
- Docker

## Avvio in locale

```bash
# 1. Infrastruttura
cd infra && docker compose up -d        # Postgres :5432, Keycloak :8180 (admin/admin)
#    se avevi un volume creato con PostgreSQL 17: docker compose down -v (dati solo di sviluppo)

# 2. Backend (profilo "local" = Postgres + sicurezza mock)
cd backend && ./mvnw spring-boot:run    # http://localhost:8080

# 3. Frontend
cd frontend && pnpm install && pnpm dev # http://localhost:5173
```

In modalità mock il frontend mostra la pagina **Accesso di sviluppo**, dove si sceglie l'utente.

Il profilo `local` carica anche i **dati dimostrativi** (`backend/src/main/resources/db/seed`): progetti GOL e POLIS,
gli utenti di test, zone e tipologie di mansione segnaposto, aziende e mansioni fittizie, beneficiari "Demo", 7 ticket
in stati diversi e 3 post di bacheca. Gli script sono idempotenti; per ripartire da zero:
`docker compose -f infra/docker-compose.yml exec postgres psql -U nexus -d nexus -c 'DROP SCHEMA nexus CASCADE'`.

### Usare Keycloak reale

```bash
cd backend && PROFILES_ACTIVE=keycloak,seed ./mvnw spring-boot:run
cd frontend && VITE_AUTH_MODE=keycloak pnpm dev
```

### Utenti di test

La password è la stessa per tutti in Keycloak: `password`. Per accedere un utente deve esistere anche in NEXUS
(tabella `app_user`, creata dal seed): gli utenti autenticati ma non censiti o disattivati ricevono 403.

| Username | Ruolo |
|---|---|
| `tutor1`, `tutor2` | TUTOR |
| `operatore.cc` | CALL_CENTER |
| `admin` | ADMIN |

## Modello dati

Schema PostgreSQL `nexus`, gestito solo da migrazioni Flyway (`backend/src/main/resources/db/migration`).
Diagramma ER, tabelle e motivazioni: [`openspec/changes/archive/2026-09-28-add-domain-model/design.md`](openspec/changes/archive/2026-09-28-add-domain-model/design.md);
requisiti in `openspec/specs/domain-model`.

| Area | Tabelle |
|---|---|
| Riferimento | `project`, `zone`, `job_category`, `stored_file` |
| Utenti | `app_user` (censiti da NEXUS, ruolo in copia da Keycloak), `user_project` |
| Beneficiari | `beneficiary`, `beneficiary_language` |
| Aziende | `company`, `job_slot`, `company_audit_event` (in sola aggiunta, imposto da trigger) |
| Segnalazioni | `ticket`, `ticket_status_history`, `ticket_company_blacklist`, `board_post` |

- **Identificativi**: chiave primaria TSID (64 bit), esposta nelle API come stringa di 13 caratteri; ticket e post hanno
  anche un numero progressivo leggibile.
- **Integrità nel database**: vincoli `CHECK`, unicità, chiavi esterne e trigger, verificati da `DomainConstraintsIT`.
- **Audit e concorrenza**: `created/updated at/by` su tutte le tabelle; blocco ottimistico su ticket, mansioni e post.
- **GDPR**: i beneficiari contengono dati di categoria particolare. Accesso controllato, nessun dato personale nei log,
  `anonymized_at` predisposto per l'anonimizzazione.

## Profili backend

| `PROFILES_ACTIVE` | Composizione | Autenticazione |
|---|---|---|
| `local` (default) | postgresql, security-mock, seed | header `X-USER-ID` |
| `keycloak` | postgresql, keycloak (in locale aggiungere `seed`) | JWT Keycloak |
| `test` | Testcontainers, security-mock (senza seed) | header `X-USER-ID` |

Variabili principali: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `KEYCLOAK_ISSUER_URI`, `KEYCLOAK_AUDIENCE`,
`CORS_ALLOWED_ORIGINS`, `OPENAPI_ENABLED`.

## Versioni congelate

Il progetto è partito il 2026-09-27 con le **ultime versioni stabili** (LTS per Java, Node e Ubuntu), ora
**congelate**: versioni esatte in `pom.xml`, `package.json` e tag Docker; action della CI fissate per SHA. Non si
aggiornano senza una decisione esplicita, che passa da una change OpenSpec. La matrice completa e le motivazioni sono in
[`openspec/changes/archive/2026-09-27-upgrade-latest-versions/design.md`](openspec/changes/archive/2026-09-27-upgrade-latest-versions/design.md).

| Java | Spring Boot | Node | pnpm | React | Vite | TypeScript | PostgreSQL | Keycloak |
|---|---|---|---|---|---|---|---|---|
| 25.0.4.1 | 4.1.1 | 24.21.0 | 12.6.0 | 19.3.0 | 8.3.1 | 6.0.3 | 18.6 | 26.7.4 |

## Demo online

La demo con dati fittizi è pubblicata su **https://portalenexus.it** (Keycloak su `https://auth.portalenexus.it`).
Architettura e decisioni: [`openspec/changes/archive/2026-09-28-add-demo-deployment/design.md`](openspec/changes/archive/2026-09-28-add-demo-deployment/design.md).

| Cosa | Dove |
|---|---|
| Stack (Caddy, backend, Keycloak, PostgreSQL) | `infra/deploy/compose.yaml` |
| Immagini | `backend/Dockerfile`, `frontend/Dockerfile` + `frontend/Caddyfile` → `ghcr.io/maubernardi/nexus-{backend,web}` |
| Preparazione del server (una volta) | `infra/deploy/bootstrap.sh` |
| Deploy | GitHub → Actions → **Deploy demo** → *Run workflow* (`.github/workflows/deploy-demo.yml`) |

- **Deploy su comando.** Il workflow costruisce le immagini del commit scelto, le pubblica su GHCR e le installa sul
  server con una chiave SSH che può solo eseguire `nexus-deploy`. Se l'avvio fallisce, torna alla release precedente.
- **Segreti** in `/opt/nexus/.env` sul server (generati da `bootstrap.sh`, mai in git); le credenziali demo si
  rileggono con `sudo cat /opt/nexus/.env`.
- **Console di Keycloak** non esposta su Internet: `ssh -L 8081:127.0.0.1:8081 <server>`, poi
  `http://localhost:8081/admin` (utente `admin`, password `KEYCLOAK_ADMIN_PASSWORD` del `.env`).
- **Stato e log sul server:**
  `cd /opt/nexus/current && sudo docker compose --env-file ../.env --env-file release.env ps` (oppure `logs -f backend`).
- **DNS (IONOS):** A `@`, `auth`, `www` → IP del server; CAA `0 issue "letsencrypt.org"`; i record email restano.

## Comandi utili

| | Backend (`backend/`) | Frontend (`frontend/`) |
|---|---|---|
| Build + test | `./mvnw verify` | `pnpm build` · `pnpm test --run` |
| Lint | — | `pnpm lint` · `pnpm format:check` |

## Gestione del progetto (Agile)

Sprint di una settimana, con documenti versionati in [`docs/agile/`](docs/agile/README.md):
[Product Backlog](docs/agile/product-backlog.md) (epiche, storie, criteri di accettazione, domande aperte),
[Roadmap](docs/agile/roadmap.md) (release R1–R4) e un file per [sprint](docs/agile/sprints/).

## Workflow OpenSpec

Ogni funzionalità parte da una change proposal:

1. `/opsx:propose <idea>`: crea proposal, specs (delta), design e tasks in `openspec/changes/<nome>/`
2. `/opsx:apply`: implementa i task
3. `/opsx:verify` e poi `/opsx:archive`: le delta vengono consolidate in `openspec/specs/`

`openspec validate --all --strict` viene eseguito anche in CI.

## Regole Git

- Tutti i **merge** li esegue il maintainer; nessun agente o automazione fa merge.
- Nessun **push diretto su `main`** senza autorizzazione esplicita: si lavora su branch dedicati con Pull Request.

Le regole sono imposte su due livelli:

- **GitHub — branch protection su `main`** (vale per chiunque, admin inclusi):
  - modifiche solo tramite Pull Request;
  - check obbligatori `Backend (Java 25 LTS)`, `Frontend (React PWA)` e `OpenSpec`, verdi e con il branch aggiornato rispetto a `main`;
  - force push e cancellazione di `main` vietati;
  - conversazioni della PR risolte prima del merge.
- **Claude Code** — `.claude/hooks/guard-git.sh` e `.claude/settings.json`: i merge sono bloccati e ogni push verso `main`
  richiede conferma esplicita.

> I check obbligatori sono legati ai nomi dei job in `.github/workflows/ci.yml`: se un job viene rinominato, va aggiornata
> anche la branch protection (Settings → Branches), altrimenti le PR restano in attesa di un check che non arriva mai.
