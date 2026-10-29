## Why

Storia **US-1001** (Sprint 7): dopo l'appuntamento fissato il Tutor compila la **scheda amministrativa del
tirocinio**, i dati da cui nasceranno Convenzione e Progetto Formativo (US-1004, US-1005). Il PO ha precisato i campi
il 28–29/10. I documenti sono sempre competenza del Tutor (PO, 29/10): la scheda è sua, il Call Center non la vede.
Fino ai moduli ufficiali della cooperativa (Q5) i campi sono segnaposto, sostituibili senza cambiare il flusso
(review dello Sprint 6, §8.3).

## What Changes

- **Migrazione V23**:
  - `internship`: una scheda per segnalazione;
  - `internship_company_tutor`: i tutor aziendali, uno o più;
  - `internship_suspension`: i periodi di sospensione. Il database impedisce le sovrapposizioni con un vincolo di
    esclusione, senza estensioni.
- **API del Tutor della segnalazione** (`/tickets/{id}/internship`):
  - `GET` per leggere la scheda, con i dati dell'azienda che servono ai documenti;
  - `PUT` per salvarla, anche incompleta: la versione è nulla al primo salvataggio;
  - `PUT …/legal-representative` per inserire il legale rappresentante dell'azienda, solo se manca;
  - `POST …/ateco-codes` per aggiungere un codice ATECO all'azienda.
- **Quando**: la scheda si legge dall'appuntamento fissato in poi e si modifica solo con l'appuntamento fissato,
  prima dell'avvio (US-1003).
- **Se l'appuntamento poi fallisce**, della scheda restano solo i dati del beneficiario; gli altri si azzerano.
- **Codici ATECO**: un codice scelto in una scheda non si toglie dall'azienda.
- **Audit `TICKET`, azione `INTERNSHIP_SAVE`**:
  - i dati del tirocinio sono registrati con i valori;
  - i dati personali solo come "cambiato": beneficiario, tutor aziendali, note.

  La cronologia della segnalazione mostra "Scheda del tirocinio aggiornata", senza dati.
- **Frontend**:
  - pagina "Scheda del tirocinio" (`/segnalazioni/:id/tirocinio`):
    - in alto i dati dell'azienda: legale rappresentante e codici ATECO, con l'inserimento se mancano;
    - sotto il modulo, con righe ripetibili per i tutor aziendali e per le sospensioni;
  - collegamento dalla pagina della segnalazione.

## Capabilities

### New Capabilities
- `internship-record`: scheda amministrativa del tirocinio.

### Modified Capabilities
<!-- nessuna: le regole su ATECO e appuntamento fallito stanno nella nuova capability -->
