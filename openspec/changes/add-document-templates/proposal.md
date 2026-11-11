## Why

Storia **US-1010** (Sprint 13): oggi i modelli Word della convenzione e del progetto formativo sono nel codice, quindi
per aggiungerne o cambiarne uno serve un rilascio. I modelli reali di GOL cambiano per progetto, tipo di tirocinio ed
ente, e non possono stare nel repository (planning del 09/11). L'ADMIN deve poterli caricare da NEXUS.

## What Changes

- **Pagina «Modelli dei documenti»** per l'ADMIN.
  - Caricamento di un file Word (.docx), con queste informazioni:
    - documento: convenzione, progetto formativo o relazione finale;
    - progetto e tipo di tirocinio;
    - ente di presa in carico, facoltativo;
    - un nome che lo distingue.
  - Elenco dei modelli caricati. Ogni modello si può scaricare, sostituire con un file nuovo, disattivare e riattivare.
  - Il file è salvato nel database come il CV (US-205): entra nel backup e resta fuori dal repository.
- **Controllo al caricamento:**
  - il file deve essere un Word valido;
  - deve usare solo i segnaposto che NEXUS sa compilare;
  - altrimenti è rifiutato, con l'elenco dei segnaposto sconosciuti.

  La pagina mostra i segnaposto che si possono usare.
- **Scelta del modello quando il Tutor scarica un documento.** NEXUS considera i modelli attivi dello stesso documento,
  del progetto della segnalazione e del tipo di tirocinio della scheda, con l'ente del beneficiario oppure senza ente.
  Quelli con l'ente valgono più di quelli senza. Poi:
  - con **un** modello, NEXUS usa quello;
  - con **più di uno**, il Tutor sceglie da un elenco;
  - con **nessuno**, NEXUS usa il modello generico che resta nel codice (PO, 09/11).
- **Audit:** caricamento, sostituzione, disattivazione e riattivazione dei modelli. Il download di un documento registra
  anche il modello usato.
- Disattivare o sostituire un modello non cambia i documenti già scaricati: NEXUS non li conserva.
- **Migrazione V40** solo additiva; vincoli con nome, testati.

## Non-goals

- **Segnaposto nuovi** e relazione finale scaricabile: arrivano con US-1011. La relazione finale si può già caricare
  come modello, ma il Tutor la scarica da US-1011.
- **Modelli reali di GOL** (EN-8): si caricano nello Sprint 14. Nei test si usano modelli finti (PO, review dello
  Sprint 12).
- **Condizioni modificabili:** progetto, tipo ed ente non si cambiano dopo il caricamento. Si disattiva il modello e se
  ne carica uno nuovo.
- **Cancellazione dei modelli** e versioni precedenti dei file.

## Capabilities

### New Capabilities
- `document-templates`: modelli Word caricati dall'ADMIN con le condizioni in cui valgono, controllo dei segnaposto,
  sostituzione, disattivazione e audit.

### Modified Capabilities
- `internship-documents`: requisito aggiunto per la scelta del modello caricato quando il Tutor scarica un documento,
  con il modello generico di ripiego.

## Impact

- Migrazione **V40**; nessun passo sul server.
- API nuove:
  - per l'ADMIN, `/api/v1/document-templates`: elenco, caricamento, file, sostituzione, attivazione e segnaposto noti;
  - per il Tutor, l'elenco dei modelli da scegliere per un documento, e il download che accetta il modello scelto.
- Frontend:
  - pagina nuova «Modelli dei documenti» nel menu dell'ADMIN;
  - nella scheda del tirocinio, la scelta del modello quando ce n'è più di uno.

  Accessibilità: WCAG 2.2 AA (1.3.1, 3.3.1, 3.3.2, 4.1.3 messaggi di stato per caricamento e download); test axe. PWA:
  nessun impatto (le API non vanno in cache).
- Collaudo: nuova prova T-23.4.
