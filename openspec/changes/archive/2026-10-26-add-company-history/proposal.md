## Why

Storia **US-503** (Sprint 4), dalla nota operativa 5 del PO: prima di proporre un beneficiario l'operatore deve valutare
l'azienda dai suoi pregressi. Lo storico è anche la base del popup "Dettagli" durante l'abbinamento (US-604).

## What Changes

- `GET /api/v1/companies/{id}/history` (Call Center e ADMIN): riepilogo e storico dell'azienda.
  - **Riepilogo** sempre completo, con le voci a zero finché i relativi eventi non esistono: proposte, accettate,
    appuntamenti falliti, tirocini, assunzioni, esiti negativi.
  - **Eventi dalla più recente**, in sola lettura: tipo, segnalazione collegata, dettaglio leggibile (mansione proposta,
    esito), autore e data; le **esclusioni** dell'azienda per una segnalazione compaiono con il motivo.
- Sezione **"Storico e pregressi"** nella scheda azienda, con link alle segnalazioni collegate.
- Vocabolario degli eventi dell'azienda (`CompanyEventType`): oggi nasce solo "proposta inviata", gli altri arriveranno
  con R2 e R3.
- Refactoring: i nomi degli autori (username → nome e cognome) in un unico servizio, usato da cronologia, note e storico.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `company-audit-trail`: consultazione dello storico con riepilogo dei pregressi.
