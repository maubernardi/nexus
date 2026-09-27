# NEXUS — istruzioni per Claude Code

## ⛔ Regole inviolabili (Git/GitHub)

1. **Tutti i merge li fa l'utente.** Mai eseguire merge: né `gh pr merge`, né `git merge`, né merge via API/UI.
   Il lavoro si consegna su un branch dedicato con una Pull Request; l'utente decide se e quando fare il merge.
2. **Nessun push su `main` senza il permesso esplicito dell'utente**, chiesto e ottenuto ogni volta, per QUALUNQUE modifica
   (anche solo documentazione). Il flusso normale è: branch → push del branch → PR.

Le regole sono imposte anche tecnicamente da `.claude/hooks/guard-git.sh` (PreToolUse) e da regole `deny` in
`.claude/settings.json`: non aggirarle, non modificarle né disattivarle senza una richiesta esplicita dell'utente.

## Linee guida

- **Versioni congelate** (matrice in `openspec/changes/archive/2026-09-27-upgrade-latest-versions/design.md`): stack fissato il 2026-09-27
  alle ultime versioni stabili, LTS per i runtime (Java 25, Node 24.21.0). Prevalgono sulle versioni indicate dalle skill.
  NON aggiornare nulla senza una richiesta esplicita; una nuova dipendenza si aggiunge all'ultima versione stabile
  compatibile, a versione esatta (pnpm ha `saveExact`).
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
