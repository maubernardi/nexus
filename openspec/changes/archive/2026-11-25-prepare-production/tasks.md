## 1. Database e backend

- [x] 1.1 `infra/deploy/pulizia-demo.sql` (D1); `DemoCleanupIT` sullo schema vero con i dati dimostrativi
- [x] 1.2 `AdminBootstrapJob`, proprietà `nexus.bootstrap.admin.*`, `existsByRole` (D4); `AdminBootstrapJobIT`

## 2. Infra e CI

- [x] 2.1 Realm di produzione senza utenti di prova; `nexus-deploy` lo scarica; password demo facoltative e dati
  dell'ADMIN nel compose (D5)
- [x] 2.2 Controllo del realm di produzione nel job del tema di Keycloak (D5)
- [x] 2.3 `infra/deploy/passaggio-in-produzione.sh` (D2, D3, D6); shellcheck pulito; comandi di Keycloak provati su
  26.8.2

## 3. Prove e documentazione

- [x] 3.1 Prova generale in locale: demo dai dati dimostrativi, pulizia, importazione del file vero, backend senza
  `seed` con l'ADMIN (D8)
- [x] 3.2 README «Passaggio in produzione», analisi §9.2, collaudo, Sprint 16
- [x] 3.3 `./mvnw verify` verde; `openspec validate prepare-production --strict` senza errori; CI verde

## 4. Passaggio (con il PO, dopo il merge)

- [x] 4.1 Deploy sulla demo; prova generale sul backup vero della demo
- [x] 4.2 Dati dell'ADMIN nel `.env`, file Access sul server, `nexus-deploy` reinstallato (23/11: dati dell'ADMIN e
      `nexus-deploy`, controllo delle chiamate in Access; 24/11: il file Access del 4/11, scelto dal PO, passato dalla
      home perché `/opt/nexus` è riservata a `deploy`)
- [x] 4.3 `passaggio-in-produzione.sh`; invito, accesso dell'ADMIN, controllo della configurazione
  - [x] script eseguito il 24/11 sulla release `433f234`: backup della demo, pulizia, 5 utenti tolti da Keycloak,
        importazione con i conteggi della prova, primo backup della produzione, file Access cancellato
  - [x] invito e accesso dell'ADMIN, configurazione (T-27.1, T-27.2: riuscite dal PO il 24/11)
- [x] 4.4 Seconda PR: profilo predefinito, realm e password demo, nomi «produzione», ASVS, chiusura della storia
  - [x] in bozza dal 23/11: profilo predefinito `keycloak`, via il realm e le password demo, primo ADMIN in
        `bootstrap.sh`, nomi visibili (workflow «Deploy» e «Controllo del sito», tag `:produzione`; file del workflow e
        ambiente «demo» di GitHub invariati, PO 23/11)
  - [x] controllo ASVS sulla produzione, dopo il passaggio (24/11, da fuori): tutto come nella revisione, tranne
        l'accesso degli amministratori di Keycloak (realm `master`) raggiungibile da Internet. PO (24/11, A40):
        bloccarlo. Caddy risponde `403` su `/realms/master`, il deploy lo verifica; il realm `master` prende
        l'indirizzo del tunnel con `keycloak-admin-tunnel.sh`, provato in locale (console dal tunnel e `kcadm`
        funzionano, realm `nexus` invariato)
  - [x] sul server: `keycloak-admin-tunnel.sh` prima del deploy di questa PR; dopo il deploy, console dal tunnel (T-27.4:
        script e deploy `388e8c2` il 24/11, riuscita dal PO)
  - [x] chiusura della storia: T-27.3 riuscita il 25/11 (backup della notte, 4,3 MB); il PO ha tolto il volume della 18
        (`pulisci`) e le variabili `NEXUS_DEMO_PASSWORD_*` dal `.env` il 24/11
