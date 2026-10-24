# NEXUS — Metodo di lavoro Agile

Il progetto è gestito con un processo **Scrum leggero**, con documenti Markdown versionati nel repository (nessun
strumento esterno). Il *cosa* e il *perché* stanno nel backlog; il *come* nelle change **OpenSpec**.

| Documento | Contenuto |
|---|---|
| [Product Backlog](product-backlog.md) | epiche e storie con priorità, stima, stato, release, criteri di accettazione |
| [Roadmap](roadmap.md) | release (obiettivi) e ordine di massima delle epiche |
| [Sprint](sprints/) | un file per sprint: obiettivo, impegno, esito della review, retrospettiva |

## Ruoli

| Ruolo | Chi | Responsabilità |
|---|---|---|
| **Product Owner** | Mauro Bernardi | ordina il backlog, sceglie l'obiettivo dello sprint, chiarisce i requisiti, **accetta** le storie, esegue merge e deploy |
| **Team di sviluppo** | Claude Code | raffina e stima le storie, le implementa (OpenSpec → codice → test → PR), prepara la demo, propone miglioramenti |

## Cadenza (sprint corti di 3 giorni)

Dal 05/10/2026 (decisione del PO nella review dello Sprint 2): sprint di **3 giorni di calendario** con impegno di circa
**20 punti**. Il PO segue il progetto nel tempo libero e si confronta con il committente quando può: le cerimonie sono
leggere e **asincrone** — review, retrospettiva e planning stanno in un unico documento Markdown preparato dal team e
discusso quando il PO è disponibile. Se lo sprint finisce prima, il team prepara il documento e aspetta; il lavoro non
unito resta nelle PR.

| Momento | Quando | Cosa produce |
|---|---|---|
| **Sprint Planning** | inizio sprint | obiettivo e storie impegnate in `sprints/sprint-NN.md` (il PO sceglie dalla cima del backlog *Ready*) |
| **Lavoro dello sprint** | durante | una change OpenSpec e una PR per storia (o piccolo gruppo coeso); aggiornamenti di stato nel backlog |
| **Sprint Review** | fine sprint | demo **online** su https://portalenexus.it; il PO accetta o rifiuta ogni storia |
| **Retrospettiva** | fine sprint | cosa ha funzionato, cosa migliorare, azioni (nel file dello sprint) |
| **Raffinamento del backlog** | continuo | storie chiarite, stimate, spezzate; domande aperte risolte con il PO |

## Migrazioni del database in PR parallele

Decisione del PO (24/10): ogni PR che aggiunge una migrazione Flyway ne indica il numero nella descrizione. Dopo ogni
merge il team controlla le PR ancora aperte e, se una ha una migrazione con numero ormai superato, la rinumera prima
del merge. La CI rifiuta una migrazione con numero non superiore all'ultima già su `main` (EN-5). Il PO unisce le PR in
qualunque ordine.

## Flusso di una storia

```
📋 Backlog → ✅ Ready → 🔨 In corso → 👀 In review → 🎉 Done
                              │             │            │
                   change OpenSpec      PR aperta    PR unita dal PO,
                   (proposal, spec,     CI verde     deploy demo verificato,
                    design, task)                    change archiviata
```

- **ID**: `US-<epica><progressivo>` (es. `US-301`); abilitatori tecnici `EN-<n>`.
- **Stima**: story point Fibonacci **1, 2, 3, 5, 8**. Una storia da **13** va spezzata prima di entrare in uno sprint.
- **Priorità** (MoSCoW): **Must**, **Should**, **Could**, **Won't** (per ora).
- **Criteri di accettazione** in forma *Dato / Quando / Allora*: diventano gli **scenari** delle spec OpenSpec e i test.

## Definition of Ready

Una storia può entrare in uno sprint quando:
1. ha il formato *Come… voglio… per…* e criteri di accettazione verificabili;
2. è stimata (≤ 8 punti) e sta in uno sprint;
3. le **domande aperte** che la riguardano sono risolte dal PO;
4. le dipendenze (altre storie, dati, template, accessi) sono disponibili o pianificate prima.

## Definition of Done

Una storia è *Done* quando:
1. la change OpenSpec è completa, validata (`openspec validate --strict`) e poi **archiviata** (spec principali aggiornate);
2. il codice ha test: unitari e d'integrazione nel backend, componenti nel frontend con **axe**; i criteri di accettazione sono coperti;
3. le nuove schermate passano un audit **WCAG 2.2 AA** in browser reale (axe con contrasto, tema chiaro e scuro, mobile);
4. la CI è verde e la PR è **unita dal PO** (regole Git inviolabili: nessun merge né push su `main` da parte del team);
5. la demo è **deployata e verificata online**;
6. ogni cambio di stato e ogni scelta introdotti dalla storia sono registrati nell'audit (EN-2, decisione del PO del 01/10);
7. backlog, README e `CLAUDE.md` sono aggiornati se serve.

## Metriche

- **Velocity**: somma dei punti delle storie *Done* per sprint; con sprint da 3 giorni guida la capacità del planning dal terzo sprint corto.
- **Burndown**: nel file dello sprint, punti residui a metà e a fine sprint.
