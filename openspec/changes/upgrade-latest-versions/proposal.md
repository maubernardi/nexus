## Why

Il progetto è appena nato e conviene partire dalle versioni più recenti dello stack: così si evita subito
un debito di aggiornamento. Il setup iniziale seguiva le versioni fissate dalle skill (Java 21, Spring Boot 3.5,
Vite 7, React 19.2), e il supporto open-source di Spring Boot 3.5 è già terminato (giugno 2026). La scelta di
usare sempre le ultime versioni stabili è del committente.

## What Changes

- **Runtime**:
  - Java 21 → **27**;
  - Node.js 24 → **26**, npm **12.1**, pnpm **12.6**.
- **Backend**:
  - **BREAKING (interno)** Spring Boot 3.5.16 → **4.1.1** (Spring Framework 7, Spring Security 7, Hibernate 7, Jackson 3);
  - starter modulari di Boot 4;
  - springdoc 3.1, Testcontainers 2.0, Flyway 11 → quella gestita da Boot 4.1;
  - Maven Wrapper con Maven 3.9.16.
- **Frontend**:
  - React **19.3**, React Router 6 → **8** (pacchetto `react-router`);
  - Vite **8**, @vitejs/plugin-react 6, Vitest **5**;
  - ESLint **10**, TypeScript **6.0**, @types/node 26;
  - shadcn CLI 4, jsdom 30.
- **Infra**: PostgreSQL 17 → **18**, Keycloak 26.3 → **26.7.4**.
- **CI**: actions/checkout v7, setup-java v6, setup-node v7, pnpm/action-setup v6; job su Java 27 e Node 26.
- Aggiornamento della documentazione (README, CLAUDE.md, `openspec/config.yaml`).

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
<!-- nessuna: aggiornamento tecnico senza variazioni di comportamento osservabile (skip_specs) -->

## Non-goals

- Nessuna nuova funzionalità e nessuna modifica ai contratti delle API o della UI.
- Nessuna adozione di versioni pre-release (milestone, RC, beta): ad esempio Spring Boot 4.2 M2 e Maven 4 RC restano fuori.

## Impact

- Codice backend: import Jackson 3 (`tools.jackson`), package dei test autoconfigure di Boot 4, coordinate Testcontainers 2.
- Codice frontend: import da `react-router`, configurazione ESLint 10.
- Accessibilità/PWA: nessuna variazione attesa; restano bloccanti i test axe, il lint jsx-a11y e la verifica di manifest e service worker.
- Chi sviluppa ha bisogno in locale di JDK 27 e Node 26.
