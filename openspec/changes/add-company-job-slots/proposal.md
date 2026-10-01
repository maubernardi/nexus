## Why

Storia **US-502** (Sprint 2): il Call Center deve gestire le mansioni di ogni azienda (titolo, descrizione, tipologia,
zona) per sapere quali posizioni sono disponibili per l'abbinamento.

## What Changes

- API: `GET/POST /api/v1/companies/{id}/job-slots`, `PUT /api/v1/job-slots/{id}`, `POST …/deactivate|activate`
  (Call Center, ADMIN), con blocco ottimistico.
- Lo stato LIBERA/BLOCCATA è in sola lettura: lo governano abbinamenti e chiusure, mai le modifiche manuali.
- `V13`: una mansione che l'azienda non offre più si **ritira** (`active = false`), non si cancella; una mansione bloccata
  non si può ritirare (vincolo `ck_job_slot_active_free`). Indice per la ricerca delle compatibili (US-601).
- Nuove mansioni solo per aziende attive; tipologia e zona devono essere attive (resta valida quella già assegnata).
- Audit (EN-2): `CREATE`, `UPDATE` (solo campi cambiati), `DEACTIVATE`, `ACTIVATE` sulle mansioni.
- Scheda azienda: sezione **Mansioni** con tabella accessibile (stato e disponibilità in testo, segnalazione che blocca),
  aggiunta e modifica in un pannello con focus sul titolo, ritiro e ripristino con esito focalizzato.
- Unificato `TicketVersionDTO` in `VersionDTO`; timeout dei test frontend a 15 s (axe sui moduli grandi).

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `company-registry`: mansioni dell'azienda.

## Non-goals

- Ricerca delle mansioni compatibili e abbinamento (US-601, US-602); annunci in bacheca (US-904).
