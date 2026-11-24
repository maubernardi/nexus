## Context

- La produzione è `infra/deploy/compose.yaml`: PostgreSQL 19, Keycloak (immagine `nexus-keycloak` con il tema), backend,
  `nexus-web` (Caddy con la PWA compilata), backup. Le immagini le costruisce il deploy per uno SHA.
- La PWA ha l'indirizzo di Keycloak scritto dentro al momento della build (`VITE_KEYCLOAK_URL`): le immagini della
  produzione puntano a `https://auth.portalenexus.it` e non servono in locale.
- In locale c'è già lo sviluppo (`infra/docker-compose.yml`, backend con `mvnw`, PWA con `pnpm dev`): utenti finti
  (`security-mock`) o Keycloak in `start-dev`, porte 5432, 8180, 8080, 5173. Non è lo stack della produzione.
- Sul PC del PO la porta 8081 è il tunnel SSH verso la console di Keycloak della produzione.
- Il realm di sviluppo (`infra/keycloak/nexus-realm.json`) ha gli utenti finti con gli stessi id del seed di NEXUS
  (`app_user.external_id`); quello di produzione no, ma ha policy delle password, blocco dei tentativi e durate delle
  sessioni.

## Goals / Non-Goals

**Goals:**
- collaudare una PR prima del merge, sul PC, con un comando;
- lo stack più vicino possibile alla produzione, con dati e utenti finti;
- nessuna email verso l'esterno, ma le email leggibili;
- niente che tocchi la produzione o il lavoro in corso nel repository.

**Non-Goals:**
- un ambiente sempre acceso o condiviso con altri;
- HTTPS in locale: servirebbe un certificato da far accettare al browser del PO;
- provare il server vero (Caddy con HTTPS, deploy, backup): restano le verifiche del deploy e il controllo in
  produzione dopo il deploy;
- collaudare con i dati veri.

## Decisions

### D1. Stack della produzione con un override
`collaudo.sh` usa il `compose.yaml` della produzione **del commit da provare** e gli aggiunge
`infra/collaudo/compose.yaml`:
- indirizzi `http://localhost:8090` (NEXUS) e `http://localhost:8091` (Keycloak), la console su 8092;
- porte solo su `127.0.0.1` e sostituite con `!override`: la 8081 della produzione non si apre mai;
- backend con `keycloak,seed` e posta verso Mailpit.

Il servizio di backup non parte: si avviano solo `web` e `mailpit`, con le loro dipendenze.

Alternativa scartata: un compose a sé per il collaudo. Si allontanerebbe dalla produzione a ogni cambio.

### D2. Immagini costruite in locale dal commit
I sorgenti del commit si estraggono con `git archive` in una cartella di lavoro
(`~/.cache/nexus-collaudo`), fuori dal checkout. Lì si costruiscono:
- il jar, senza test, perché li fa la CI della PR;
- la PWA, con `VITE_KEYCLOAK_URL=http://localhost:8091`;
- le immagini `nexus-collaudo/nexus-{backend,web,keycloak}:<sha>`.

Se le immagini di quello SHA esistono già, la build si salta. Il commit si indica come:
- numero della PR, letto da `pull/N/head`;
- branch, sempre preso aggiornato da GitHub;
- SHA.

Servono gli strumenti dello sviluppo (JDK, Node, pnpm), già sul PC del PO.

### D3. Caddyfile del collaudo
Il Caddyfile della produzione lega i siti ai domini con HTTPS: con `http://localhost` darebbe indirizzi non validi
(`www.`, CSP con `https://`). Il collaudo monta `infra/collaudo/Caddyfile` sopra quello dell'immagine, con le stesse
regole:
- solo `/api` verso il backend;
- endpoint tecnici in 404;
- cache della PWA;
- `/admin` e `/realms/master` in 403;
- CSP con l'indirizzo locale di Keycloak.

Niente HSTS né `www`. Chi cambia le regole di `frontend/Caddyfile` le cambia anche qui: lo dice il commento in cima.

### D4. Realm del collaudo dal realm di produzione
`realm-collaudo.py` parte dal realm di produzione del commit e cambia tre cose:
- gli indirizzi del client `nexus-frontend`;
- la posta, verso Mailpit, senza TLS né password;
- gli utenti, presi dal realm di sviluppo con i loro id, così il seed li riconosce. La password del collaudo rispetta la
  policy della produzione. In più c'è l'account tecnico del backend, per il cambio dell'indirizzo email.

Il resto resta quello della produzione: policy, blocco, sessioni, tema, eventi.

### D5. Ogni avvio riparte da zero
`avvia` cancella il collaudo precedente, cioè container e volumi, e riparte dal seed. Così:
- ogni PR si prova sugli stessi dati;
- le migrazioni di un'altra PR non restano nel database.

`ferma` e `riprendi` tengono i dati, per un collaudo su più giorni. `cancella` toglie tutto, immagini e sorgenti
comprese. Prima dell'avvio lo script controlla che le porte 8090, 8091, 8092 e 8025 siano libere.

### D6. Email in Mailpit
Mailpit (`axllent/mailpit`, versione esatta, seguita da Dependabot) riceve sulla 1025 la posta del backend e di
Keycloak. La casella si legge su `http://localhost:8025`.

Il backend ha STARTTLS obbligatorio verso IONOS. Nel collaudo `SPRING_APPLICATION_JSON` lo spegne, insieme
all'autenticazione: le proprietà della produzione non cambiano.

### D7. Prova nella CI
Job «Ambiente di collaudo»:
1. avvia il collaudo sullo SHA della PR;
2. `collaudo-e2e.py` fa da utente:
   - accesso di `tutor1` dalla pagina di Keycloak, con PKCE come la PWA, e `/api/v1/me`;
   - cambio dell'indirizzo, con l'email del backend in Mailpit;
   - «Password dimenticata?», con l'email di Keycloak in Mailpit;
3. lo script stesso controlla `403` su `/realms/master`;
4. `cancella`, poi la verifica che non resti nessun volume (A37).

Così un cambio al compose della produzione che rompe il collaudo si vede nella PR.

### D8. Processo
Il collaudo di una storia si fa sul PC, dal branch della PR, prima del merge:
- avvio io il collaudo e do al PO l'indirizzo e il link alle prove;
- il PO prova e spunta, e io aggiungo le spunte alla stessa PR;
- poi merge, deploy e un controllo veloce in produzione, senza dati finti.

La Definition of Done passa da «demo deployata e verificata» a «collaudo sul PC riuscito, poi deploy in produzione
verificato».

## Risks / Trade-offs

- [Il Caddyfile del collaudo può allontanarsi da quello della produzione] → commento in entrambi i file; le regole
  importanti, cioè `/api`, endpoint tecnici e `/realms/master`, sono controllate dallo script a ogni avvio; il deploy
  verifica la produzione.
- [HTTP invece di HTTPS: cookie `Secure` e HSTS non si provano] → `localhost` è un contesto sicuro per il browser;
  HTTPS resta coperto dalle verifiche del deploy.
- [La prima build richiede qualche minuto e circa 2,5 GB di memoria per lo stack] → le immagini di uno SHA si
  riusano; i limiti di memoria sono quelli della produzione.
- [Strumenti di sviluppo necessari sul PC] → sono già quelli del README; in alternativa avvio io il collaudo.
