# NEXUS — istruzioni per Claude Code

- Brief funzionale: `CLAUDE_CODE_PROMPT.md`. Lo stack indicato nel brief (Next.js/Prisma/NextAuth) è **sostituito** da
  Spring Boot (`backend/`) + React/Vite (`frontend/`); vedi `openspec/config.yaml`.
- Sviluppo spec-driven con OpenSpec: ogni nuova funzionalità passa da una change (`/opsx:propose` → `/opsx:apply` → `/opsx:archive`).
- Backend: seguire la skill `java-backend` (package base `it.nexus`, niente SDK `com.btinkeeng.sdk`: gli equivalenti
  sono in `it.nexus.config` e `it.nexus.web.errors`). Ogni endpoint con `@PreAuthorize("hasRole(@requestsAuthorizer.X)")`.
- Frontend: seguire la skill `react-frontend`. Ogni testo passa da `t()` (`src/languages/it.ts`).
- Accessibilità WCAG 2.2 AA obbligatoria: nuovi componenti con test `expectNoAxeViolations` (`src/tests/axe.ts`),
  un solo `h1` per pagina via `PageHeading`, titolo pagina tramite `handle.titleKey` della route.
- PWA: le API non vanno mai messe in cache dal service worker (dati personali, GDPR).
- Verifica prima del commit: `cd backend && ./mvnw verify`; `cd frontend && pnpm lint && pnpm format:check && pnpm test --run && pnpm build`.
