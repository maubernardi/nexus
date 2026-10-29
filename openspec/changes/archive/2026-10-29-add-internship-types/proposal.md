## Why

Storia **US-108** (Sprint 7, richiesta del PO del 29/10): il tipo di tirocinio non era previsto. Il PO lo vuole come
catalogo gestito dall'ADMIN, con le voci Curriculare, Extracurriculare, Di inclusione e Di inserimento
socio-terapeutico. Il tipo determina il tipo di documento (US-1004, US-1005); altri parametri legati al tipo
arriveranno più avanti.

## What Changes

- **Migrazione V21**: tabella `internship_type` con gli stessi vincoli di zone e tipologie (codice unico, nome non
  vuoto e unico tra le voci attive) e le quattro voci iniziali.
- Terzo catalogo della gestione dell'ADMIN (`/catalogs/internship-types`), con le stesse regole di US-106 e l'audit
  `INTERNSHIP_TYPE`.
- `GET /reference/internship-types`: i tipi attivi, per la scheda del tirocinio (US-1001).
- Frontend: sezione **Tipi di tirocinio** nella pagina "Cataloghi".

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `reference-data`: catalogo dei tipi di tirocinio.
