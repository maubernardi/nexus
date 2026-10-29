## 1. Tema

- [x] 1.1 Tema `nexus` (login): `template.ftl` da `keycloak.v2` con intestazione e piè di pagina del sito, CSS con i token chiari e scuri, testi italiani, logo
- [x] 1.2 Verifica sulla pagina vera: chiaro, scuro, errore, 390 px, axe WCAG 2.2 AA

## 2. Lingua e distribuzione

- [x] 2.1 Realm di sviluppo e demo: `loginTheme: nexus`, sola lingua `it`
- [x] 2.2 Immagine `nexus-keycloak`, workflow di deploy e `infra/deploy/compose.yaml`; tema montato in sviluppo
- [x] 2.3 Controllo in CI della pagina di accesso con il tema
- [x] 2.4 README: comando `kcadm` per il realm già esistente sulla demo, provato in locale

## 3. Consegna

- [ ] 3.1 PR con CI verde; dopo il merge, deploy e aggiornamento del realm della demo (su richiesta del PO)
