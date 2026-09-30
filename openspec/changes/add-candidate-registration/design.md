## Context

Motivazione: proposal.md (US-201). Modello dati già esistente (`candidate`, `candidate_language`, `zone`, `job_category`).
Accesso: solo utenti censiti (filtro esistente); ruoli da `@PreAuthorize`.

## Decisions

### D1 — API e DTO
- `POST /api/v1/candidates` → `201` + `CandidateDTO`; `GET /api/v1/candidates` → `List<CandidateSummaryDTO>` (id, nome,
  cognome, anno di nascita, zona) del Tutor corrente; `GET /api/v1/candidates/{id}` → `CandidateDTO`.
- Id come stringa TSID (13 caratteri); un id non valido o non autorizzato → 404 (nessuna distinzione, per non rivelare
  l'esistenza dei record).
- `hasDrivingLicense` non si invia: è derivato dai tipi di patente (vincolo del database).
- MapStruct con un `TsidMapper` condiviso (Long ↔ String).

### D2 — Validazione
Bean Validation sui DTO (`@NotBlank`, `@Size`, enum) più vincoli personalizzati: `@BirthYear` (1900..anno corrente),
`@IsoCountry`, `@IsoLanguage` (tabelle ISO del JDK), unicità delle lingue. La zona deve esistere ed essere attiva
(controllo nel service → 400 sul campo `residenceZoneId`). Gli errori arrivano nel formato uniforme con `fieldErrors`.

### D3 — Utente corrente
Un `CurrentAppUserService` risolve l'`AppUser` dall'`external_id` del principal (il filtro dei censiti garantisce che
esista). Proprietario = utente corrente.

### D4 — Frontend: form accessibile riusabile
- Controlli **nativi** (`input`, `select`, `textarea`, checkbox) con componenti atomici: `FormField` (etichetta, testo di
  aiuto, errore con `aria-describedby`/`aria-invalid`), `CheckboxGroup` (`fieldset`/`legend`), `ErrorSummary` (regione
  `role="alert"`, link ai campi, riceve il focus all'invio fallito). Più semplici e affidabili dei componenti custom su
  mobile e con gli screen reader.
- react-hook-form; gli errori del server (`fieldErrors`) vengono riportati sui campi.
- Paesi e lingue: codici ISO in costanti, nomi in italiano con `Intl.DisplayNames`, Italia/italiano in cima.
- Valori codificati (genere, titolo di studio, mezzo, patenti, livelli): costanti tipizzate con etichette i18n.
- Sezione "Informazioni riservate" (L. 68/99, vincoli) con testo che invita a inserire solo il necessario (minimizzazione).
- Route `/candidati/nuovo` protetta per ruolo (`RoleRoute`), voce di menu "Nuovo candidato" per il Tutor. Dopo il
  salvataggio: conferma accessibile (annunciata) con il nome del candidato.

## Risks / Trade-offs

- [Liste ISO lunghe nelle select] → ordinamento alfabetico in italiano con le voci più frequenti in cima.
- [Minimizzazione verso altri tutor non ancora applicata ovunque] → l'elenco è già limitato ai propri candidati; il resto in US-204.
