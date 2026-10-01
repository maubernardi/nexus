## Why

Storia **US-201** (Sprint 1): il Tutor deve poter registrare un beneficiario per poterlo poi segnalare (US-301). È il primo
modulo complesso di NEXUS: fissa anche gli schemi di form accessibili che le storie successive riuseranno.

## What Changes

- API `POST /api/v1/beneficiaries` (solo Tutor): registra un beneficiario di cui il Tutor diventa proprietario, con validazione
  lato server (valori ammessi, ISO 3166 / ISO 639-1, anno di nascita, coerenza patente).
- API `GET /api/v1/beneficiaries` (i propri beneficiari, riepilogo) e `GET /api/v1/beneficiaries/{id}` (proprietario, Call
  Center, ADMIN): servono alla conferma e alla selezione nella segnalazione. L'interfaccia completa di elenco e dettaglio
  resta nella storia US-202.
- API dei dati di riferimento: `GET /api/v1/reference/zones` e `/job-categories` (voci attive).
- Pagina **Nuovo beneficiario** per il Tutor: modulo in quattro sezioni, errori accanto ai campi e riepilogo in cima,
  utilizzabile da tastiera e a 320 px; voce di menu dedicata; protezione della route per ruolo.

## Capabilities

### New Capabilities
- `beneficiary-registry`: registrazione e consultazione dei beneficiari da parte dei ruoli autorizzati.
- `reference-data`: elenchi di riferimento (zone, tipologie di mansione) per i moduli.

### Modified Capabilities
<!-- nessuna -->

## Non-goals

- Elenco, ricerca e modifica dei beneficiari nell'interfaccia (US-202, US-203); minimizzazione verso altri tutor (US-204); CV (US-205).
- Registrazione da parte di Call Center o ADMIN.

## Impact

- Backend: DTO, mapper, service, resource per beneficiari e riferimenti; validazioni personalizzate.
- Frontend: pagina e componenti di form riusabili (campo con etichetta/errore, riepilogo errori, gruppo di checkbox), route
  protette per ruolo, voce di navigazione.
- Accessibilità: WCAG 2.2 AA (etichette, errori annunciati, riepilogo con link ai campi, target ≥ 24 px, reflow a 320 px).
