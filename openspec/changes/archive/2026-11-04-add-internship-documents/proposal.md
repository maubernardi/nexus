## Why

Storia **US-1004** (Sprint 8): il Tutor scarica la **Convenzione** già compilata con i dati della scheda del
tirocinio, invece di riscriverla a mano. I documenti sono competenza del Tutor (PO, 29/10). Fino ai modelli veri (Q5)
si usano **modelli segnaposto, uno per tipo di tirocinio** (review dello Sprint 7, §8.3). La modifica dei modelli sarà
una storia a parte.

## What Changes

- **Generazione da modello `.docx`**. I segnaposto `{NOME}` del modello si sostituiscono con i dati della scheda,
  dell'azienda e della segnalazione, compresi `{GRIGLIA_ORARIA}` e `{MONTE_ORE_TOTALE}`. Un dato mancante resta una
  riga vuota da completare a mano: si può scaricare anche con la scheda incompleta.
- **Modelli segnaposto** in `backend/src/main/resources/templates/documents/convenzione/`, cercati in quest'ordine:
  - quello per il codice del tipo di tirocinio (Curriculare, Extracurriculare, Di inclusione, Di inserimento
    socio-terapeutico);
  - altrimenti `default.docx`, per i tipi aggiunti dall'ADMIN.

  I file li produce `tools/genera-modelli-docx.py`.
- **API**: `GET /tickets/{id}/internship/documents/convenzione` restituisce il `.docx` (download). È del solo Tutor
  della segnalazione, dall'appuntamento fissato in poi, come la scheda.
- **Audit `DOCUMENT_GENERATED`** con il tipo di documento, senza dati; in cronologia "Convenzione scaricata".
- **Nuova dipendenza**: Apache POI `poi-ooxml` 6.0.0, l'ultima stabile, a versione esatta.
- **Frontend**: nella scheda una sezione "Documenti" con "Scarica la Convenzione". Se la scheda è incompleta, un avviso
  dice che i dati mancanti restano da completare a mano.

## Capabilities

### New Capabilities
- `internship-documents`: documenti del tirocinio generati dai modelli Word.

### Modified Capabilities
<!-- nessuna -->
