## Why

Storia **US-508** (Sprint 15, PO 19/11). Il database attuale (Access) non ha la partita IVA delle aziende, e 1.587
aziende non hanno nemmeno il nome ([analisi](../../../docs/analisi/database-attuale.md)). Per importarle tutte
(US-1309) senza togliere a NEXUS l'obbligo della partita IVA, il PO ha scelto due valori segnaposto. Il programma non
li lascia salvare, così la prima modifica dell'azienda li corregge. Questa storia prepara NEXUS a riconoscerli prima
dell'importazione.

## What Changes

- **Valori segnaposto** per i dati che mancano nel database attuale:
  - partita IVA **«00000000000»**;
  - nome **«Azienda senza nome n. ‹numero di Access›»**.

  Li scrive solo l'importazione (US-1309).
- **Unicità della partita IVA** (V45): l'unicità nel database esclude «00000000000», così più aziende possono non
  averla.
- **Il modulo li rifiuta**: registrazione e modifica dell'azienda rispondono `400` sul campo se la partita IVA è
  «00000000000» o il nome comincia con «Azienda senza nome n.».
  - Nella scheda, i campi con il segnaposto si aprono vuoti e chiedono il dato vero.
  - Le altre sezioni della scheda (legale rappresentante, ATECO, mansioni, note) restano modificabili.
- **«Da completare»**:
  - l'API dell'azienda dice quali dati mancano;
  - l'elenco mostra «Da completare» nella colonna del dato che manca e ha il filtro «Solo le aziende da completare»;
  - la scheda e il dettaglio dell'abbinamento dicono quali dati mancano invece di mostrare gli zeri.
- **Documenti**: al posto dei valori segnaposto c'è la riga da completare, come per ogni dato mancante.
- **Dati dimostrativi**: due aziende da completare nel seed, per provare elenco, filtro e scheda.

## Non-goals

- **L'importazione** dal database attuale: è US-1309. Qui si prepara solo NEXUS ai suoi valori.
- **Un elenco dei dati da completare oltre a nome e partita IVA** (indirizzo, comune, referenti): sono facoltativi in
  NEXUS e restano vuoti senza segnaposto.
- **Il controllo della partita IVA con il carattere di controllo**: resta il controllo delle 11 cifre.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `company-registry`: partita IVA univoca tranne il segnaposto; dati da completare riconoscibili, filtrabili e non
  salvabili.
- `internship-documents`: i valori segnaposto dell'azienda diventano la riga da completare.
- `dev-seed-data`: due aziende dimostrative da completare.

## Impact

- Migrazione **V45**: il vincolo `uq_company_vat_code` diventa un indice univoco parziale con lo stesso nome, che
  esclude «00000000000». La release precedente funziona ancora, perché non scrive mai il segnaposto.
- API:
  - `CompanyDTO` ha `missingData` (`NAME`, `VAT_CODE`);
  - `GET /companies` ha il parametro `toComplete`;
  - `POST` e `PUT /companies` rifiutano i segnaposto con `400` sul campo.
- Frontend:
  - elenco delle aziende con il filtro e l'etichetta «Da completare»;
  - scheda con l'avviso dei dati che mancano e i campi vuoti da compilare;
  - dettaglio dell'abbinamento senza gli zeri.
- Accessibilità (WCAG 2.2 AA):
  - l'etichetta è testo, non solo colore, e si legge con l'intestazione della colonna;
  - il filtro è una casella con etichetta, come «Mostra anche le aziende disattivate»;
  - l'avviso nella scheda è testo nella pagina, sotto il titolo;
  - test con axe.
- PWA: nessun impatto, le API restano fuori dalla cache.
