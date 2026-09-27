## 1. Runtime locale

- [x] 1.1 Verificare JDK 25 LTS (Temurin/OpenJDK 25.0.4.1) e installare Node 24.21.0 LTS e pnpm 12.6.0; creare `.nvmrc`

## 2. Backend

- [x] 2.1 Aggiornare `pom.xml` a Spring Boot 4.1.1, Java 25, starter modulari, springdoc 3.1.1, Testcontainers 2
- [x] 2.2 Aggiornare il Maven Wrapper a Maven 3.9.16 e aggiungere maven-enforcer (JDK [25,26), Maven [3.9.16])
- [x] 2.3 Adeguare il codice a Jackson 3, ai package di test di Boot 4 e all'API Testcontainers 2
- [x] 2.4 `./mvnw clean verify` verde; smoke test dell'avvio reale con profili `local` e `keycloak`

## 3. Frontend

- [x] 3.1 Aggiornare le dipendenze all'ultima versione stabile e fissarle a versione esatta (TS 6.0.3, vedi D4)
- [x] 3.2 Migrare da `react-router-dom` 6 a `react-router` 8
- [x] 3.3 Verificare ESLint 10 con jsx-a11y e assets-generator 2.0 (D5); rimuovere `vite-tsconfig-paths` e il pacchetto `cn`
- [x] 3.4 Congelare runtime e tool: `saveExact`, `devEngines` (Node 24.21.0, pnpm 12.6.0, `onFail: error`)
- [x] 3.5 `pnpm lint`, `pnpm format:check`, `pnpm test --run`, `pnpm build` verdi su Node 24.21.0; manifest e `sw.js` generati

## 4. Infra, CI e documentazione

- [x] 4.1 docker-compose con `postgres:18.6-alpine` e Keycloak 26.7.4; container healthy e realm importato
- [x] 4.2 Workflow CI su `ubuntu-26.04`, action fissate per SHA, JDK 25.0.4, Node da `.nvmrc`
- [x] 4.3 Aggiornare README, CLAUDE.md, `openspec/config.yaml` con la matrice congelata e la politica
- [ ] 4.4 `openspec validate --all --strict`; PR con CI verde
