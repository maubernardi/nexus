## Why

Storia **US-114** (Sprint 12): i documenti reali della cooperativa (convenzioni, progetti formativi) chiedono città e
provincia di sedi e residenze. Serve l'elenco ufficiale dei comuni e delle province italiane, **temporizzato** (fusioni,
cambi di nome e di provincia non devono cambiare i dati già inseriti né i documenti prodotti), caricato dall'ADMIN dal
file dell'ISTAT (decisioni del PO del 09/11).

## What Changes

- **Migrazione V33**: tabelle `province`, `municipality` (una riga per versione, validità `valid_from`–`valid_to`, la
  versione attuale ha `valid_to` vuoto) e `municipality_import` (registro dei caricamenti). Vincoli con nome, testati.
- **Caricamento del file ISTAT** («Elenco dei comuni italiani», CSV): l'ADMIN lo carica dai Cataloghi con la data da
  cui vale; prima vede l'**anteprima** delle differenze (comuni e province nuovi, soppressi, cambiati), poi conferma.
  Chiave del comune: codice catastale (non cambia con la provincia); della provincia: codice dell'unità territoriale.
  Una versione cambiata o soppressa si chiude e la nuova si apre: nulla si cancella. File non riconosciuto, incompleto
  o con una data non successiva all'ultimo caricamento: rifiutato con il motivo. Ogni caricamento nell'audit.
- **Ricerca**: `GET /municipalities?q=` per gli utenti autenticati (dalla seconda lettera, solo comuni validi oggi,
  «Sesto Fiorentino (FI)»); l'ADMIN può vedere anche le versioni passate. La scelta del comune nei moduli arriva con
  US-1007.
- **Demo**: con il profilo `seed` l'elenco attuale (file ISTAT nel codice, licenza CC BY 4.0) si carica da solo se le
  tabelle sono vuote.
- **Cataloghi**: sezione «Comuni e province» con lo stato, il caricamento con anteprima e la ricerca.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `reference-data`: requisito aggiunto per comuni e province.

## Impact

- Migrazione **V33**; nessun passo sul server: in produzione l'ADMIN carica il file la prima volta.
- Il luogo di nascita resta testo libero (PO, 09/11).
