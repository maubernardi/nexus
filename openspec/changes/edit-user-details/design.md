## Context

- `UserAdminService` crea gli utenti (US-103) e cambia accesso, ruolo e progetti (US-104, US-105). Non corregge i dati.
- Lo username si trova in tre posti:
  - `app_user.username`, unico;
  - l'account di Keycloak;
  - l'autore di ogni riga di audit (`created_by`, `AuditorAware` con lo username del token).

  `UserDisplayNameService` ricava il nome dell'autore dallo username: se lo username cambia, le righe già scritte con
  quello vecchio perdono il nome.
- `KeycloakIdentityProviderUsers` usa la API di amministrazione con il client `nexus-backend-admin` (`view-users`,
  `manage-users`). Per un aggiornamento rilegge l'utente intero e lo riscrive (`updateEmail`, `setEnabled`).
- Prove sul collaudo del 24/11, con dati finti:
  - un `PUT` dello username risponde 400 `error-user-attribute-read-only`, perché nel realm `editUsernameAllowed` è
    falso;
  - con `editUsernameAllowed` vero, il profilo utente di Keycloak (User Profile) dà `edit: ["admin", "user"]` a
    username, email, nome e cognome. Un utente creato dalla API (ruolo `default-roles-nexus`, quindi
    `manage-account`) li cambia dalla pagina dell'account: 204;
  - con `edit: ["admin"]` l'utente riceve 400 «sola lettura», mentre il client tecnico corregge username, nome ed
    email (204). L'accesso funziona con il nuovo username e non più con il vecchio;
  - l'invito (`execute-actions-email`) contiene l'email: dopo un cambio dell'email il vecchio collegamento finisce in
    errore (`invalid_email`). Dopo un cambio del solo username resta valido.
- Il realm della produzione si importa solo al primo avvio: le modifiche a un realm già esistente si portano con uno
  script una tantum del PO (`keycloak-password-policy.sh`, `keycloak-backend-client.sh`).

## Goals / Non-Goals

**Goals:**
- una sola operazione per correggere i dati, valida in NEXUS e in Keycloak, o tutto o niente;
- nessuna riga di audit con un autore che non si ritrova più;
- realm di produzione, sviluppo e collaudo con le stesse regole sui dati dell'account, verificate dalla CI.

**Non-Goals:**
- uno storico degli username;
- cambiare il realm della produzione dal backend all'avvio: resta lo script del PO, come per le altre modifiche al
  realm.

## Decisions

### D1. «Mai entrato» = nessuna password in Keycloak
Lo username si corregge solo se l'account non ha credenziali di tipo `password` (`GET /users/{id}/credentials`, con
`view-users`). Senza password non si entra in NEXUS (non c'è un altro modo di accedere), quindi nessuna riga porta il
suo username come autore.
- *Alternativa*: cercare lo username tra gli autori dell'audit e delle tabelle. Scartata: le tabelle sono tante e la
  regola diventerebbe fragile.
- *Alternativa*: usare il registro degli accessi di Keycloak. Scartata: gli eventi durano 12 mesi, la password resta.

Un utente che ha impostato la password ma non ha mai fatto nulla resta bloccato: è una scelta prudente, e la pagina
spiega il motivo.

### D2. API: `GET` e `PUT /api/v1/users/{id}/details`
- `GET` restituisce i dati e `usernameEditable`. Serve Keycloak per sapere se c'è la password; se non risponde, 503 e
  la pagina propone «Riprova».
- `PUT` riceve nome, cognome, username, email e telefono, con le stesse validazioni di `UserCreateDTO`. Restituisce i
  dati aggiornati e `invitationSent`:
  - assente se l'invito non serviva;
  - `true` o `false` se è partito o no.
- Una richiesta senza cambiamenti non chiama Keycloak e non scrive l'audit.

### D3. Ordine delle operazioni, come in `setActive`
1. Si normalizzano username ed email in minuscolo e si controllano gli unici in NEXUS (`FieldValidationException` sul
   campo).
2. Se lo username cambia: `hasPassword` → 400 sul campo `username` se c'è la password.
3. `app_user` e audit `DETAILS_CHANGED` con `saveAndFlush`; si annulla una richiesta di US-110 in attesa, se cambia
   l'email.
4. **Keycloak per ultimo** (`updateDetails`, che rilegge e riscrive l'utente intero). Gestione degli errori:
   - 409 → errore sul campo («già usato da un account di accesso»);
   - altri errori → 503.

   In tutti e due i casi la transazione si annulla e NEXUS non cambia.
5. Dopo l'aggiornamento:
   - email cambiata e nessuna password: si rimanda l'invito (`invite`, che scrive `INVITATION_SENT`); se non parte,
     la correzione resta e la pagina lo dice;
   - email cambiata e password già impostata: `EmailChangedEvent` → avviso al vecchio indirizzo dopo il commit,
     come in US-110.

### D4. Audit `DETAILS_CHANGED`
- Contiene solo i campi cambiati.
- Lo username ha prima e dopo in chiaro, come in `CREATE`, perché è il riferimento dell'autore.
- Nome, cognome, email e telefono sono `redacted`, come in `CREATE`.

### D5. Keycloak: «Edit username» e profilo utente solo per l'admin
- `editUsernameAllowed: true` nel realm.
- Nel profilo utente `permissions.edit` di `username`, `email`, `firstName` e `lastName` diventa `["admin"]`; `view`
  resta `["admin", "user"]`.
- Nei file del realm il profilo sta nel componente `org.keycloak.userprofile.UserProfileProvider`
  (`kc.user.profile.config`). Si parte dal profilo predefinito di Keycloak 26.8, letto dal collaudo, con il solo
  `edit` cambiato. Così le nuove installazioni, il realm di sviluppo e il collaudo nascono già corretti.
- *Alternativa*: togliere `manage-account` dai ruoli predefiniti. Chiude anche le altre funzioni della pagina
  dell'account, che NEXUS non usa, ma non basta: con `editUsernameAllowed` vero anche l'azione «aggiorna profilo»
  della pagina di accesso farebbe cambiare lo username. Il profilo per il solo admin chiude tutte le strade.

### D6. Script una tantum `infra/deploy/keycloak-user-profile.sh`
- Stessa struttura di `keycloak-password-policy.sh`: `sudo`, release in esecuzione, `kcadm` nel container con la
  password della console già presente.
- Imposta `editUsernameAllowed=true`. Legge il profilo, cambia solo `edit` dei quattro attributi con `python3` del
  server e lo riscrive.
- È idempotente e mostra il risultato. Funziona con qualunque versione di NEXUS: si può lanciare prima o dopo il
  deploy.
- Finché non si lancia, la correzione dello username risponde 503 e il resto funziona. Per questo il README dice di
  lanciarlo subito.

### D7. Frontend: pagina «Dati dell'utente»
- Route `/utenti/:id/dati` per l'ADMIN, con il pulsante «Dati» nell'elenco (`aria-label` con il nome).
- Il modulo segue «Nuovo utente»: `FormField`, `ErrorSummary` e gli stessi controlli dei campi.
- Lo username, se non è modificabile, è in sola lettura (`readOnly`, non `disabled`, per restare leggibile e
  raggiungibile). Ha un suggerimento collegato con `aria-describedby`: «È già entrato in NEXUS: lo username non si
  cambia più».
- Dopo il salvataggio si torna a «Utenti» con l'esito, come per i progetti. Se l'invito è ripartito, l'esito lo
  dice; se non è partito, avvisa e rimanda a «Invia di nuovo l'invito».

### D8. Prove
- **IT** del servizio con Keycloak simulato (`UserDetailsIT`): ordine, errori, invito, avviso e audit.
- Nello stesso `UserDetailsIT`: 403, 400 e richiesta senza cambiamenti.
- **IT** con Keycloak vero (`KeycloakIdentityProviderUsersIT`, realm di sviluppo): correzione e `hasPassword`.
- **Frontend**: test con axe.
- **CI**: realm di produzione con le regole di D5; nel collaudo, `collaudo-e2e.py` corregge username ed email di un
  utente nuovo e trova l'invito in Mailpit.
- **T-34** del PO sul collaudo del PC.

## Risks / Trade-offs

- [Lo script non viene lanciato] → la correzione dello username risponde 503 e il resto funziona. Il README e la PR
  lo mettono nei passi del deploy.
- [Profilo utente nel realm JSON legato alla versione di Keycloak] → è il profilo predefinito della 26.8 con il solo
  `edit` cambiato. Lo script di produzione cambia solo quei campi, senza riscrivere il resto.
- [Keycloak aggiornato e NEXUS che non salva al commit] → caso raro, come in `setActive` e `changeRole`: le due copie
  restano diverse e si correggono rifacendo la stessa correzione.
- [L'utente sbagliato era già entrato] → lo username resta quello, come deciso dal PO. Si può disattivare e crearne
  uno nuovo con un'altra email.

## Migration Plan

1. Merge e deploy, come al solito.
2. Il PO lancia sul server `sudo bash keycloak-user-profile.sh`, una volta, prima o dopo il deploy.
3. Controllo veloce in produzione: dalla pagina «Dati» il PO corregge lo username dell'utente sbagliato.

Rollback: il deploy precedente funziona anche con il realm nuovo, perché le regole del profilo valgono solo per la
pagina dell'account. Per tornare indietro anche su Keycloak basta rimettere `edit: ["admin", "user"]` dalla console
del tunnel.
