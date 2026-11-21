## ADDED Requirements

### Requirement: Passaggio dalla demo alla produzione
Il passaggio SHALL avvenire sullo stesso server con uno script lanciato dal PO, che chiede conferma prima di toccare i
dati e parte da un backup della demo su IONOS. La produzione SHALL tenere configurazione e cataloghi della demo e le
impostazioni di Keycloak. SHALL togliere segnalazioni, beneficiari, utenti (in NEXUS e in Keycloak, tranne l'account
tecnico del backend) e aziende della demo, con la loro storia. Le aziende SHALL arrivare dal database attuale.
La pulizia del database SHALL avvenire in una sola transazione. Le regole di sola aggiunta MAY essere sospese solo
dentro quella transazione e MUST tornare attive alla fine. Dopo il passaggio il backend MUST NOT caricare dati
dimostrativi, e Keycloak MUST NOT poter ricreare gli utenti di prova. Lo script e la pulizia MUST NOT stampare dati
personali.

#### Scenario: Pulizia riuscita
- **WHEN** la pulizia gira sul database della demo
- **THEN** segnalazioni, beneficiari, utenti, aziende e le loro voci dell'audit non ci sono più. Configurazione e
  cataloghi hanno le stesse righe di prima, e le regole di sola aggiunta rifiutano di nuovo le cancellazioni

#### Scenario: Pulizia interrotta
- **WHEN** un passo o un controllo della pulizia fallisce
- **THEN** la transazione si annulla, il database resta quello della demo e lo script dice come riavviarla

#### Scenario: Voci dimostrative rimaste
- **WHEN** la pulizia finisce
- **THEN** elenca, solo con il codice, progetti e tipologie di mansione mai modificati dopo i dati dimostrativi e gli
  enti con i dati inventati, da controllare dai Cataloghi

#### Scenario: Backend senza dati dimostrativi
- **WHEN** dopo la pulizia il backend parte senza il profilo `seed`
- **THEN** Flyway valida le migrazioni e l'applicazione parte

#### Scenario: Keycloak ripartito vuoto
- **WHEN** il database di Keycloak della produzione riparte vuoto e Keycloak importa il realm
- **THEN** il realm non ha utenti, tranne l'account tecnico del backend

#### Scenario: Tornare alla demo
- **WHEN** il passaggio non riesce dopo la pulizia
- **THEN** il PO ripristina il backup fatto all'inizio dello script, seguendo il README, e la demo torna com'era
