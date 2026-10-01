# NEXUS — istruzioni per Claude Code

## ⛔ Regole inviolabili (Git/GitHub)

1. **Tutti i merge li fa l'utente.** Mai eseguire merge: né `gh pr merge`, né `git merge`, né merge via API/UI.
   Il lavoro si consegna su un branch dedicato con una Pull Request; l'utente decide se e quando fare il merge.
2. **Nessun push su `main` senza il permesso esplicito dell'utente**, chiesto e ottenuto ogni volta, per QUALUNQUE modifica
   (anche solo documentazione). Il flusso normale è: branch → push del branch → PR.

Le regole sono imposte anche tecnicamente da `.claude/hooks/guard-git.sh` (PreToolUse) e da regole `deny` in
`.claude/settings.json`: non aggirarle, non modificarle né disattivarle senza una richiesta esplicita dell'utente.

## Linee guida

- **Processo Agile** (documenti in `docs/agile/`): sprint di 1 settimana, storie in `product-backlog.md` stimate in story
  point Fibonacci, Definition of Ready/Done in `docs/agile/README.md`. Ogni storia (o piccolo gruppo coeso) → una change
  OpenSpec → una PR; i criteri di accettazione diventano scenari delle spec. Aggiornare lo stato delle storie nel backlog e
  il file dello sprint corrente; non iniziare storie che non soddisfano la Definition of Ready (domande aperte irrisolte).

- **Versioni congelate** (matrice in `openspec/changes/archive/2026-09-27-upgrade-latest-versions/design.md`): stack fissato il 2026-09-27
  alle ultime versioni stabili, LTS per i runtime (Java 25, Node 24.21.0). Prevalgono sulle versioni indicate dalle skill.
  NON aggiornare nulla senza una richiesta esplicita; una nuova dipendenza si aggiunge all'ultima versione stabile
  compatibile, a versione esatta (pnpm ha `saveExact`).
- Brief funzionale: `CLAUDE_CODE_PROMPT.md`. Lo stack indicato nel brief (Next.js/Prisma/NextAuth) è **sostituito** da
  Spring Boot (`backend/`) + React/Vite (`frontend/`); vedi `openspec/config.yaml`.
- Sviluppo spec-driven con OpenSpec: ogni nuova funzionalità passa da una change (`/opsx:propose` → `/opsx:apply` → `/opsx:archive`).
- Backend: seguire la skill `java-backend` (package base `it.nexus`, niente SDK `com.btinkeeng.sdk`: gli equivalenti
  sono in `it.nexus.config` e `it.nexus.web.errors`). Ogni endpoint con `@PreAuthorize("hasRole(@requestsAuthorizer.X)")`.
- **Terminologia**: il soggetto della segnalazione è il **beneficiario** (`Beneficiary` nel codice, `beneficiary` nel
  database e nelle API, "beneficiario" nei testi), mai "candidato". Un beneficiario ha **una sola segnalazione aperta**
  alla volta (aperta = ogni stato tranne `FORM_RESTITUZIONE`).
- **Audit** (decisione del PO): ogni cambio di stato e ogni scelta (presa in carico, abbinamento, approvazioni, rifiuti
  con motivo, configurazioni) va registrato in modo immodificabile: autore, istante, prima/dopo, motivo (EN-2).
- Modello dati (`it.nexus.domain`):
  - le entità estendono `AbstractTsidEntity` (id TSID + audit), `AbstractReferenceEntity` per le tabelle con codice
    oppure `AbstractCreationAuditingEntity` per le righe non modificabili;
  - le entità con chiave composta implementano `Persistable`, altrimenti un duplicato diventa un update silenzioso;
  - nei DTO l'id è `String` (`TSID.from(id).toString()`), mai un numero;
  - ogni vincolo va messo anche nel database (`CHECK`/`UNIQUE`/FK con nome esplicito) e testato per nome come in
    `DomainConstraintsIT`;
  - `toString()` e log senza dati personali.
- Frontend: seguire la skill `react-frontend`. Ogni testo passa da `t()` (`src/languages/it.ts`).
- Accessibilità WCAG 2.2 AA obbligatoria: nuovi componenti con test `expectNoAxeViolations` (`src/tests/axe.ts`),
  un solo `h1` per pagina via `PageHeading`, titolo pagina tramite `handle.titleKey` della route.
- PWA: le API non vanno mai messe in cache dal service worker (dati personali, GDPR).
- Verifica prima del commit: `cd backend && ./mvnw verify`; `cd frontend && pnpm lint && pnpm format:check && pnpm test --run && pnpm build`.
