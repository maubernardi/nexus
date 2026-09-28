## 1. Immagini container

- [x] 1.1 `backend/Dockerfile` (runtime Temurin 25.0.4.1 JRE, utente non root, layer del jar) e verifica `docker run` locale
- [x] 1.2 Backend: `spring.flyway.user/password` separabili, `forward-headers-strategy`, `jwk-set-uri` configurabile (default invariati) + test verdi
- [x] 1.3 `frontend/Dockerfile` (Caddy 2.11.4 + `dist`) e `Caddyfile` con routing, blocco `/admin`, header di sicurezza e cache

## 2. Stack di produzione

- [ ] 2.1 `infra/deploy/compose.yaml`: web, backend, keycloak, postgres; solo `web` con porte pubbliche; healthcheck, limiti di memoria, log
- [ ] 2.2 Init PostgreSQL: utenti `nexus_owner`/`nexus_app`/`keycloak`, schema e default privileges
- [ ] 2.3 Realm demo con placeholder delle password; verifica dell'import con Keycloak 26.7.4 in locale
- [ ] 2.4 Prova completa in locale dello stack di produzione (certificati interni di Caddy, domini simulati): login, `/api/v1/me`, `/admin` bloccato, trigger non eliminabile da `nexus_app`

## 3. Server e deploy

- [ ] 3.1 `infra/deploy/bootstrap.sh` (idempotente) e script `nexus-deploy` (forced command)
- [ ] 3.2 Workflow `deploy-demo.yml` (build, push GHCR, deploy via SSH, smoke test) con action fissate per SHA
- [ ] 3.3 Chiave di deploy: generazione, segreti dell'environment GitHub `demo` (chiave, known_hosts verificato)

## 4. Messa online (con il committente)

- [x] 4.1 DNS IONOS: A `auth` e `www`, CAA Let's Encrypt; verifica della propagazione
- [ ] 4.2 Esecuzione di `bootstrap.sh` sul server
- [ ] 4.3 Primo deploy da GitHub; verifica degli scenari della spec `deployment` su `portalenexus.it`
- [ ] 4.4 README (DNS, preparazione, deploy, tunnel per la console Keycloak), `openspec validate`, PR con CI verde
