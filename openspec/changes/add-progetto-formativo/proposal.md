## Why

Storia **US-1005** (Sprint 8): oltre alla Convenzione, il Tutor scarica il **Progetto Formativo** compilato dalla
scheda del tirocinio. Valgono le stesse regole della Convenzione (US-1004), con il suo modello.

## What Changes

- **Nuovo tipo di documento** `PROGETTO_FORMATIVO` (`/tickets/{id}/internship/documents/progetto-formativo`), con lo
  stesso accesso, gli stessi dati, le righe vuote per quelli mancanti e l'audit della Convenzione. Il file scaricato si
  chiama `Progetto-formativo-segnalazione-N.docx`.
- **Modelli segnaposto**, uno per tipo di tirocinio più il predefinito, in
  `templates/documents/progetto-formativo/`, generati dallo stesso script. Contengono:
  - tirocinante, soggetti promotore e ospitante;
  - tabella del tirocinio, con ore settimanali e monte ore;
  - articolazione dell'orario;
  - righe libere per obiettivi e attività;
  - obblighi del tirocinante e firme.

  I modelli della Convenzione rigenerati restano identici.
- **Frontend**: nella sezione "Documenti" il secondo pulsante, "Scarica il Progetto Formativo (.docx)", con il suo
  esito.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `internship-documents`: Progetto Formativo in Word.
