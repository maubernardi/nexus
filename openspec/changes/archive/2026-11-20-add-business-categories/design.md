## Context

Vedi proposal.md, «Why». I cataloghi dell'ADMIN (zone, tipologie, motivi di chiusura…) estendono
`AbstractReferenceEntity` (codice, nome, attiva) e passano tutti da `CatalogServiceImpl`, con un `CatalogKind` e lo
stesso componente `CatalogSection` nel frontend. La mansione sceglie la tipologia tra le voci attive più quella che
ha già (`activeCategory(id, current)`), e il modulo fa lo stesso con `withCurrent`.

## Goals / Non-Goals

**Goals:** categoria e attività sull'azienda, come gli altri cataloghi e gli altri campi dell'azienda, pronte per
l'importazione di US-1309.

**Non-Goals:** importazione, ricerca per attività, segnaposto nei documenti (vedi proposal.md).

## Decisions

### D1. Un catalogo come gli altri

Tabella `business_category`, con le stesse colonne e gli stessi vincoli di `closure_reason` (V41):
- `uq_business_category_code`;
- `ck_business_category_code`;
- `ck_business_category_name`;
- l'indice `uq_business_category_active_name` sui nomi attivi.

`CatalogKind.BUSINESS_CATEGORIES` (`/catalogs/business-categories`, prefisso dei codici `CATEGORIA`) e
`AuditEntityType.BUSINESS_CATEGORY`. Nessuna regola speciale: tutte le voci si disattivano.

### D2. Voci iniziali con id fisso

V46 inserisce le 53 voci con:
- **id** = 893700000000000400 + numero in Access: da …401 a …455, senza …446 (il numero 46 in Access non c'è) e
  …451 (riga vuota, come la 56);
- **codice** ricavato dal nome con la regola di `ReferenceCodes` (al più 26 caratteri, senza trattini bassi finali).

L'importazione (US-1309) ritrova la categoria dall'id, che non cambia con la rinomina; il codice serve solo a leggere
i dati. Sono corretti due nomi: un refuso («edelizia») e le spaziature di «Telecomunicazioni,informatica,cinema e tv».

### D3. Azienda

- **Colonne** (V46, facoltative):
  - `company.business_category_id`, con `fk_company_business_category` e un indice per il filtro;
  - `company.activity` `varchar(200)`, con `ck_company_activity` (se c'è, non è vuota). In Access il campo è di 50
    caratteri.
- **Validazione** come la mansione: `businessCategoryId` deve essere una voce attiva oppure quella che l'azienda ha
  già, altrimenti `400` sul campo con «Categoria non valida». Vuoto = nessuna categoria.
- **Audit**: `businessCategory` con il nome prima e dopo, come il comune; `activity` con il testo prima e dopo, perché
  è un dato dell'azienda e non di una persona.

### D4. API e ricerca

- **`CompanyDTO`**:
  - `businessCategory` (`ReferenceItemDTO`, assente se non c'è);
  - `activity`.
- **`CompanyFormDTO`**:
  - `businessCategoryId`;
  - `activity` (`@Size(max = 200)`).
- **`GET /companies`**: il parametro `businessCategoryId` (id esterno) si aggiunge agli altri filtri. Un id non valido
  dà un elenco vuoto, non un errore, come una ricerca senza risultati.
- **`GET /reference/business-categories`**: solo Call Center e ADMIN, come l'anagrafica delle aziende.

### D5. Frontend

- **Cataloghi**: un `CatalogSection` in più, con i testi del catalogo.
- **Modulo dell'azienda**:
  - «Categoria merceologica» è un `NativeSelect` con «Nessuna» e le voci attive più quella dell'azienda
    (`withCurrent`);
  - «Attività» è un campo di testo, con un esempio nel suggerimento.
- **Elenco**:
  - colonna «Categoria» dopo la partita IVA;
  - menu «Categoria merceologica» nel riquadro di ricerca con «Tutte le categorie» e le voci attive, che cambia
    subito l'indirizzo (`categoria=<id>`), come le caselle.
- **Dettaglio dell'abbinamento**: righe «Categoria merceologica» e «Attività».

### D6. Dati dimostrativi

`R__seed_03` aggiorna le aziende dimostrative senza categoria e senza attività, come `demo-addresses.sql` per il
comune. Così anche la demo esistente riceve i dati, e le aziende modificate a mano non cambiano.

## Risks / Trade-offs

- **[Id fissi da tenere a mente]**: l'importazione dipende dagli id delle voci iniziali → sono scritti in V46 e in
  questo design, e una migrazione già su `main` non si modifica.
- **[Filtro con una categoria disattivata]**: il menu del filtro mostra solo le voci attive → per trovare le aziende
  di una categoria disattivata la si riattiva; sono casi rari.
