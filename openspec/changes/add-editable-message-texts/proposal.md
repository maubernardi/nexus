## Why

Storia **US-1204** (Sprint 16, PO 22/11). I testi delle notifiche e delle email di NEXUS sono nel codice: per
cambiarne uno servono una PR e un deploy. Il PO vuole che l'ADMIN li cambi da solo. Vuole anche che l'oggetto delle
email delle notifiche dica di che cosa si tratta, non solo il numero della segnalazione.

## What Changes

- **Pagina «Testi dei messaggi»**, solo per l'ADMIN, con la sua voce nel menu di amministrazione.
  - Per ogni tipo di notifica cambia, separatamente, tre testi: quello nell'app, la parte distintiva dell'oggetto e
    quello dell'email (PO, 22/11).
  - Per le due email del cambio di indirizzo cambia oggetto e testo.
  - Per ogni messaggio la pagina dice a chi va e quando parte, elenca i segnaposto ammessi (`{numero}`; `{codice}` e
    `{ora}` nel codice per il cambio di indirizzo) e mostra l'anteprima composta.
- **Oggetto delle notifiche**: «PORTALE NEXUS - Segnalazione n. N - » più una parte distintiva per tipo, con un
  predefinito per ognuno (per esempio «Nuova proposta di tirocinio»), modificabile (PO, 22/11).
- **Parti fisse**: inizio dell'oggetto, collegamento alla segnalazione, avvertenza e firma.
- **Ripristino del testo predefinito** per ogni parte; audit di modifiche e ripristini.
- **Le notifiche nell'app** prendono il testo dal backend: il frontend non ne ha più una copia.

## Non-goals

- **Le email di Keycloak** (invito, password dimenticata, password cambiata): restano nel tema. La console di
  Keycloak le ridefinisce già (*Realm settings → Localization → Realm overrides*), provato il 23/11: il README dice
  come.
- **Altre lingue**: NEXUS è solo in italiano.
- **Testi dei documenti Word**: sono i modelli caricati dall'ADMIN (EN-8).

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `notifications`: nuovo requisito per i testi modificabili; cambiano «Notifiche anche via email» (il testo
  dell'email può essere diverso da quello dell'app) e «Firma e testi delle email» (parte distintiva dell'oggetto).

## Impact

- **Migrazione V51**, solo additiva: tabella `message_text`, con i soli testi cambiati e i vincoli con nome testati in
  `DomainConstraintsIT`.
- **Backend**:
  - `MessageKey`, `MessagePart`, `MessageDefaults` (i testi di oggi più le parti distintive), `MessageTexts`;
  - `MessageTextService` e `/api/v1/message-texts` (GET, PUT, DELETE), solo ADMIN;
  - `NotificationEmail`, `EmailChangeMailer` e l'elenco delle notifiche usano i testi in uso;
  - audit `MESSAGE_TEXT`.
- **Frontend**:
  - `MessageTextsPage` e `MessageTextPartForm`, rotta `/testi`, voce di menu;
  - le notifiche usano il testo dell'API.
- **Accessibilità**:
  - messaggi in `<details>`, campi raggruppati con il titolo del messaggio;
  - pulsanti con il nome completo («Salva: …, «Proposta di tirocinio»»), esito in `role="status"`;
  - test con axe.
- **Passaggio in produzione**: `message_text` è configurazione e resta; la pulizia della demo non la tocca.
- **Dati personali**: nessuno. I testi sono modelli, e l'audit registra solo i testi.
