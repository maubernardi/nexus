## Why

Voce **EN-5** (Sprint 3), decisione del PO nella review dello Sprint 2 (24/10): nello Sprint 2 due PR aperte in
parallelo contenevano V11 e V12; se il server avesse ricevuto la V12 prima della V11, Flyway avrebbe rifiutato il
deploy successivo. Il problema va visto nella PR, non durante il deploy.

## What Changes

- Script `.github/scripts/check-migrations.sh`, eseguito nel job **Backend** della CI (già obbligatorio per il merge)
  sulle PR: fallisce se la PR
  - aggiunge una migrazione con numero non superiore all'ultima del branch di destinazione (messaggio con il numero da
    usare), anche quando il branch della PR è rimasto indietro;
  - contiene due migrazioni con lo stesso numero;
  - modifica, rinomina o cancella una migrazione già presente sul branch di destinazione.
- Modello di descrizione delle PR con la sezione "Migrazioni Flyway".

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `deployment`: controllo delle migrazioni nelle PR.
