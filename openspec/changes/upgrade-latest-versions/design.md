## Context

Stato attuale e motivazione: vedi proposal.md. I requisiti non cambiano: le spec della change
`setup-project-infrastructure` restano il riferimento e la loro suite di test è la rete di sicurezza di questo aggiornamento.

## Goals / Non-Goals

**Goals:** ogni componente all'ultima versione **stabile** disponibile al 2026-09-27, con build, test, lint e CI verdi.

**Non-Goals:** refactoring non richiesti dall'aggiornamento.

## Decisions

### D1 — Matrice delle versioni

| Componente | Prima | Dopo | Nota |
|---|---|---|---|
| Java | 21 | 27 | vedi D2 |
| Spring Boot | 3.5.16 | 4.1.1 | ultima GA (4.2 è in milestone) |
| springdoc-openapi | 2.8.17 | 3.1.1 | linea per Boot 4 |
| Testcontainers | 1.x (via Boot) | 2.0.5 (via Boot) | nuove coordinate `testcontainers-*`, `org.testcontainers.postgresql.PostgreSQLContainer` |
| Lombok / MapStruct | 1.18.48 / 1.6.3 | invariati | già le ultime GA; Lombok 1.18.48 supporta JDK 27 |
| Maven | 3.9.12 | 3.9.16 | Maven 4 è ancora in RC |
| Node.js | 24 LTS | 26 | ultima release (diventa LTS a ottobre 2026) |
| pnpm / npm | 11.20 / 12.0 | 12.6 / 12.1 | |
| React | 19.2.8 | 19.3.0 | |
| React Router | 6 (`react-router-dom`) | 8 (`react-router`) | `react-router-dom` è deprecato dalla v7 |
| Vite / plugin-react | 7.3.1 / 5.2 | 8.3 / 6.1 | alias `@/*` con `resolve.tsconfigPaths` nativo: rimosso `vite-tsconfig-paths` |
| Vitest | 4.1 | 5.0 | |
| ESLint | 9.39 | 10.x | vedi D4 |
| TypeScript | 5.9 | 6.0.3 | vedi D3 |
| PostgreSQL | 17 | 18 | |
| Keycloak | 26.3 | 26.7.4 | |

### D2 — Java 27 anche se Spring Boot 4.1 dichiara compatibilità fino a Java 26
Java 26 non è LTS e il suo supporto è terminato con l'uscita della 27 (settembre 2026): restare sulla 26 non avrebbe senso.
Le scelte possibili sono due:
- 27, l'ultima versione ma fuori dalla matrice ufficiale di Boot 4.1;
- 25, l'ultima LTS, supportata ufficialmente.

Si adotta **27** perché il committente chiede le ultime versioni. La condizione è che `./mvnw verify`
(unit, integration e avvio reale) sia verde. Il supporto ufficiale arriverà con Boot 4.2 (novembre 2026).
*Rollback:* una sola proprietà (`java.version`) e la versione JDK in CI.

### D3 — TypeScript 6.0 invece di 7.0
TypeScript 7 (il compilatore nativo) non è ancora supportato da typescript-eslint (peer `<6.1.0`), su cui si basa
il lint, jsx-a11y compreso. Si usa la 6.0.3, l'ultima compatibile. Si passerà alla 7 quando typescript-eslint la supporterà.

### D4 — ESLint 10 ed eslint-plugin-jsx-a11y
Il plugin ufficiale jsx-a11y (6.10.2, ultima release) dichiara peer fino a ESLint 9. Il plugin si usa in flat config e
non dipende dalle API rimosse in ESLint 10, quindi va verificato che funzioni. Se funziona, si tiene il plugin ufficiale
con una deroga esplicita sul peer. Se non funziona, si passa al fork `eslint-plugin-jsx-a11y-x` (peer ESLint 10).

### D5 — Spring Boot 4: impatti sul codice
- Starter modulari:
  - `spring-boot-starter-webmvc`, `spring-boot-starter-flyway`, `spring-boot-starter-security-oauth2-resource-server`;
  - starter di test per modulo.
- Jackson 3 (`tools.jackson.databind`) per `ObjectMapper`/`JsonMapper`. Le annotazioni `com.fasterxml.jackson.annotation` restano.
- Le annotazioni di test (`@AutoConfigureMockMvc`) passano ai nuovi package `org.springframework.boot.*.test.autoconfigure`.

### D6 — @vite-pwa/assets-generator
vite-plugin-pwa 1.3 (ultima) dichiara peer `^1` per il generatore di icone, mentre è uscita la 2.0. Si prova la 2.0
e si verifica che la build generi le icone. Se non le genera, si resta sulla 1.x fino a un aggiornamento del plugin.

### D7 — Librerie transitive: versioni del BOM di Spring Boot
Per le librerie gestite da Spring Boot si usa la versione del BOM di Boot 4.1.1 (l'ultima release), anche quando esiste
una release più recente. Ad esempio Flyway 12.4 invece di 13.8, Jackson 3.1 invece di 3.2, Hibernate 7.4.5 invece di 7.4.10.
Il BOM è la combinazione che Spring testa insieme. Forzare le versioni, soprattutto una nuova major come Flyway 13, espone
a incompatibilità con l'autoconfigurazione senza benefici concreti. Le versioni avanzeranno con i rilasci di Boot.
Si interviene a mano solo per fix di sicurezza (CVE), con la proprietà `<xxx.version>` nel pom.

### D8 — Bundle frontend
Con React Router 8 il bundle unico supera i 500 kB. Le librerie vengono separate nei chunk `react` e `vendor`
(`build.rolldownOptions.output.codeSplitting`), che restano in cache tra un rilascio e l'altro. `keycloak-js` e
`workbox-window` restano import dinamici: Keycloak non viene scaricato in modalità mock.

## Risks / Trade-offs

- [Combinazioni fuori matrice ufficiale: Java 27 con Boot 4.1, ESLint 10 con jsx-a11y] → coperte da test e CI; rollback descritti sopra.
- [Node 26 non ancora LTS] → diventa LTS a ottobre 2026; nessuna azione.
- [Volume Postgres 17 esistente in locale incompatibile con Postgres 18] → documentare `docker compose down -v`
  (sono solo dati di sviluppo). Con PostgreSQL 18 il mount del volume passa a `/var/lib/postgresql`.
