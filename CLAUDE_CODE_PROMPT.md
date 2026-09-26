# SYSTEM PROMPT / BRIEF PER CLAUDE CODE
**Progetto:** NEXUS — Progressive Web App per l'Area Lavoro e Call Center Sociale
**Versione Specifiche:** 5.0 Final
**Obiettivo:** Iniziare il coding dell'applicazione gestendo backend, frontend PWA, database, FSM e generazione documentale Word.

---

## 1. VISIONE GENERALE E ARCHITETTURA

NEXUS è una PWA (Progressive Web App) multi-progetto sviluppata per la gestione e il tracciamento dei tirocini dell'Area Lavoro di una Cooperativa Sociale.
Connette due ruoli chiave:
1. **Tutor territoriali (Mobile-first):** Compilano le segnalazioni sui tirocinanti, consultano la bacheca opportunita, gestiscono le schede e avviano i tirocini.
2. **Operatori Call Center (Desktop):** Lavorano le segnalazioni in coda FIFO, effettuano il matching con il DB Aziende, validano/pubblicano gli annunci in bacheca e gestiscono il Fast-Track.

### Tech Stack Consigliato
- **Frontend:** Next.js (React), TailwindCSS, Lucide Icons, PWA Support (next-pwa / service workers).
- **Backend:** Node.js (Next.js API Routes / App Router Server Actions).
- **Database:** PostgreSQL con Prisma ORM.
- **Generazione Documenti:** `docxtemplater` + `pizzip` (per la compilazione dei placeholder Word `.docx`).
- **Autenticazione & RBAC:** NextAuth.js / Auth.js con ruolo utente e assegnazione Progetti.

---

## 2. MODELLO ER (DATABASE SCHEMA PRISMA)

Crea uno schema Prisma che rifletta esattamente i seguenti modelli principali:

### `User`
- `id` (String, PK)
- `name`, `surname`, `email`, `phone`
- `role` (Enum: `TUTOR`, `CALL_CENTER`, `ADMIN`)
- `projects` (Relation: Many-to-Many con `Project`)

### `Project`
- `id` (String, PK)
- `code` (String, es. "GOL", "POLIS", "FSE")
- `name` (String)

### `Candidate` (Minimizzazione GDPR)
- `id` (String, PK)
- `firstName`, `lastName` (visibili solo al Tutor proprietario e CC)
- `birthYear` (Int), `gender` (String)
- `nationality`, `citizenship` (String)
- `residenceZone` (String)
- `hasDrivingLicense` (Boolean), `licenseTypes` (String[])
- `hasVehicle` (Boolean), `transportMode` (String)
- `hasLaw68` (Boolean - Collocamento Mirato L. 68/99)
- `education` (String), `constraints` (String - es. barriere, allergie)
- `languages` (Json - es. `[{lang: "Italiano", level: "C2"}]`)
- `cvUrl` (String, optional)

### `Ticket` (Segnalazione / Percorso Tirocinio)
- `id` (String, PK - Progressivo univoco es. `TICK-0001`)
- `tutorId` (FK User)
- `projectId` (FK Project)
- `candidateId` (FK Candidate)
- `type` (Enum: `NORMAL`, `SPECIAL`)
- `status` (Enum: `NUOVA`, `IN_ATTESA_APPROVAZIONE_ADMIN`, `IN_LAVORAZIONE`, `PROPOSTA_AZIENDA`, `PROPOSTA_ACCOLTA`, `APPUNTAMENTO`, `IN_TIROCINIO`, `FORM_RESTITUZIONE`, `RIAPERTO`)
- `isFastTrack` (Boolean - true se nasce da Segnalazione Speciale)
- `priority` (String, default: "FIFO")
- `assignedCcOperatorId` (FK User, optional)
- `companyId` (FK Company, optional)
- `jobSlotId` (FK JobSlot, optional)
- `blacklistedCompanyIds` (String[])
- `timerStartDate` (DateTime, optional - Timer 7 giorni lavorativi)
- `timerReminderSent` (Boolean, default: false)
- `contractData` (Json, optional - Dati Fase 7: orari, sospensioni, CF, ecc.)
- `closureReport` (Json, optional - Form restituzione finale)

### `Company` & `JobSlot` (Mansioni)
- `Company`: `id`, `name`, `vatCode`, `legalAddress`, `contactPerson`, `phone`, `auditTrail` (Json)
- `JobSlot`: `id`, `companyId`, `title`, `description`, `zone`, `status` (Enum: `LIBERA`, `BLOCCATA`)

### `BoardPost` (Bacheca Annunci)
- `id` (String, PK - Progressivo unico es. `#1`, `#2`)
- `title` / `jobTitle` (String)
- `companyId` (FK Company - Nascosto ai Tutor)
- `zone` (String), `ageRange` (String), `workHours` (String), `duration` (String)
- `notes` (String)
- `projectId` (FK Project, optional - Se NULL, visibile a tutti. Se presente, visibile solo al Progetto)
- `publishedAt` (DateTime, optional)
- `status` (Enum: `DRAFT`, `PUBLISHED`, `ARCHIVED`)
- `createdFromTicketId` (FK Ticket, optional - Se nato da un Rilancio in Fase 6)

---

## 3. MACCHINA A STATI FINITA (FSM) E LOGICHE DI BUSINESS

Implementa rigorosamente le seguenti fasi di transizione di stato:

[SEGNALAZIONE NORMALE] ───────> [NUOVA] ──(Lavorazione FIFO)──> [IN LAVORAZIONE]
│
[SEGNALAZIONE SPECIALE (Bacheca)] ───> [VALIDAZIONE CC] ──────────────┤
│                       │
(Se Approvata)           (Se Rifiutata)
│                       │
▼                       ▼
[PROPOSTA AZIENDA] <──────────────┘
│
[PROPOSTA ACCOLTA] ──(Timer 7gg Lav)──> [APPUNTAMENTO] ──> [IN TIROCINIO] ──> [REPORT]

### Regole chiave da codificare:

1. **Fase 1 (Inserimento):**
   - **Normale:** Crea Ticket `NUOVA` ed entra nella coda FIFO. (Se la mansione è testo libero $\rightarrow$ `IN_ATTESA_APPROVAZIONE_ADMIN`).
   - **Speciale:** Creata a partire da un `BoardPost` `#N` attivo. Imposta `type = SPECIAL`, `isFastTrack = true` e stato = `IN_LAVORAZIONE` con badge prioritario per il CC.

2. **Fase 3 (Lavorazione CC e Fast-Track):**
   - **Segnalazione Speciale:**
     - **Approva:** Passa il ticket a `PROPOSTA_AZIENDA`, imposta il `JobSlot` a `BLOCCATA` e imposta il `BoardPost` a `ARCHIVED`.
     - **Rifiuta:** Imposta `isFastTrack = false`, declassa a segnalazione normale `IN_LAVORAZIONE` (lasciando il `BoardPost` in stato `PUBLISHED`).

3. **Fase 4 (Abbinamento CC):**
   - Assegna `Company` e `JobSlot` (che passa a `BLOCCATA`). Passa lo stato a `PROPOSTA_AZIENDA`.

4. **Fase 5 (Valutazione Tutor & Timer Lavorativo):**
   - **Proposta Accolta:** Passa a `PROPOSTA_ACCOLTA`. Avvia un **Timer di 7 Giorni Lavorativi** (escludendo sabati e domeniche).
   - **Cronjob / Scheduler Timer:** Al **4° giorno lavorativo** invia una notifica reminder unica. Al **7° giorno lavorativo** senza azioni, scatta il **Timeout** $\rightarrow$ Mansione passa a `LIBERA`, Ticket passa a `IN_LAVORAZIONE` (o `RIAPERTO`).

5. **Fase 6 (Feedback Contatto & Rilancio in Bacheca):**
   - **Appuntamento Preso:** Passa a `APPUNTAMENTO`.
   - **Appuntamento Fallito Ordinario:** Passa a `RIAPERTO` (Mansione torna `LIBERA`).
   - **Appuntamento Fallito con Rilancio in Bacheca:**
     - Il `Ticket` A del Tutor passa a `RIAPERTO` (Mansione disaccoppiata).
     - Viene generata una **Bozza `BoardPost` (`status: DRAFT`)** precompilata con i dati dell'azienda e il `projectId` ereditato dal Ticket A.
     - Notifica al CC. Quando il CC clicca "Pubblica", il post passa a `PUBLISHED`.
     - **Regola Sblocco 7gg:** Un'API/pulsante permette al CC di rimuovere il `projectId` rendendo il post pubblico a tutti **solo se `currentDate >= publishedAt + 7 giorni solari`**.

6. **Fase 7 (Scheda Amministrativa & Documenti Word):**
   - Compilazione griglia oraria settimanale e periodi di sospensione ("Dal-Al").
   - Stato $\rightarrow$ `IN_TIROCINIO`.
   - **Generazione Documentale:** Script che popola i file `.docx` della Convenzione e Progetto Formativo sostituendo i placeholder (es. `{NOME_TIROCINANTE}`, `{MONTE_ORE_TOTALE}`, `{GRIGLIA_ORARIA}`) e rende disponibile il download.

7. **Fase Finale (Report Restituzione & Audit Trail):**
   - Compilazione report (Gradimenti 1-5, Assunzione SI/NO, Mansione di nuovo disponibile SI/NO).
   - Stato $\rightarrow$ `FORM_RESTITUZIONE`. Se *Mansione Disponibile = SI*, la mansione torna `LIBERA`. Registrazione nel Log Audit Trail immodificabile dell'Azienda.

---

## 4. PERMESSI E REGOLAMENTAZIONE AMBITO VISIBILITÀ (RBAC)

1. **Tutor:**
   - Può vedere/modificare in scrittura **solo i propri ticket**.
   - Può vedere in lettura i ticket altrui **solo se appartengono a Progetti a cui è assegnato**.
   - In Bacheca vede **solo** i post pubblici (`projectId == NULL`) + i post legati ai propri progetti.
   - Non vede **mai** il nome dell'azienda nei post di Bacheca.
2. **Call Center / Admin:**
   - Visibilità globale su tutti i ticket, progetti, aziende e bozze di bacheca.
   - Può filtrare le bozze in bacheca ed ha l'esclusiva sulle modifiche dei post.

---

## 5. TASK INIZIALI PER CLAUDE CODE

Inizia la costruzione del progetto seguendo questo ordine sequenziale:

1. **Setup repository e schema DB:** Genera la struttura delle cartelle Next.js e crea il file `schema.prisma` con i modelli descritti.
2. **Seed Database:** Crea uno script `prisma/seed.ts` con Progetti di test (`GOL`, `POLIS`), Utenti (`Tutor 1`, `Tutor 2`, `Operatore CC`), Aziende di prova e Mansioni.
3. **FSM & Server Actions Workflow:** Implementa le Server Actions o API Routes per la gestione dei cambi stato del Ticket, rispettando tutte le logiche di blocco/sblocco mansioni.
4. **Interfaccia PWA Tutor:** Crea la vista Mobile-first per le Fasi 1, 5, 6 e 7.
5. **Dashboard Call Center Desktop:** Crea la vista Coda FIFO, gestione Fast-Track (Segnalazioni Speciali) e pannello di pubblicazione Bacheca.
6. **Modulo Generazione `.docx`:** Implementa la funzione helper per la compilazione dei template Word con `docxtemplater`.

Comincia subito generando lo schema Prisma e la struttura iniziale del progetto.
