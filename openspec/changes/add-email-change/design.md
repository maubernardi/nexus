## Context

L'indirizzo email di un utente esiste in due copie: `app_user.email` (NEXUS, usato per le notifiche) e l'utente
Keycloak (usato anche per accedere: `loginWithEmailAllowed`). Il backend oggi riallinea da Keycloak solo il ruolo e non
ha credenziali per la API di amministrazione di Keycloak.

## Goals / Non-Goals

**Goals:** cambio self-service dell'indirizzo, confermato con OTP al nuovo indirizzo; copie sempre allineate; avviso
al vecchio indirizzo; audit.

**Non-Goals:** modifica di nome, cognome o telefono; gestione utenti da parte dell'ADMIN (US-103/105); cambio password.

## Decisions

### Richiesta di cambio in tabella, una per utente
`email_change_request`: utente (unico), nuovo indirizzo, hash del codice, tentativi, scadenza, ultimo invio.
- Una nuova richiesta sostituisce la precedente (stessa riga); conferma e annullamento la cancellano.
- È uno stato di lavoro, non un dato da conservare: righe cancellabili, nessun trigger di immutabilità. La traccia
  immodificabile è l'evento di audit.
- Codice: 6 cifre da `SecureRandom`, salvato con BCrypt (già in Spring Security). In chiaro esiste solo nell'email.

### Errori del codice senza rollback
Un codice sbagliato incrementa i tentativi e risponde 400: il metodo di conferma usa
`noRollbackFor = BadRequestException.class`, altrimenti l'incremento verrebbe annullato insieme all'errore.

### Invio sincrono del codice
L'email col codice parte durante la richiesta: se l'invio fallisce la richiesta non si salva e l'utente vede l'errore
(503). L'avviso al vecchio indirizzo invece parte dopo il commit, in modo asincrono come le notifiche: un errore non
annulla un cambio già confermato.
- Email spente (locale, test): con `nexus.mail.log-otp` (solo nel profilo `security-mock`) il codice va nel log; senza,
  la richiesta risponde 503. In produzione il profilo mock non c'è mai.

### Keycloak prima del database
Alla conferma: controlli (codice, scadenza, unicità in NEXUS) → aggiornamento in Keycloak → aggiornamento di
`app_user` e audit, nella stessa transazione.
- Se Keycloak rifiuta (indirizzo già usato: 409) o non risponde, nulla cambia in NEXUS.
- Il caso inverso (Keycloak aggiornato, commit fallito) è ridotto al minimo dal controllo di unicità fatto prima.
- Interfaccia `IdentityProviderUsers`: implementazione Keycloak nel profilo `keycloak`; altrove (locale, test) una
  versione che non fa nulla.
- Client `nexus-backend-admin` con `client_credentials`; token chiesto a ogni cambio (operazione rara). Si legge
  l'utente, si cambia `email` (con `emailVerified: true`, verificato dall'OTP) e lo si riscrive intero: un PUT parziale
  con lo User Profile di Keycloak può azzerare gli attributi mancanti.
- Endpoint interno `http://keycloak:8080` (come le chiavi JWT): il traffico non esce dalla rete dei container.

### API
- `GET /me/email` → indirizzo attuale e, se c'è, la richiesta in corso (nuovo indirizzo, scadenza, quando si può
  richiedere un altro codice).
- `POST /me/email-change` `{newEmail}` → crea o sostituisce la richiesta e invia il codice (anche «invia di nuovo»).
- `POST /me/email-change/confirm` `{code}` → conferma.
- `DELETE /me/email-change` → annulla.

L'indirizzo mostrato è quello di `app_user` (non quello del token): in locale gli utenti mock hanno l'email fissa.

## Risks / Trade-offs

- [Server esistente senza client tecnico] → script una tantum; finché manca, il cambio risponde 503 con un messaggio
  chiaro e un avviso nel log all'avvio, il resto dell'app funziona.
- [Chi ha la sessione aperta può cambiare l'indirizzo] → serve comunque l'accesso alla nuova casella; l'avviso al
  vecchio indirizzo rende visibile un cambio non voluto.
