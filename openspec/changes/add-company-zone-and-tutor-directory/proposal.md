## Why

Storia **US-511** (Sprint 18), anticipata dal PO il 26/11: il Tutor deve vedere le aziende. Oggi le aziende sono solo
di Call Center e ADMIN: menu, pagina e API. Il Tutor non sa quali aziende conosce già il Call Center, e non può
segnalarne una a ragion veduta (analisi con il PO del 23/11). Serve anche la **zona** dell'azienda, che oggi manca:
il Tutor ragiona per zone.

## What Changes

- **Zona dell'azienda**: una zona del catalogo (US-106), facoltativa, nella scheda dell'azienda. La sceglie il Call
  Center, ed entra nell'audit. Nell'elenco del Call Center diventa una colonna e un filtro. Le aziende importate
  partono senza zona.
- **Aziende per il Tutor**: la voce «Aziende» nel menu del Tutor apre un elenco in sola lettura delle sole aziende
  attive:
  - colonne: ragione sociale, settore (categoria merceologica), zona, indirizzo con il comune (la sede operativa se
    c'è, altrimenti la legale), stelle e pollici (US-514);
  - ricerca per ragione sociale e filtri per settore e zona, nell'indirizzo;
  - pagine come nell'elenco del Call Center (US-513).
- **«Azzera i filtri»** (PO, 26/11, dal collaudo): nei due elenchi delle aziende, quando c'è almeno un filtro o una
  ricerca, un pulsante li toglie tutti e riparte dalla prima pagina; le aziende per pagina restano.
- **Minimizzazione**: un'API a parte per il Tutor, che restituisce solo quei campi. Niente partita IVA, recapiti,
  referenti, note, mansioni o storico; la ricerca non guarda la partita IVA. Il Tutor non crea né modifica aziende.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `company-registry`: zona dell'azienda; elenco delle aziende per il Tutor.
- `dev-seed-data`: zona delle aziende dimostrative.

## Non-goals

- La scheda dell'azienda per il Tutor: arriva con US-1104, insieme alle valutazioni dei tirocini.
- Il 🚢 della nave scuola (US-515) e le valutazioni (US-1104) nella lista del Tutor: li aggiungono quelle storie.
- Il collegamento tra zone e comuni (PO, 23/11): non in questa storia.
- La richiesta di contatto dal Tutor (US-507).

## Impact

- Database: **V53**, `company.zone_id` con `fk_company_zone` e un indice.
- Backend:
  - `Company`, `CompanyDTO`, `CompanyFormDTO`, `CompanyServiceImpl` con l'audit, ricerca con `zoneId`;
  - `GET /api/v1/companies/directory` per Tutor e ADMIN, con `CompanyDirectoryItemDTO` e una query solo sul nome;
  - il catalogo delle categorie merceologiche si legge anche dal Tutor, per il filtro: è un catalogo, senza dati
    personali (come le zone).
- Frontend:
  - zona nel modulo e nell'elenco del Call Center;
  - pagina nuova `TutorCompaniesPage` sullo stesso indirizzo `/aziende`, scelta in base al ruolo;
  - voce «Aziende» nel menu del Tutor;
  - testi in `it.ts`.
- Accessibilità (WCAG 2.2 AA): la pagina nuova riusa tabella, filtri, pagine e intestazione già verificati; stelle e
  pollici hanno il testo per il lettore di schermo.
- PWA: nessuna API in cache.
- Collaudo: prove T-37 sul PC del PO prima del merge (EN-11).
