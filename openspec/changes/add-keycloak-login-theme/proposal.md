## Why

Richiesta del PO (29/10, fuori dal piano degli sprint): la pagina di accesso di Keycloak deve rispecchiare il sito,
non il tema generico di Keycloak. La localizzazione resta attiva, ma con il solo italiano: oggi il realm offre anche
l'inglese e un browser in inglese vede la pagina in inglese.

## What Changes

- **Tema di accesso `nexus`** (`keycloak/themes/nexus/login`). Estende il tema predefinito di Keycloak 26.7.4
  (`keycloak.v2`, PatternFly 5), quindi nessuna dipendenza nuova. Riprende dal sito:
  - l'intestazione verde con il filetto oro, il logo e il nome;
  - il titolo con il filetto oro;
  - i colori e i font di sistema, chiari e scuri come preferisce il sistema;
  - campi e pulsanti alti 44 px, il focus visibile e il piè di pagina.

  Testi italiani propri ("Accedi a NEXUS") sopra quelli di Keycloak.
- **Solo italiano**:
  - realm con la sola lingua `it` (anche di default);
  - tema con `locales=it`;
  - senza il selettore della lingua.
- **Distribuzione**:
  - nuova immagine `nexus-keycloak` (`keycloak/Dockerfile`), cioè l'immagine ufficiale con il tema, costruita dal
    workflow di deploy come le altre e usata da `infra/deploy/compose.yaml`;
  - in sviluppo il tema è montato nel Keycloak di `infra/docker-compose.yml`;
  - i file dei realm (sviluppo e demo) hanno `loginTheme: nexus` e la sola lingua italiana.
- **Realm già esistente sulla demo**: l'import all'avvio non tocca un realm che c'è già. Una volta sola serve un
  comando `kcadm`, documentato nel README e provato in locale, oppure la stessa modifica dalla console.
- **CI**: un controllo nuovo costruisce l'immagine, avvia Keycloak con il realm di sviluppo e verifica la pagina:
  - che usi il tema;
  - che sia in italiano;
  - che non abbia il selettore della lingua;
  - che il CSS venga servito.

## Capabilities

### New Capabilities
- `login-page`: pagina di accesso con l'aspetto del sito, solo in italiano, accessibile.

### Modified Capabilities
<!-- nessuna -->
