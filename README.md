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
├── backend/    Spring Boot 4.1 · Java 27 · PostgreSQL · Flyway · Spring Security 7 (Keycloak JWT)
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

- Java 27 (es. Temurin; il wrapper Maven è incluso)
- Node.js 26 e pnpm 12 (`npm i -g pnpm`; da Node 25 corepack non è più incluso)
- Docker (per PostgreSQL, Keycloak e i test Testcontainers)

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
