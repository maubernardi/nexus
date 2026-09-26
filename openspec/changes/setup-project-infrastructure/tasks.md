## 1. Repository e infrastruttura locale

- [x] 1.1 Creare `.gitignore`, `.editorconfig`, `.gitattributes` alla radice (esclusi segreti, `.env*`, build output)
- [x] 1.2 Creare `infra/docker-compose.yml` con PostgreSQL 17 e Keycloak 26 (healthcheck) e `infra/.env.example`
- [x] 1.3 Creare `infra/keycloak/nexus-realm.json` con i ruoli `TUTOR`, `CALL_CENTER`, `ADMIN`, il client pubblico `nexus-frontend` (PKCE) e utenti di test
- [x] 1.4 Verificare `docker compose up` → Postgres e Keycloak healthy

## 2. Backend Spring Boot

- [x] 2.1 Generare il progetto Maven (Spring Boot 3.5, Java 21, wrapper) con Web, Validation, Data JPA, PostgreSQL, Flyway, Security, OAuth2 Resource Server, Actuator, springdoc, Lombok, MapStruct, Testcontainers
- [x] 2.2 Configurare `application.yml` a gruppi di profili (`local`, `keycloak`, `test`) con `${ENV_VAR:default}`
- [x] 2.3 Aggiungere `V1__init.sql` (schema `nexus`) e verificare che Flyway venga applicato all'avvio
- [x] 2.4 Implementare `ApiErrorResponseDTO`, le eccezioni di dominio e il `GlobalExceptionHandler` (404, 400 con fieldErrors, 500 generico)
- [x] 2.5 Implementare `SecurityConfig` (JWT Keycloak → `ROLE_*`), `MockSecurityConfig` (`X-USER-ID`), `RequestsAuthorizer`, entry point 401/403 JSON
- [x] 2.6 Implementare `GET /api/v1/me` (Resource → Service → `CurrentUserDTO`) con `@PreAuthorize`
- [x] 2.7 Configurare OpenAPI (schema bearer) e CORS da proprietà
- [x] 2.8 Test: `CurrentUserServiceTest` (Mockito) e `CurrentUserResourceIT` (200/401/403, formato errori, health pubblico), eseguiti con `./mvnw verify` verde

## 3. Frontend React PWA

- [x] 3.1 Scaffold Vite 7 + React 19 + TS strict con pnpm, alias `@/*`, struttura atomic design
- [x] 3.2 Configurare Tailwind v4 + shadcn/ui (`components.json`, `cn()`), token di colore AA per chiaro/scuro, focus ring e reduced-motion
- [x] 3.3 Configurare i18next (it), TanStack Query, Zustand, react-router, istanza Axios con interceptor auth (mock/keycloak)
- [x] 3.4 Configurare vite-plugin-pwa (manifest, icone generate, `registerType: prompt`, API NetworkOnly) e il proxy `/api`
- [x] 3.5 Implementare l'app shell accessibile: skip link, header con utente/ruolo, nav responsive, `main`, footer, RouteAnnouncer, pagine Home e 404
- [x] 3.6 Implementare gli avvisi PWA: banner offline e prompt di aggiornamento, entrambi con regioni live
- [x] 3.7 Configurare ESLint (typescript-eslint, react-hooks, jsx-a11y) e Prettier; `pnpm lint` con zero warning
- [x] 3.8 Configurare Vitest + Testing Library + axe-core; test di shell, skip link e banner senza violazioni axe
- [x] 3.9 Verificare che `pnpm build` produca `manifest.webmanifest` e `sw.js`

## 4. CI e documentazione

- [x] 4.1 Creare `.github/workflows/ci.yml` con i job backend e frontend
- [x] 4.2 Scrivere `README.md` (architettura, prerequisiti, avvio locale, profili, utenti di test, workflow OpenSpec)
- [x] 4.3 Eseguire `openspec validate setup-project-infrastructure --strict` senza errori
- [x] 4.4 Primo commit e push su `github.com/maubernardi/nexus`, CI verde
