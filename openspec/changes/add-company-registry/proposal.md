## Why

Storia **US-501** (Sprint 2): il Call Center deve gestire le aziende ospitanti (ragione sociale, P.IVA, sede, referente,
contatti) per avere un database affidabile per l'abbinamento. Le aziende con uno storico non si cancellano.

## What Changes

- API `/api/v1/companies` (Call Center, ADMIN): ricerca per ragione sociale o P.IVA (con o senza disattivate),
  dettaglio, creazione, modifica con blocco ottimistico, disattivazione e riattivazione. Nessuna cancellazione.
- P.IVA di 11 cifre, univoca (errore sul campo); email e telefono validati.
- `V12`: colonna `version` sulle aziende (due operatori non si sovrascrivono: `409`).
- Audit (EN-2): `CREATE`, `UPDATE` (solo i campi cambiati, prima/dopo), `DEACTIVATE`, `ACTIVATE`.
- Interfaccia: pagina **Aziende** (ricerca nell'indirizzo, tabella accessibile), **Nuova azienda**, **Scheda azienda**
  (modifica, attiva/disattiva con esito focalizzato). Voce di menu per Call Center e ADMIN.

## Capabilities

### New Capabilities
- `company-registry`: anagrafica delle aziende ospitanti.

### Modified Capabilities
<!-- nessuna -->

## Non-goals

- Mansioni dell'azienda (US-502), audit trail consultabile dell'azienda (US-503), controllo del codice di controllo della
  P.IVA (non richiesto: solo 11 cifre).
