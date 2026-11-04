## ADDED Requirements

### Requirement: Notifiche anche via email
Ogni notifica in app SHALL arrivare anche via email alla stessa persona, all'indirizzo dell'anagrafica utenti. L'email
MUST contenere solo il testo della notifica, con il numero della segnalazione, e il collegamento alla segnalazione:
nessun altro dato personale. L'invio SHALL avvenire dopo il salvataggio dell'operazione, tramite l'SMTP IONOS, da un
indirizzo del dominio. Un invio fallito MUST NOT annullare l'operazione né la notifica in app. La password SMTP MUST
NOT stare nel repository; senza password le email restano spente.

#### Scenario: Email della notifica
- **WHEN** il Tutor invia il report di restituzione e l'operatore riceve la notifica in app
- **THEN** l'operatore riceve anche un'email da `noreply@portalenexus.it` con oggetto «NEXUS · segnalazione n. N»,
  lo stesso testo e il collegamento alla segnalazione

#### Scenario: Server di posta irraggiungibile
- **WHEN** l'invio dell'email fallisce
- **THEN** l'operazione resta salvata, la notifica in app c'è e il log registra l'errore senza l'indirizzo

#### Scenario: Password non configurata
- **WHEN** il backend parte senza la password SMTP
- **THEN** le email restano spente, il log lo segnala e il resto dell'app funziona
