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

### Usare Keycloak reale

```bash
cd backend && PROFILES_ACTIVE=keycloak ./mvnw spring-boot:run
cd frontend && VITE_AUTH_MODE=keycloak pnpm dev
```

### Utenti di test

La password è la stessa per tutti in Keycloak: `password`.

| Username | Ruolo |
|---|---|
| `tutor1`, `tutor2` | TUTOR |
| `operatore.cc` | CALL_CENTER |
| `admin` | ADMIN |

## Profili backend

| `PROFILES_ACTIVE` | Composizione | Autenticazione |
|---|---|---|
| `local` (default) | postgresql, security-mock | header `X-USER-ID` |
| `keycloak` | postgresql, keycloak | JWT Keycloak |
| `test` | Testcontainers, security-mock | header `X-USER-ID` |

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

## Comandi utili

| | Backend (`backend/`) | Frontend (`frontend/`) |
|---|---|---|
| Build + test | `./mvnw verify` | `pnpm build` · `pnpm test --run` |
| Lint | — | `pnpm lint` · `pnpm format:check` |

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
