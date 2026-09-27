## Why

Il setup iniziale usava le versioni fissate dalle skill (Java 21, Spring Boot 3.5, Vite 7, React 19.2). Spring Boot 3.5,
per esempio, è già fuori dal supporto open-source (giugno 2026). Il committente ha scelto di **partire dalle ultime versioni
stabili disponibili oggi** e di **congelarle**. Il progetto ottiene così una base aggiornata e build riproducibili, senza
aggiornamenti non richiesti.

## What Changes

- **Runtime (ultime LTS)**: Java 21 → **25** (Temurin 25.0.4.1); Node.js → **24.21.0** "Krypton" (con npm 11.19.0 incluso); pnpm **12.6.0**.
- **Backend**:
  - **BREAKING (interno)** Spring Boot 3.5.16 → **4.1.1** (Spring Framework 7, Spring Security 7, Hibernate 7, Jackson 3);
  - starter modulari di Boot 4, springdoc 3.1.1, Testcontainers 2, Maven Wrapper con Maven 3.9.16.
- **Frontend**:
  - React **19.3**, React Router 6 → **8** (pacchetto `react-router`);
  - Vite **8**, Vitest **5**, ESLint **10**, TypeScript **6.0**.
- **Infra**: PostgreSQL **18.6**, Keycloak **26.7.4**.
- **CI**: runner `ubuntu-26.04`, action alle ultime major fissate per SHA.
- **Congelamento delle versioni**:
  - versioni esatte ovunque (`package.json`, `pom.xml`, tag Docker, CI);
  - `saveExact` di pnpm;
  - vincoli bloccanti su JDK/Maven (maven-enforcer) e su Node/pnpm (`devEngines`).
- Aggiornamento della documentazione (README, CLAUDE.md, `openspec/config.yaml`).

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
<!-- nessuna: aggiornamento tecnico senza variazioni di comportamento osservabile (skip_specs) -->

## Non-goals

- Nessuna nuova funzionalità e nessuna modifica ai contratti delle API o della UI.
- Nessuna versione pre-release (milestone, RC, beta) e nessun runtime non-LTS (ad esempio Java 27 e Node 26 restano fuori).
- Nessun aggiornamento automatico (Dependabot/Renovate): dopo questa change le versioni cambiano solo su richiesta esplicita.

## Impact

- Codice backend: import Jackson 3 (`tools.jackson`), package dei test autoconfigure di Boot 4, API Testcontainers 2.
- Codice frontend: import da `react-router`, alias con `resolve.tsconfigPaths` nativo di Vite 8.
- Accessibilità/PWA: nessuna variazione. Restano bloccanti i test axe, il lint jsx-a11y e la verifica di manifest e service worker.
- Chi sviluppa ha bisogno in locale di JDK 25 e Node 24.21.0 (`.nvmrc`); versioni diverse fanno fallire la build.
