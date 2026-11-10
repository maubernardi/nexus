## Why

Storia **US-115** (Sprint 12): i documenti reali di GOL (US-1011) e le storie US-206 e US-308 hanno bisogno di tre
elenchi gestiti dall'ADMIN: le **sedi di competenza** (il luogo della relazione finale), i **tipi di tirocinio ammessi
per ogni progetto** (GOL: solo Extracurriculare e Inclusione) e gli **enti che hanno in carico** i beneficiari dei
tirocini di inclusione, con i dati che finiscono nella convenzione e nel progetto personalizzato (decisioni del PO del
09/11).

## What Changes

- **Migrazione V34**:
  - `competence_site` (catalogo) con `competence_site_project` (molti a molti con i progetti);
  - `project_internship_type` (tipi ammessi per progetto; nessuna riga = tutti i tipi);
  - `care_agency` (catalogo) con tipologia, codice fiscale o partita IVA, sede legale, legale rappresentante (nome,
    qualifica, luogo e data di nascita) e referente per la convenzione (nome, telefono, mail).
  - Voci iniziali in ogni ambiente: le tre sedi di GOL (Firenze, Borgo San Lorenzo, Sesto Fiorentino), i tipi ammessi da
    GOL, i quattro enti con i nomi ufficiali. I dati delle persone degli enti li inserisce l'ADMIN (nella demo, inventati).
- **Cataloghi**: sedi di competenza ed enti si aggiungono, rinominano, disattivano e riattivano come gli altri
  cataloghi; in più l'ADMIN sceglie i progetti di una sede, i tipi ammessi di un progetto e i dati di un ente. Audit.
- **Scheda del tirocinio**: propone solo i tipi ammessi dal progetto della segnalazione (più quello già scelto); il
  server rifiuta un tipo nuovo non ammesso.
- Disattivare una sede o un ente, o togliere un tipo a un progetto, vale solo per le scelte nuove.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `reference-data`: requisiti aggiunti per sedi di competenza, tipi per progetto, enti di presa in carico.
- `internship-record`: requisito aggiunto per i tipi ammessi dal progetto.

## Impact

- Migrazione **V34**; nessun passo sul server. Dopo il deploy l'ADMIN completa i dati degli enti dai Cataloghi.
