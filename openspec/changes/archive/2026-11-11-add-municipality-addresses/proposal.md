## Why

Storia **US-1007** (Sprint 13): i documenti reali (US-1011) chiedono città e provincia di sede legale, sede del
tirocinio e residenza del tirocinante, e la provincia del soggetto promotore nella convenzione è quella della sede del
tirocinio (planning del 09/11). Oggi sono testi liberi: il comune deve venire dall'elenco ufficiale di US-114.

## What Changes

- **Migrazione V37**: comune (riferimento alla versione del comune di US-114) per la sede legale dell'azienda e per la
  sede del tirocinio; la residenza del beneficiario nella scheda diventa **comune, via e numero civico** (PO, 09/11).
  Vincoli con nome, testati.
- **Scelta del comune** nei moduli: casella con suggerimenti (dalla seconda lettera, solo comuni validi oggi,
  «Sesto Fiorentino (FI)»), accessibile da tastiera e con lo screen reader. Si salva la **versione** del comune: un
  cambio di nome o di provincia successivo non cambia i dati già inseriti; riaprendo il modulo il comune salvato resta
  anche se nel frattempo è cambiato, ma uno nuovo si sceglie solo tra quelli validi.
- **Azienda** (Call Center e ADMIN): sede legale = indirizzo + comune, facoltativi come oggi.
- **Scheda del tirocinio** (Tutor): sede = indirizzo + comune; residenza = comune, via, numero civico. Per avviare il
  tirocinio servono tutti (la sede o la residenza incomplete compaiono tra i dati mancanti).
- **Dati già inseriti**: il testo libero resta nell'indirizzo (nella residenza: nella via) e il comune è vuoto; si
  ripassano a mano. Nella demo le aziende dimostrative ricevono il comune.
- **Documenti di oggi**: sede legale, sede del tirocinio e residenza compilate come «Via Roma 1, Firenze (FI)»; i
  segnaposto separati per comune e provincia arrivano con US-1011.
- **Audit**: comune e indirizzo dell'azienda e della sede con i valori; la residenza solo come «cambiata».
- **Anonimizzazione** (US-1303): via anche comune e numero civico della residenza.

## Non-goals

- Luogo di nascita (resta testo libero, PO 09/11), residenza del legale rappresentante e sede degli enti di presa in
  carico: restano testi liberi.
- Nuovi segnaposto dei documenti (US-1011) e scelta della provincia del promotore nella convenzione.
- CAP: l'elenco dell'ISTAT non li contiene.
- Riconoscimento automatico del comune dai testi già inseriti.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `reference-data`: requisito aggiunto per la scelta del comune nei moduli.
- `company-registry`: requisito aggiunto per la sede legale con il comune.
- `internship-record`: requisito aggiunto per sede e residenza con il comune.
- `internship-documents`: requisito aggiunto per gli indirizzi composti nei documenti.
- `dev-seed-data`: requisito aggiunto per il comune delle aziende dimostrative.

## Impact

- Migrazione **V37**; nessun passo sul server.
- API: `CompanyDTO`/`CompanyFormDTO` e `InternshipDTO`/`InternshipFormDTO` con i nuovi campi (`residence` diventa
  `residenceStreet`).
- Frontend: nuova molecola `MunicipalityField` (combobox WAI-ARIA 1.2 con lista), moduli dell'azienda e della scheda,
  dettagli dell'abbinamento. Accessibilità: WCAG 2.2 AA (4.1.2 nome, ruolo e valore; 2.1.1 tastiera; 4.1.3 messaggi
  di stato; 2.5.8 dimensione dei bersagli); test axe. PWA: nessun impatto (le API non vanno in cache).
- Collaudo: nuova prova T-23.1.
