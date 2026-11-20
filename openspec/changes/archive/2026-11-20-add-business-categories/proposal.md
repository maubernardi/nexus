## Why

Storia **US-509** (Sprint 15, PO 19/11). Il database attuale classifica il 97% delle aziende con una **categoria
merceologica** (Albergo, Ristorante / Trattoria / Bistrot, Pulizie e facchinaggio…). Il 92% delle aziende ha anche
una breve **descrizione dell'attività** («HOTEL», «PIZZERIA»…), che in Access sta nel campo «Paese»
([analisi](../../../docs/analisi/database-attuale.md), G4). NEXUS ha solo i codici ATECO, che si scelgono nella scheda
del tirocinio. Il PO vuole che NEXUS sostituisca il database attuale in tutto: categoria e attività entrano
nell'azienda prima dell'importazione (US-1309).

## What Changes

- **Catalogo «Categorie merceologiche»** (V46), gestito dall'ADMIN come gli altri cataloghi: aggiunta, rinomina,
  disattivazione e riattivazione, con l'audit.
  - Parte con le **53 voci** del database attuale: le 55 righe di Access meno le due vuote, mai usate.
  - Due nomi sono corretti: «edelizia» diventa «edilizia» e «Telecomunicazioni,informatica,cinema e tv» diventa
    «Telecomunicazioni, informatica, cinema e TV».
  - L'id di ogni voce iniziale è fisso: 893700000000000400 più il numero della categoria in Access. Così l'importazione
    ritrova la categoria anche se l'ADMIN l'ha rinominata.
- **Azienda**:
  - categoria merceologica facoltativa, scelta tra le voci attive; una voce disattivata resta sulle aziende che
    l'hanno;
  - descrizione dell'attività facoltativa, testo libero di al massimo 200 caratteri (es. «Pizzeria»);
  - le modifiche entrano nell'audit dell'azienda con i valori prima e dopo.
- **Elenco delle aziende**: colonna «Categoria» e filtro per categoria, che resta nell'indirizzo della pagina e si
  combina con la ricerca e gli altri filtri.
- **Dettaglio dell'abbinamento**: categoria e attività tra i dati dell'azienda.
- **API**:
  - `CompanyDTO` con `businessCategory` e `activity`;
  - modulo con `businessCategoryId` e `activity`;
  - `GET /companies?businessCategoryId=…`;
  - `GET /reference/business-categories` (Call Center e ADMIN);
  - `/catalogs/business-categories` per l'ADMIN.
- **Dati dimostrativi**: le aziende dimostrative ricevono categoria e attività.

## Non-goals

- **L'importazione** delle categorie delle aziende: è US-1309, che usa gli id fissi delle voci iniziali.
- **La tabella «Merciologico»** di Access (113 righe) e la tabella «Categoria» (3 righe): l'analisi le ha trovate ma
  le aziende usano «Nuove categorie».
- **Ricerca per attività** nel campo di ricerca: resta per ragione sociale e partita IVA; la categoria ha il suo
  filtro.
- **Segnaposto dei documenti** per categoria e attività: i modelli reali non li usano.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `reference-data`: catalogo delle categorie merceologiche con le voci iniziali del database attuale.
- `company-registry`: categoria merceologica e attività dell'azienda, colonna e filtro nell'elenco.
- `dev-seed-data`: categoria e attività delle aziende dimostrative.

## Impact

- Migrazione **V46**, solo additiva (A16):
  - tabella `business_category` con le 53 voci;
  - colonne facoltative `business_category_id` e `activity` su `company`;
  - vincoli con nome testati in `DomainConstraintsIT`.
- Frontend:
  - sezione del catalogo nella pagina «Cataloghi»;
  - campi nuovi nel modulo dell'azienda;
  - colonna e filtro nell'elenco;
  - righe nuove nel dettaglio dell'abbinamento.
- Accessibilità (WCAG 2.2 AA):
  - la categoria è un menu a tendina nativo con etichetta, sia nel modulo sia nel filtro;
  - il filtro si applica alla scelta, come le caselle, e resta nell'indirizzo;
  - test con axe.
- PWA: nessun impatto, le API restano fuori dalla cache.
