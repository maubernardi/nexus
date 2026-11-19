## Context

Motivazione: vedi `proposal.md`.

US-1011 ha messo nel progetto intestazione e avviso (`project.header_line`, `project.notice_reference`, V43), modificati
dall'ADMIN in «Cataloghi» con `PUT /document-catalogs/projects/{id}/documents`, e i dati del promotore in
`ApplicationProperties.Promoter` (`NEXUS_PROMOTER_*`). `DocumentValues` riceve il promotore nelle `Sources`.
I segnaposto ammessi nei modelli sono `DocumentPlaceholders.KNOWN`; ogni modello caricato ha sempre un progetto
(US-1010).

## Goals / Non-Goals

**Goals:**
- Dati del promotore e variabili per progetto, modificati dall'ADMIN, nell'audit.
- I modelli di EN-8 validi senza cambiarli.

**Non-Goals:** vedi `proposal.md`.

## Decisions

### D1 — Promotore: colonne di `project`, non una tabella a parte

I 14 dati sono uno per progetto e si leggono sempre con il progetto: un `@Embeddable ProjectPromoter` su `Project`, con
colonne `promoter_*` facoltative. Un solo vincolo con nome, `ck_project_promoter_not_blank`: ogni testo, se c'è, non è
vuoto. Le due date sono `date`; nei documenti diventano `gg/mm/aaaa` come le altre.

Alternativa scartata: tabella `project_promoter` 1:1. Un'entità e un repository in più per una relazione che non
cambia mai cardinalità.

### D2 — Variabili: tabella `project_variable`

`id` TSID, `project_id`, `name` (fino a 50), `value` (fino a 500), `description` (fino a 300, facoltativa). Vincoli con
nome:
- `uq_project_variable_name` (`project_id`, `name`);
- `ck_project_variable_name`: `^[A-Z][A-Z0-9_]{0,49}$`;
- `ck_project_variable_value`, `ck_project_variable_description`: non vuoti.

Il divieto dei nomi di NEXUS sta nel servizio, perché l'elenco è nel codice.

### D3 — Un solo salvataggio per i dati dei documenti del progetto

`PUT /document-catalogs/projects/{id}/documents` riceve intestazione, avviso, promotore e l'elenco completo delle
variabili. Il servizio confronta l'elenco con quello salvato: aggiunge, aggiorna valore e descrizione, toglie. Il nome
non si modifica: una variabile con un nome nuovo è una variabile nuova.

Prima di togliere una variabile il servizio legge i segnaposto dei modelli attivi del progetto: se un modello la usa,
risponde `400` sul campo `variables` con il nome del modello. I modelli disattivati non contano, perché i modelli non
si cancellano: altrimenti una variabile provata una volta resterebbe per sempre. In cambio, la riattivazione di un
modello ricontrolla i segnaposto e, se uno non è più noto, risponde `409` con il suo nome. Così un modello attivo non
ha mai segnaposto sconosciuti.

Alternativa scartata: endpoint separati per ogni variabile. Più chiamate e più stati intermedi per un modulo che l'ADMIN
salva una volta.

### D4 — Segnaposto ammessi per progetto

Al caricamento e alla sostituzione del file sono ammessi `KNOWN` più i nomi delle variabili del progetto del modello.
`GET /document-templates/placeholders` restituisce, dopo quelli di NEXUS, un gruppo «Variabili del progetto «Nome»» per
ogni progetto con variabili; la descrizione è quella della variabile o, se manca, il suo valore.

### D5 — Valori nei documenti

`DocumentValues` prende il promotore dal progetto della segnalazione, al posto di `ApplicationProperties`; le variabili
del progetto si aggiungono dopo i segnaposto di NEXUS (i nomi non si sovrappongono, D2). Un dato del promotore mancante
resta la riga da completare, come prima.

### D6 — Audit

Una riga `UPDATE` sul progetto con i campi cambiati: intestazione, avviso, ogni dato del promotore, ogni variabile
(`variable:NOME`, prima e dopo il valore). Nome, luogo e data di nascita del legale rappresentante con
`redactedIfChanged`: dati personali.

## Risks / Trade-offs

- [Un modello caricato prima di una variabile con lo stesso nome] → non succede: un segnaposto sconosciuto rifiuta il
  modello, quindi la variabile deve esistere prima.
- [Modelli senza progetto] → non esistono (US-1010): il progetto è obbligatorio.
- [Ripristino della release precedente (A16)] → colonne e tabella vengono ignorate; la versione precedente legge di
  nuovo il `.env`, che è vuoto: righe da completare.
