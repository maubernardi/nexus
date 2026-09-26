## 1. Runtime locale

- [x] 1.1 Installare JDK 27 (Temurin) e Node 26 in locale; verificare `java -version` e `node -v`

## 2. Backend

- [x] 2.1 Aggiornare `pom.xml` a Spring Boot 4.1.1, Java 27, starter modulari, springdoc 3.1.1, Testcontainers 2
- [x] 2.2 Aggiornare il Maven Wrapper a Maven 3.9.16
- [x] 2.3 Adeguare il codice a Jackson 3 e ai nuovi package di test di Boot 4
- [x] 2.4 `./mvnw verify` verde e smoke test dell'avvio reale con profili `local` e `keycloak`

## 3. Frontend

- [x] 3.1 Aggiornare Node/pnpm (`engines`, `packageManager`) e tutte le dipendenze all'ultima versione stabile (TS 6.0.3, vedi design D3)
- [x] 3.2 Migrare da `react-router-dom` 6 a `react-router` 8
- [x] 3.3 Verificare ESLint 10 con jsx-a11y (design D4) e il generatore di asset PWA 2.0 (design D6)
- [x] 3.4 `pnpm lint`, `pnpm format:check`, `pnpm test --run`, `pnpm build` verdi; manifest e `sw.js` generati

## 4. Infra, CI e documentazione

- [x] 4.1 docker-compose con PostgreSQL 18 e Keycloak 26.7.4; container healthy e realm importato
- [x] 4.2 Workflow CI con action alle ultime major, Java 27, Node 26
- [x] 4.3 Aggiornare README, CLAUDE.md, `openspec/config.yaml`
- [x] 4.4 `openspec validate --all --strict`; PR con CI verde
