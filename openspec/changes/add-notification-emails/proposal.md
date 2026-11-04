## Why

Storia **US-1202** (Sprint 8). Le notifiche in app (US-1201) si vedono solo entrando in NEXUS: il PO le vuole anche
via email, così Tutor e operatori sono avvisati anche fuori dall'app (Q9: "in app e via email, alla controparte").

## What Changes

- **Ogni notifica in app arriva anche via email** alla stessa persona, all'indirizzo dell'anagrafica utenti.
  - Il testo è quello della notifica: solo il numero della segnalazione, nessun dato personale.
  - C'è il collegamento alla segnalazione.
  - Oggetto: «NEXUS · segnalazione n. N».
- **Invio dopo il commit, in un thread a parte**: un errore SMTP non annulla l'operazione né rallenta la risposta.
  Resta la notifica in app e un avviso nel log, senza l'indirizzo.
- **SMTP IONOS** (dati del PO, review dello Sprint 7):
  - `smtp.ionos.it:587` con STARTTLS obbligatorio;
  - utente `admin@portalenexus.it`, mittente `NEXUS <noreply@portalenexus.it>`.

  La password non è nel repository: `NEXUS_SMTP_PASSWORD` nel `.env` del server. Senza password le email restano
  spente, con un avviso nel log all'avvio.
- **Spente in locale e nei test**; nella demo le accende il compose.
- Il controllo di salute dell'SMTP è disattivato: il server di posta non decide se il backend è pronto.
- Nuova dipendenza `spring-boot-starter-mail`, versione gestita da Spring Boot 4.1.1.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `notifications`: le notifiche arrivano anche via email.
