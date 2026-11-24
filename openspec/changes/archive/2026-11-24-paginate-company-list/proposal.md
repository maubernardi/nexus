## Why

Storia **US-513** (Sprint 17), chiesta dal PO il 24/11 durante il collaudo T-27.2, subito dopo il passaggio in
produzione. Con le 13.799 aziende importate dal database attuale l'elenco delle aziende è troppo lento: il server le
manda tutte insieme, con i referenti, e il browser disegna una riga per ciascuna.

## What Changes

- **Elenco a pagine**: il server manda una pagina alla volta, nello stesso ordine per ragione sociale, con il numero di
  aziende che rispondono ai filtri.
- **Aziende per pagina**: 10, 25, 50 o 100, da un menu a tendina (PO, 24/11); 25 se non si sceglie.
- **Pulsanti** Prima, Precedente, Successiva e Ultima, con «Pagina 3 di 552». Sopra la tabella: «13.799 aziende: dalla
  51 alla 75».
- Pagina e aziende per pagina stanno **nell'indirizzo**, come i filtri: Indietro e i link condivisi le mantengono. Un
  filtro nuovo riparte dalla prima pagina.
- **BREAKING** per l'API: `GET /api/v1/companies` risponde con una pagina (`companies`, `total`, `page`, `size`) invece
  dell'elenco. L'unico client è l'elenco delle aziende della PWA.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `company-registry`: ricerca delle aziende una pagina alla volta, con le aziende per pagina a scelta.

## Impact

- Backend:
  - `CompanyRepository.search` con `Pageable` e query di conteggio;
  - `CompanyService.search` e `CompanyResource` con `page` e `size`;
  - nuovo `CompanyPageDTO`;
  - `CompanyResourceIT`.
- Frontend:
  - `companyApi` e `useCompanySearch` con la pagina;
  - nuova molecola `Pagination`, riusabile;
  - `CompaniesPage`;
  - testi in `it.ts`.
- Collaudo T-27.2 e T-32. Nessuna migrazione.
