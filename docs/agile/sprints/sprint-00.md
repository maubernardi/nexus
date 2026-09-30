# Sprint 0 — Fondamenta (26–29 settembre 2026)

**Obiettivo:** base tecnica solida e demo online, prima delle funzionalità di dominio.
**Esito:** ✅ raggiunto. Lavoro non stimato in story point (esclusa dal calcolo della velocity).

## Lavoro completato

| PR | Contenuto | Change OpenSpec |
|---|---|---|
| — | Setup iniziale: backend Spring Boot, frontend React PWA, accessibilità WCAG 2.2 AA, CI | `setup-project-infrastructure` |
| #1 | Stack alle ultime versioni stabili (Java 25 LTS, Spring Boot 4.1, Node 24 LTS…) e **congelamento** | `upgrade-latest-versions` |
| #2, #3 | Regole Git inviolabili (merge solo del PO, niente push su `main`), hook, branch protection | — |
| #4, #5 | **Modello dati** (15 tabelle, vincoli nel DB, audit trail immodificabile), utenti censiti, seed | `add-domain-model` |
| #6, #7 | **Demo online** su portalenexus.it: Docker, Caddy HTTPS, Keycloak, deploy su comando con chiave limitata | `add-demo-deployment` |
| #8, #9 | **Restyle** con la palette irlandese (verde e oro), verificato WCAG AA | `apply-irish-palette` |
| #10 | Intestazione su una sola riga su mobile | — |

Valutazioni fatte e chiuse: Logto come alternativa a Keycloak (scartato: pagina di login non conforme WCAG).

## Retrospettiva

**Cosa ha funzionato**
- Decisioni prese passo passo con il PO prima di scrivere codice (modello ER, deploy).
- Test che hanno trovato bug reali prima del rilascio (duplicati silenziosi, schema delle query native, CSP, header mobile).
- Stack di produzione provato per intero in locale prima del server.

**Cosa migliorare**
- Due errori negli script del server (`firewall-cmd`, cache degli URL raw) hanno richiesto un secondo giro.
- Verifiche visive su mobile da fare **prima** della PR, non dopo il deploy.

**Azioni**
- Comandi da eseguire sul server: sempre con URL per commit (non per branch).
- Ogni storia con interfaccia: controllo automatico multi-larghezza (320–1280 px) e axe con contrasto nella Definition of Done.
