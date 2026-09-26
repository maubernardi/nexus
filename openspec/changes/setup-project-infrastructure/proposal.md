## Why

NEXUS parte da zero: prima di implementare il dominio (Ticket, FSM, Bacheca, documenti Word) serve
una base tecnica condivisa. Backend Java, frontend React PWA, database, autenticazione, CI e
accessibilità devono essere impostati bene fin dall'inizio, perché ogni feature successiva ci si appoggia.
Il brief (`CLAUDE_CODE_PROMPT.md`) suggerisce Next.js/Prisma. Il progetto adotta invece Spring Boot
+ React/Vite: questa change fissa quella scelta.

## What Changes

- Struttura monorepo: `backend/`, `frontend/`, `infra/`, `openspec/`, `.github/`.
- **Backend** Spring Boot 3.5 / Java 21 (Maven wrapper):
  - PostgreSQL + Flyway (migrazione iniziale vuota), Actuator health.
  - Spring Security OAuth2 resource server (Keycloak) con profilo `security-mock` per lo sviluppo locale.
  - Gestione errori centralizzata (`ApiErrorResponseDTO`), OpenAPI/Swagger UI.
  - Endpoint `GET /api/v1/me` con l'utente autenticato e i suoi ruoli (`TUTOR`, `CALL_CENTER`, `ADMIN`).
  - Test unitari + integration test con Testcontainers.
- **Frontend** React 19 + Vite 7 + TypeScript strict:
  - Tailwind v4 + shadcn/ui, i18next (italiano), TanStack Query, Zustand, react-router, Axios.
  - Configurato come **PWA** installabile (manifest, icone, service worker Workbox, pagina offline).
  - App shell accessibile (skip link, landmark, focus visibile, supporto tastiera, `prefers-reduced-motion`).
- **Accessibilità**: baseline WCAG 2.2 AA verificata automaticamente (eslint-plugin-jsx-a11y, test axe).
- **Infra locale**: `docker-compose` con PostgreSQL 17 e Keycloak (realm `nexus` importato con i ruoli e gli utenti di test).
- **CI**: workflow GitHub Actions per build, test e lint di backend e frontend.
- README con le istruzioni di avvio.

## Capabilities

### New Capabilities
- `api-platform`: contratto trasversale delle API: autenticazione JWT/deny-by-default, formato errori uniforme, health check, endpoint identità utente corrente.
- `pwa-shell`: applicazione frontend installabile come PWA, con shell offline e navigazione di base per ruolo.
- `accessibility-baseline`: requisiti WCAG 2.2 AA trasversali a tutta l'interfaccia e loro verifica automatica.

### Modified Capabilities
<!-- nessuna: primo insieme di specifiche -->

## Non-goals

- Nessuna entità di dominio (User, Project, Candidate, Ticket, Company, JobSlot, BoardPost): arriverà nella change successiva (schema DB + seed).
- Nessuna logica FSM, scheduler timer, generazione `.docx`.
- Nessun deploy in ambienti remoti (solo sviluppo locale + CI).
- Il login reale con Keycloak nel frontend viene predisposto ma non è obbligatorio in questa fase: in locale si usa il profilo mock.

## Impact

- Nuovo codice: `backend/`, `frontend/`, `infra/`, `.github/workflows/`.
- Dipendenze: Spring Boot 3.5.x, PostgreSQL 17, Keycloak 26, Node 22+/pnpm, vite-plugin-pwa.
- Accessibilità/PWA: definisce le regole che ogni change frontend successiva dovrà rispettare.
