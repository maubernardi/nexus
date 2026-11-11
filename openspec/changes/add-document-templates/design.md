## Context

- I documenti di oggi (US-1004, US-1005) sono compilati da `InternshipDocumentServiceImpl` con `DocxTemplate`
  (segnaposto `{NOME}`). Il modello sta nel classpath, in `templates/documents/<documento>/<TIPO>.docx`, con
  `default.docx` come ripiego. L'elenco dei segnaposto noti è `PLACEHOLDERS`, e un test controlla i modelli forniti.
- I file caricati stanno già nel database: `stored_file` contiene i metadati e `stored_file_content` il contenuto, al
  massimo 5 MB (US-205). Il caricamento multipart arriva fino a 6 MB.
- Cataloghi utili (US-115): progetti con i tipi ammessi (vuoto = tutti), enti di presa in carico, ente del beneficiario
  (US-206).
- Se il deploy fallisce, la release precedente riparte sullo stesso database con `ddl-auto=validate` (A16).

## Goals / Non-Goals

**Goals:**
- L'ADMIN carica e gestisce i modelli senza rilasci; il Tutor scarica i documenti con il modello giusto.

**Non-Goals:**
- Segnaposto nuovi e relazione finale scaricabile (US-1011); vedi la proposta.

## Decisions

### D1 — Modello = riga di catalogo + file nel database
Tabella `document_template`. Le colonne:
- `document_kind`: `CONVENZIONE`, `PROGETTO_FORMATIVO` o `RELAZIONE_FINALE`;
- `project_id` e `internship_type_id`, obbligatori;
- `care_agency_id`, facoltativo;
- `name`: 200 caratteri, obbligatorio;
- `file_id`: unico, punta a `stored_file`;
- `active`, `version` e le colonne di audit.

Il contenuto va in `stored_file_content`, come il CV, quindi resta il limite di 5 MB e il file è nel backup. Vincoli
con nome:
- `ck_document_template_kind`, `ck_document_template_name`;
- le chiavi esterne;
- `uq_document_template_file`.

Più modelli attivi con le stesse condizioni sono ammessi: in quel caso il Tutor sceglie (D4).
*Alternativa scartata:* i file su disco. Andrebbero fuori dal backup del database e servirebbe un volume in più sul
server.

### D2 — Segnaposto noti in un solo posto
`DocumentPlaceholders.KNOWN` è l'elenco dei segnaposto che `InternshipDocumentServiceImpl` sa compilare. Lo usano il
controllo dei caricamenti, il test dei modelli nel codice e la pagina, che lo mostra (`GET
/document-templates/placeholders`). US-1011 lo estenderà.

### D3 — Controlli al caricamento e alla sostituzione
- **File:** non vuoto, al massimo 5 MB, che inizia come un archivio ZIP (`PK`) e che Apache POI apre come documento
  Word. Altrimenti l'errore sul campo `file` è «Il file deve essere un documento Word .docx»: un .doc del vecchio
  formato va salvato come .docx.
- **Segnaposto:** quelli trovati da `DocxTemplate.placeholders` meno i noti. Se ne restano, il caricamento è rifiutato
  con il messaggio «Il modello usa segnaposto che NEXUS non conosce: {CUP}, {DATA_STIPULA}».
- **Condizioni:**
  - progetto, tipo ed ente devono esistere;
  - il tipo deve essere ammesso dal progetto (stessa regola della scheda);
  - nome obbligatorio.

  Gli errori vanno sul campo (`document`, `projectId`, `internshipTypeId`, `careAgencyId`, `name`).

Il nome del file salvato è quello caricato, senza percorso e con l'estensione `.docx`, e serve solo per riscaricarlo.

### D4 — Scelta del modello
I candidati sono i modelli attivi con:
- lo stesso documento;
- il progetto della segnalazione;
- il tipo di tirocinio della scheda;
- l'ente del beneficiario oppure nessun ente.

Se almeno un candidato ha l'ente, restano solo quelli con l'ente. Poi:
- **nessun candidato:** si usa il ripiego di oggi nel classpath (modello del tipo o `default.docx`); vale anche se la
  scheda non ha il tipo;
- **un candidato:** si usa quello;
- **più candidati:** serve `?template=<id>`. Senza, il download risponde `400` sul campo `template` con «Scegli il
  modello». Un id che non è tra i candidati è anch'esso `400`.

L'elenco dei candidati per il Tutor è `GET /tickets/{id}/internship/documents/{documento}/templates`, con id e nome in
ordine di nome. Ha lo stesso accesso della scheda: solo il Tutor della segnalazione.

### D5 — Relazione finale
`InternshipDocumentKind` riceve `RELAZIONE_FINALE` (`relazione-finale`), così l'ADMIN può già caricarne i modelli. Lo
scaricamento resta rifiutato (`404`) fino a US-1011, che lo attiverà a tirocinio chiuso con i suoi segnaposto.

### D6 — Sostituzione e disattivazione
- **Sostituire il file** crea un file nuovo e cancella quello vecchio. NEXUS non conserva i documenti generati, quindi
  quelli già scaricati non cambiano.
- **Disattivare o riattivare** cambia solo `active`, con la versione per i conflitti.
- **Le condizioni non si modificano:** per correggerle si disattiva il modello e se ne carica uno nuovo. Così l'audit
  di ogni modello resta leggibile.

### D7 — Audit
Entità `DOCUMENT_TEMPLATE`:
- `TEMPLATE_UPLOADED` con documento, codice del progetto, del tipo e dell'ente, nome e dimensione;
- `TEMPLATE_FILE_REPLACED` con la dimensione prima e dopo;
- `TEMPLATE_DEACTIVATED` e `TEMPLATE_ACTIVATED`.

Il `DOCUMENT_GENERATED` della segnalazione registra anche `template`: il nome del modello oppure «modello generico».

### D8 — API dell'ADMIN
Sotto `/api/v1/document-templates`, tutte con `@PreAuthorize` sul ruolo ADMIN:
- `GET` per l'elenco;
- `POST` multipart (`file`, `document`, `projectId`, `internshipTypeId`, `careAgencyId`, `name`) per il caricamento;
- `GET /{id}/file` per scaricare il file;
- `PUT /{id}/file` multipart (`file`, `version`) per la sostituzione;
- `PUT /{id}/active` (`active`, `version`) per disattivare o riattivare;
- `GET /placeholders` per l'elenco dei segnaposto noti.

### D9 — Interfaccia
- **Pagina `/modelli`, «Modelli dei documenti»**, nel menu dell'ADMIN dopo «Cataloghi». Contiene:
  - un'introduzione, con i segnaposto noti in un elenco a scomparsa (`details`);
  - il modulo di caricamento: documento, progetto, tipo (solo quelli ammessi dal progetto scelto), ente («Tutti gli
    enti» se vuoto), nome e file, con il riepilogo degli errori come gli altri moduli;
  - l'elenco dei modelli, con documento e condizioni, stato e data. Per ogni modello ci sono le azioni «Scarica il
    file», «Sostituisci il file» e «Disattiva» o «Riattiva». I loro esiti sono annunciati da un messaggio di stato.
- **Scheda del tirocinio:** quando per un documento ci sono più modelli, accanto al pulsante compare la tendina
  «Modello della convenzione» (o del progetto formativo). Senza una scelta il download non parte e il messaggio lo
  dice.

## Risks / Trade-offs

- [Un modello con un segnaposto scritto male, per esempio in minuscolo, non viene riconosciuto come segnaposto] → resta
  testo nel documento, visibile al primo download. Il test di EN-8 controllerà i modelli reali.
- [File grandi nel database] → limite di 5 MB e un solo file per modello; i file vecchi si cancellano alla sostituzione.
- [Ripristino della release precedente (A16)] → tabella nuova, ignorata dalla release precedente, che usa i modelli nel
  codice.

## Migration Plan

Deploy normale: V40 crea la tabella vuota. Finché l'ADMIN non carica modelli, i documenti usano quelli nel codice come
oggi.
