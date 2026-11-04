## Decisions

### D1 — Evento dopo il commit
`NotificationService.notify` salva la notifica e pubblica `NotificationCreatedEvent`. `NotificationEmailSender` lo
riceve con `@TransactionalEventListener(AFTER_COMMIT)` e `@Async`, sui thread virtuali già attivi. Così:
- nessuna email per un'operazione annullata (rollback, versione superata);
- l'SMTP lento o irraggiungibile non tocca la risposta all'utente.

L'evento porta già tutto ciò che serve (indirizzo, tipo, segnalazione): niente lettura dal database nel thread
dell'invio.

### D2 — Testi in Java, uguali a quelli dell'app
I testi stanno in `NotificationEmail`, uno per tipo, con uno `switch` esaustivo sull'enum: un tipo nuovo senza testo
non compila. Sono gli stessi testi del frontend (`notifications.types`), in testo semplice. Contengono solo il numero
della segnalazione, come le notifiche in app (US-1201).

### D3 — Accese solo con la password
`nexus.mail.enabled` (variabile `MAIL_ENABLED`) è falso in locale e nei test; nella demo il compose lo mette a `true`.
Se è acceso ma manca la password (`NEXUS_SMTP_PASSWORD` → `MAIL_PASSWORD`), le email restano spente e il log lo dice
all'avvio. Il PO deve fare un solo passo: aggiungere la password al `.env` e rifare il deploy.

### D4 — Indirizzi dall'anagrafica di NEXUS
L'indirizzo è `app_user.email`. Gli utenti demo hanno indirizzi `@nexus.local`: l'invio fallisce e resta nel log, ma
l'app continua. Per provare, si dà un indirizzo vero a un utente demo (comando nel README). La gestione degli utenti
arriva con US-103.

### D5 — Niente controllo di salute dell'SMTP
Lo starter registra un controllo di salute che si collega all'SMTP. Senza password, o con IONOS lento, il backend
risulterebbe non pronto e il deploy tornerebbe indietro. `management.health.mail.enabled: false`.
