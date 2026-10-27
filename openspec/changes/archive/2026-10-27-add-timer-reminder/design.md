## Decisions

### D1 — Attività pianificata nel backend
Un `@Scheduled` con cron configurabile basta per una sola istanza dell'applicazione (la demo e la produzione
previste). Con più istanze servirà un lock distribuito (es. ShedLock): oggi non serve.

### D2 — Idempotenza
Il promemoria è segnato sulla segnalazione (`timer_reminder_sent_at`) nella stessa transazione della notifica: se il
controllo gira di nuovo non lo ripete; se la transazione fallisce (es. il Tutor agisce nello stesso momento), il
controllo dell'ora successiva riprova.

### D3 — Istante passato come parametro
Il servizio riceve l'istante corrente: i test simulano giorni e ore precisi senza toccare l'orologio.

### D4 — Ordine con la scadenza (US-705)
Una segnalazione già scaduta riceverebbe comunque il promemoria (es. dati demo con timer di settembre): con US-705 il
controllo prima riaprirà le scadute, poi invierà i promemoria alle altre.
