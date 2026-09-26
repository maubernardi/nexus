## Context

Il repository è vuoto: ci sono solo il brief (`CLAUDE_CODE_PROMPT.md`) e OpenSpec. Il brief suggerisce
Next.js + Prisma + NextAuth, ma il progetto usa Spring Boot (Java) e React/Vite: vedi proposal.md, sezione Why.
I requisiti sono in `specs/api-platform`, `specs/pwa-shell` e `specs/accessibility-baseline`.
Vincoli: dati personali di persone fragili (GDPR, minimizzazione), Tutor che usano smartphone con
connettività variabile, operatori CC su desktop.

## Goals / Non-Goals

**Goals:**
- Poter lanciare subito backend, frontend e dipendenze in locale, anche senza Keycloak (profilo mock).
- Definire le convenzioni (layering, errori, sicurezza, a11y, PWA) che ogni change successiva erediterà.
- Una CI che blocca build, test, lint e violazioni di accessibilità.

**Non-Goals:**
- Modello di dominio, FSM, scheduler, documenti Word (change successive).
- Immagini Docker di produzione e deploy.

## Decisions

### D1 — Monorepo con tre radici indipendenti
`backend/` (Maven), `frontend/` (pnpm), `infra/` (docker-compose + realm Keycloak). Tool e CI restano
separati per area, mentre versioni e change OpenSpec avanzano insieme.
*Alternativa:* due repository → scartata: le change OpenSpec toccano quasi sempre entrambi i lati.

### D2 — Backend: package `it.nexus`, niente SDK aziendale
Il layering segue la skill java-backend (`web.rest.resource` → `services`/`services.impl` → `repository` →
`domain`, `domain.dto`, `mapper`). Lo SDK condiviso `com.btinkeeng.sdk` non è disponibile per questo progetto,
quindi nel package `it.nexus.config` e `it.nexus.web.errors` si implementa solo il minimo indispensabile:
- `GlobalExceptionHandler` (`@RestControllerAdvice`), unica traduzione eccezioni → `ApiErrorResponseDTO`;
- `SecurityConfig` (JWT) e `MockSecurityConfig` (`@Profile("security-mock")`);
- `RequestsAuthorizer` con le costanti di ruolo, usate in `@PreAuthorize("hasRole(@requestsAuthorizer.ROLE_X)")`.

### D3 — Sicurezza: Keycloak resource server + mock header
- Profili: `local` = `postgresql, security-mock`; `keycloak` = `postgresql, keycloak`; `test` = Testcontainers + mock.
- In modalità JWT, i realm role Keycloak `TUTOR|CALL_CENTER|ADMIN` diventano `ROLE_*`. Il principal è
  `preferred_username`, le anagrafiche vengono dai claim standard OIDC.
- In modalità mock, l'header `X-USER-ID` seleziona uno degli utenti definiti in `application-security-mock.yml`.
  All'avvio viene loggato un warning. Il profilo mock non entra mai nei gruppi non-locali.
- Deny by default: `anyRequest().authenticated()`. Sono pubblici solo `/actuator/health/**`, `/v3/api-docs/**`
  e `/swagger-ui/**`. 401 e 403 sono serializzati nel formato errori uniforme da un `AuthenticationEntryPoint`
  e un `AccessDeniedHandler` dedicati.
*Alternativa:* sessione server-side / BFF → rimandata. Per una PWA con API stateless i JWT sono più semplici.

### D4 — Persistenza
PostgreSQL 17, Flyway come unica fonte dello schema (`ddl-auto: validate`, `open-in-view: false`).
`V1__init.sql` crea solo lo schema `nexus` e un commento: le tabelle di dominio arrivano con la change del modello ER.
Integration test con Testcontainers tramite `@ServiceConnection`.

### D5 — Frontend: Vite + React 19, atomic design
Struttura e librerie secondo la skill react-frontend. In sviluppo il proxy Vite inoltra `/api` a `localhost:8080`,
così niente CORS in locale. L'autenticazione è astratta in `config/auth`:
- `VITE_AUTH_MODE=mock`: selettore utente di sviluppo, invio di `X-USER-ID`;
- `VITE_AUTH_MODE=keycloak`: `keycloak-js` con PKCE, refresh del token nell'interceptor Axios.

### D6 — PWA con vite-plugin-pwa (Workbox)
- `registerType: 'prompt'`: l'aggiornamento non è mai automatico, per non perdere form in compilazione
  (requisito "Aggiornamento dell'applicazione").
- Precache della shell. Le chiamate `/api/**` sono `NetworkOnly`: in questa fase **nessun dato personale va in
  cache sul dispositivo** (GDPR). Un'eventuale strategia offline per i dati sarà una change dedicata.
- Icone generate da un SVG sorgente (`@vite-pwa/assets-generator`), incluse le varianti maskable e apple-touch.

### D7 — Accessibilità come vincolo verificato
- `eslint-plugin-jsx-a11y` (recommended, strict) in lint con `max-warnings 0`.
- `axe-core` nei test di componenti e pagine, tramite l'helper `expectNoAxeViolations` (`vitest-axe` è fermo da anni e non si usa); `jest-dom` per i ruoli ARIA. La regola sul contrasto è esclusa in jsdom: i token vengono verificati a parte con il calcolo WCAG.
- Palette Tailwind v4 (`@theme`) con token verificati per un contrasto ≥ 4.5:1 su chiaro e scuro, focus ring
  `outline` a 2px con offset, `prefers-reduced-motion` globale, target minimi 24px (44px sui controlli primari mobile).
- Componenti shadcn/ui basati su Radix, che gestiscono già ruoli, focus e tastiera.
- `RouteAnnouncer`: a ogni cambio route aggiorna `document.title` e sposta il focus sull'`h1`.

### D8 — CI GitHub Actions
Un workflow con due job paralleli:
- `backend`: Temurin 21, `./mvnw verify`, con Testcontainers sul Docker del runner;
- `frontend`: Node 22 + pnpm, `lint`, `test`, `build`.

## Risks / Trade-offs

- [In locale c'è JDK 25, il target è 21] → `maven.compiler.release=21`; la CI usa JDK 21; Lombok deve essere una versione compatibile con JDK 25.
- [Il profilo mock finisce per errore in un ambiente reale] → è attivo solo nei gruppi `local`/`test`, logga un warning all'avvio ed è escluso dalle configurazioni di deploy.
- [axe automatico copre solo una parte dei criteri WCAG] → a ogni change UI si aggiunge una checklist manuale (tastiera, screen reader, zoom 200%).
- [Service worker con cache obsoleta] → aggiornamento a prompt + `cleanupOutdatedCaches`.

## Open Questions

- Hosting di produzione e dominio: non influenzano questa change.
