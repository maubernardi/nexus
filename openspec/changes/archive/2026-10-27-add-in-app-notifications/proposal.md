## Why

Storia **US-1201** (Sprint 5): le scadenze e le richieste della segnalazione (proposte, accettazioni, decisioni sulle
tipologie, e presto promemoria, rifiuti e scadenze del timer) devono raggiungere la controparte senza che debba cercarle.
Decisione della review dello Sprint 4 (§8.3): per ora solo **in app**, l'email resta in US-1202.

## What Changes

- Migrazione **V18**: tabella `notification` (destinatario, tipo, segnalazione, letta il; `ck_notification_type`); gli
  avvisi provvisori "tipologia cambiata" non ancora visti (US-302) diventano notifiche e la colonna
  `ticket.category_change_unseen` si elimina.
- Notifiche generate: al Tutor per **proposta** (US-602), **tipologia cambiata** e **tipologia approvata** (US-302);
  all'operatore in carico per l'**accettazione** (US-701). Chi agisce non riceve la propria.
- API: `GET /api/v1/notifications` (più recenti, con il numero delle non lette), `GET /notifications/unread-count`,
  `POST /notifications/{id}/read`, `POST /notifications/read-all`; solo le proprie.
- Interfaccia: **campanella** nell'intestazione con il numero di non lette (aggiornato ogni minuto, annunciato quando
  aumenta) e pagina **"Notifiche"**; l'avviso provvisorio di US-302 (Home e dettaglio) è sostituito dalla notifica.

## Capabilities

### New Capabilities
- `notifications`: notifiche in app.

### Modified Capabilities
- `ticket-submission`: la decisione sulla nuova tipologia si notifica al Tutor.
