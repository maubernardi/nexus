## Why

Storie **US-202**, **US-203** e **US-204** (Sprint 3), un gruppo coeso sui beneficiari: il Tutor deve ritrovare i
propri beneficiari, correggerne i dati e i dati devono essere visibili solo a chi ne ha diritto. Oggi il Tutor può solo
registrarli; l'elenco esiste solo come scelta nella nuova segnalazione.

## What Changes

- **I miei beneficiari** (`/beneficiari`, Tutor): elenco dei soli beneficiari di cui è proprietario, con ricerca per nome
  e cognome (in qualunque ordine, senza badare agli accenti), zona e segnalazione aperta. Nel menu la voce
  "Beneficiari" sostituisce "Nuovo beneficiario", che diventa un pulsante della pagina.
- **Scheda** (`/beneficiari/:id`): tutti i dati in testo, chi l'ha inserito, "Modifica" per il proprietario.
- **Modifica** (`/beneficiari/:id/modifica`, `PUT /api/v1/beneficiaries/{id}`): solo il Tutor proprietario (`403` per
  chi vede il beneficiario senza esserne proprietario, `404` per chi non lo vede). Audit `UPDATE` con i **soli nomi** dei
  campi cambiati, mai i valori (dati personali); nessun evento se non cambia nulla.
- **Visibilità** (`GET /api/v1/beneficiaries/{id}`): oltre a proprietario, Call Center e ADMIN, anche i Tutor dei
  progetti in cui il beneficiario ha segnalazioni, con i dati completi (Q11); gli altri ricevono `404`.
- Il modulo del beneficiario serve anche in modifica; il DTO `BeneficiaryCreateDTO` diventa `BeneficiaryFormDTO`.
- La voce del menu resta evidenziata anche nelle pagine interne della sezione (scheda, modifica, nuova azienda…).

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `beneficiary-registry`: consultazione con ricerca e visibilità per progetto; nuova modifica del beneficiario.
