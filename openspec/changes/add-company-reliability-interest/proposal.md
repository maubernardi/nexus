## Why

Storia **US-514** (Sprint 18), chiesta dal PO il 25/11. Nel database attuale il Call Center segna l'affidabilità di
un'azienda e il suo interesse al progetto scrivendo dei simboli all'inizio del nome: ogni `$` è un punto di
affidabilità, ogni `£` di interesse. Con l'importazione (US-1309) i simboli sono arrivati in NEXUS dentro i nomi: 343
aziende su 13.799, che si ordinano e si cercano male. Il dato è prezioso: va portato in due campi veri, senza perderlo,
e il nome va ripulito. Il PO lo vuole subito, anche nel collaudo (26/11).

## What Changes

- **Affidabilità**: da 1 a 4 stelle, facoltativa. Nella scheda l'operatore vede 4 stelle vuote, bianche, e clicca
  quella che vuole: si riempiono fino a lì. Si può togliere.
- **Interesse al progetto**: uno o due pollici alzati, facoltativo, nello stesso modo.
- Le cambiano operatori del Call Center e ADMIN con il modulo dell'azienda; ogni cambio entra nell'audit con prima e
  dopo.
- **Elenco delle aziende**: una colonna per le stelle e una per i pollici; filtri «almeno N stelle» e «interesse al
  progetto» (almeno un pollice, oppure due).
- **Nomi ripuliti senza perdere il dato** (PO, 25/11), con la migrazione **V52** sulle aziende già in NEXUS:
  - all'inizio del nome ogni `$` è una stella, e cinque `$` valgono 4 stelle;
  - ogni `£` è un pollice, al massimo due;
  - `&` e `^` iniziali sono refusi e si tolgono. La `&` dentro il nome resta, come in «Rossi & C.».
- **Il nome non può più iniziare** con `$`, `£`, `&` o `^`: il modulo lo dice e indica stelle e pollici. Il vincolo è
  anche nel database.
- **Importazione dal database attuale** (US-1309): la stessa regola, per quando si azzera la pre-produzione, con i
  conteggi nel resoconto.
- **Collaudo**: alcune aziende dimostrative hanno stelle e pollici.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `company-registry`: affidabilità e interesse al progetto dell'azienda, nella scheda e nell'elenco; nome senza i
  simboli del database attuale.
- `legacy-import`: i simboli all'inizio del nome diventano affidabilità e interesse.
- `dev-seed-data`: aziende dimostrative con stelle e pollici.

## Non-goals

- Il Tutor non vede ancora stelle e pollici: arrivano nella sua lista delle aziende con US-511.
- Nessun ordinamento dell'elenco per stelle o pollici: resta per ragione sociale.
- Nessun cambio rapido dall'elenco: stelle e pollici si cambiano dalla scheda dell'azienda.

## Impact

- Database: **V52**, con due colonne nuove su `company`, i loro `CHECK` e il vincolo sul nome, più la conversione dei
  nomi già presenti.
- Backend:
  - `Company`, `CompanyDTO`, `CompanyFormDTO` (validazione del nome);
  - `CompanyServiceImpl`, con l'audit;
  - `CompanyRepository.search` e `CompanyResource` con i due filtri;
  - `it.nexus.legacy`, con la regola dei simboli e il resoconto.
- Frontend:
  - un componente nuovo per scegliere stelle e pollici (gruppo di pulsanti di opzione), riusabile da US-1104;
  - `CompanyForm`;
  - `CompaniesPage`, con le colonne e i filtri;
  - testi in `it.ts`.
- Accessibilità (WCAG 2.2 AA):
  - la scelta si fa anche da tastiera, con le frecce, come un gruppo di opzioni;
  - ogni opzione ha il suo nome, per esempio «3 stelle su 4»;
  - le icone vuote hanno un contorno con contrasto di almeno 3:1;
  - lo stato non dipende solo dal colore: l'icona vuota e quella piena hanno anche una forma diversa.
- PWA: nessun impatto.
- Collaudo: prove T-35 sul PC del PO prima del merge (EN-11).
