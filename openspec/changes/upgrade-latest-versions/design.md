## Context

Motivazione: vedi proposal.md. I requisiti non cambiano: le spec di `setup-project-infrastructure` restano il riferimento
e la loro suite di test fa da rete di sicurezza. Data di riferimento per "ultima versione stabile": **2026-09-27**.

## Goals / Non-Goals

**Goals:** stack all'ultima versione stabile (LTS per i runtime) e build riproducibili, con versioni che non cambiano da sole.

**Non-Goals:** refactoring non richiesti dall'aggiornamento; automazione degli aggiornamenti.

## Decisions

### D1 — Criterio di scelta
- **Runtime con linea LTS** (Java, Node.js, Ubuntu): ultima **LTS**.
- **Framework e librerie**: ultima release **GA**, mai milestone, RC o beta.
- **Librerie npm**: ultima release già "maturata" (pnpm per default ignora le release pubblicate da meno di 24 ore,
  come protezione supply-chain). Ad esempio @tanstack/react-query 5.103.2 e react-hook-form 7.88.0: le release
  successive erano uscite da poche ore.

### D2 — Matrice delle versioni congelate

| Componente | Prima | Congelata | Nota |
|---|---|---|---|
| Java (Temurin) | 21 | **25.0.4.1** LTS | 27 è l'ultima GA ma non è LTS |
| Maven | 3.9.12 | **3.9.16** | Maven 4 è ancora in RC |
| Spring Boot | 3.5.16 | **4.1.1** | ultima GA (4.2 è in milestone); supporta Java 25 |
| springdoc-openapi | 2.8.17 | **3.1.1** | linea per Boot 4 |
| Testcontainers | 1.x | **2.0.5** (via BOM) | `org.testcontainers.postgresql.PostgreSQLContainer` |
| Lombok / MapStruct | | **1.18.48 / 1.6.3** | ultime GA |
| Node.js | 24.19 | **24.21.0** LTS | 26 è "Current", diventa LTS a ottobre 2026 |
| npm | | **11.19.0** | quello incluso in Node 24.21.0; il progetto usa pnpm |
| pnpm | 11.20 | **12.6.0** | |
| React / React Router | 19.2.8 / 6 | **19.3.0 / 8.4.0** | `react-router-dom` è deprecato dalla v7 |
| Vite / plugin-react / Vitest | 7.3.1 / 5.2 / 4.1 | **8.3.1 / 6.1.1 / 5.0.2** | alias `@/*` con `resolve.tsconfigPaths` nativo |
| TypeScript | 5.9 | **6.0.3** | vedi D4 |
| ESLint | 9.39 | **10.11.0** | vedi D5 |
| PostgreSQL | 17 | **18.6** | |
| Keycloak | 26.3 | **26.7.4** | |
| Runner CI | ubuntu-latest | **ubuntu-26.04** | ultima LTS |

Le altre dipendenze npm sono tutte a versione esatta in `frontend/package.json`, e il lockfile fissa le transitive.

### D3 — Come si congelano le versioni
- **npm**: versioni esatte in `package.json` (senza `^`/`~`), `saveExact: true` in `pnpm-workspace.yaml`, lockfile con `--frozen-lockfile` in CI.
- **Runtime frontend**: `.nvmrc` = 24.21.0; `devEngines.runtime` (Node 24.21.0) e `devEngines.packageManager`
  (pnpm 12.6.0) con `onFail: error`: pnpm si rifiuta di girare con versioni diverse; `packageManager` fissa pnpm per corepack e CI.
- **Maven**: versioni esplicite nel `pom.xml`, parent Boot 4.1.1 (fissa plugin e dipendenze transitive), wrapper 3.9.16,
  `maven-enforcer-plugin` con JDK `[25,26)` e Maven `[3.9.16]`.
- **Docker**: tag completi (`postgres:18.6-alpine`, `keycloak:26.7.4`), anche nei Testcontainers.
- **CI**: action fissate per **SHA del commit**, con il tag come commento; JDK `25.0.4`; Node da `.nvmrc`; runner `ubuntu-26.04`.
- **Politica**: nessun bump senza richiesta esplicita. Per le CVE si propone l'aggiornamento puntuale e lo si applica dopo approvazione.

### D4 — TypeScript 6.0 invece di 7.0
TypeScript 7 (il compilatore nativo) non è supportato da typescript-eslint (peer `<6.1.0`), su cui si basa il lint,
jsx-a11y compreso. Si usa la 6.0.3. Per la 6.0 è stato rimosso `baseUrl` dai tsconfig, perché è deprecato.

### D5 — Deroghe sui peer dependency (verificate)
- `eslint-plugin-jsx-a11y` 6.10.2 (ultima release) dichiara peer fino a ESLint 9. Con ESLint 10 funziona: le regole
  scattano su un file di prova con violazioni volute.
- `vite-plugin-pwa` 1.3.0 dichiara peer `@vite-pwa/assets-generator ^1`. La 2.0 genera correttamente icone e link.

Entrambe le deroghe sono dichiarate in `pnpm-workspace.yaml` (`peerDependencyRules.allowedVersions`).

### D6 — Spring Boot 4: impatti sul codice
- Starter modulari:
  - `spring-boot-starter-webmvc`, `spring-boot-starter-flyway`, `spring-boot-starter-security-oauth2-resource-server`;
  - `spring-boot-starter-webmvc-test`, `spring-boot-starter-security-test`.
- Jackson 3: `tools.jackson.databind.json.JsonMapper` al posto di `ObjectMapper`. Le annotazioni `com.fasterxml.jackson.annotation` restano.
  Jackson 2 rimane solo come dipendenza transitiva di swagger-core (springdoc).
- `@AutoConfigureMockMvc` si importa da `org.springframework.boot.webmvc.test.autoconfigure`.

### D7 — Librerie transitive: versioni del BOM di Spring Boot
Flyway, Jackson, Hibernate, Tomcat e le altre librerie gestite da Boot restano alle versioni del BOM di Boot 4.1.1
(ad esempio Flyway 12.4). Il BOM è la combinazione che Spring testa insieme; forzare nuove major rischia incompatibilità
con l'autoconfigurazione. Si interviene a mano solo per CVE, con `<xxx.version>` nel pom.

### D8 — Bundle frontend
Con React Router 8 il bundle unico supera i 500 kB. Le librerie vengono separate nei chunk `react` e `vendor`
(`build.rolldownOptions.output.codeSplitting`). `keycloak-js` e `workbox-window` restano import dinamici:
Keycloak non viene scaricato in modalità mock.

### D9 — Pacchetto npm `cn` rimosso
La CLI shadcn aveva generato un import errato (`from "cn"`) e installato il pacchetto npm `cn`, non necessario.
Il pacchetto viene rimosso: `cn()` viene da `@/lib/utils`.

## Risks / Trade-offs

- [Con versioni congelate arrivano fix di sicurezza solo su richiesta] → la politica D3 prevede una proposta puntuale
  per le CVE. Si può valutare più avanti un audit periodico (`pnpm audit`, OWASP dependency-check) che segnala ma non aggiorna.
- [Deroghe sui peer (D5)] → coperte da lint, test e build in CI.
- [Volume Postgres 17 esistente in locale incompatibile con Postgres 18] → `docker compose down -v` (solo dati di sviluppo).
  Con PostgreSQL 18 il volume si monta su `/var/lib/postgresql`.
