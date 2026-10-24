## Why

Voce **EN-4** (Sprint 3), decisione del PO nella review dello Sprint 2 (§7.1, 04/10): nome, telefono ed email del
referente di un'azienda sono dati personali di una persona fisica e l'audit non si può cancellare.

## What Changes

- Per le aziende l'audit registra referente, telefono ed email come `{"redacted": true}` quando sono impostati o
  cambiano, mai i valori; gli altri campi restano con prima/dopo.
- `AuditChanges.redactedIfChanged(campo, prima, dopo)`: come `redacted`, ma solo se il valore cambia.
- Gli eventi già registrati sulla demo restano (registro immodificabile): contengono solo dati fittizi.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `audit-log`: dati personali dei referenti aziendali.
