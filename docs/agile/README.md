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

## Cadenza (sprint di 1 settimana)

| Momento | Quando | Cosa produce |
|---|---|---|
| **Sprint Planning** | inizio sprint | obiettivo e storie impegnate in `sprints/sprint-NN.md` (il PO sceglie dalla cima del backlog *Ready*) |
| **Lavoro dello sprint** | durante | una change OpenSpec e una PR per storia (o piccolo gruppo coeso); aggiornamenti di stato nel backlog |
| **Sprint Review** | fine sprint | demo **online** su https://portalenexus.it; il PO accetta o rifiuta ogni storia |
| **Retrospettiva** | fine sprint | cosa ha funzionato, cosa migliorare, azioni (nel file dello sprint) |
| **Raffinamento del backlog** | continuo | storie chiarite, stimate, spezzate; domande aperte risolte con il PO |

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
6. backlog, README e `CLAUDE.md` sono aggiornati se serve.

## Metriche

- **Velocity**: somma dei punti delle storie *Done* per sprint; dalla terza settimana guida la capacità del planning.
- **Burndown**: nel file dello sprint, punti residui a metà e a fine sprint.
