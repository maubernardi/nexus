# notifications Specification

## Purpose
Notifiche in app: ogni passaggio della segnalazione avvisa la controparte (Tutor ↔ operatore che l'ha in carico).
## Requirements
### Requirement: Notifiche ai destinatari
Il sistema SHALL avvisare con una notifica in app la controparte di ogni passaggio della segnalazione: il Tutor della
segnalazione per la proposta all'azienda e per la decisione sulla nuova tipologia, l'operatore che ha in carico la
segnalazione per l'accettazione della proposta. Chi compie l'azione MUST NOT ricevere la propria notifica. Le notifiche
MUST NOT contenere dati personali oltre al riferimento alla segnalazione.

#### Scenario: Proposta al Tutor
- **WHEN** l'operatore propone una mansione per la segnalazione n. 15
- **THEN** il Tutor della segnalazione ha una notifica non letta "C'è una proposta di tirocinio per la segnalazione n. 15"
  e l'operatore nessuna

#### Scenario: Accettazione all'operatore
- **WHEN** il Tutor accetta la proposta
- **THEN** l'operatore che ha in carico la segnalazione riceve la notifica dell'accettazione

### Requirement: Campanella ed elenco
Ogni utente SHALL vedere nell'intestazione una campanella con il numero di notifiche non lette, compreso nel nome
accessibile, aggiornato periodicamente e annunciato agli screen reader quando aumenta; e una pagina "Notifiche" con le
notifiche dalla più recente, lo stato "Nuova" in testo, il link alla segnalazione, "Segna come letta" e "Segna tutte
come lette". Aprire la segnalazione dalla notifica la segna come letta. Un utente MUST NOT leggere o modificare le
notifiche altrui.

#### Scenario: Contatore
- **WHEN** il Tutor ha 3 notifiche non lette
- **THEN** la campanella si chiama "Notifiche, 3 da leggere" e porta alla pagina "Notifiche"

#### Scenario: Segna come lette
- **WHEN** il Tutor segna tutte le notifiche come lette
- **THEN** il contatore torna a zero e l'esito è annunciato

#### Scenario: Notifica altrui
- **WHEN** un utente prova a segnare come letta una notifica di un altro
- **THEN** la risposta è `404`

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

### Requirement: Firma e testi delle email
Ogni email inviata da NEXUS, dal backend o da Keycloak, SHALL avere l'oggetto che inizia con «PORTALE NEXUS - » e
chiudersi con la firma «Portale Nexus». L'avviso di
cambio della password SHALL dire «<username>, le tue credenziali sono state cambiate il <data> alle <ora> da questo
indirizzo IP: <ip>» e invitare a contattare l'amministratore se non è stato l'utente; data e ora MUST essere quelle
italiane (fuso di Roma).

#### Scenario: Firma nelle email del backend
- **WHEN** NEXUS invia una notifica, un codice per il cambio di indirizzo o l'avviso al vecchio indirizzo
- **THEN** l'oggetto inizia con «PORTALE NEXUS - » (es. «PORTALE NEXUS - Segnalazione n. 34») e l'email finisce con
  «Portale Nexus»

#### Scenario: Firma nelle email di Keycloak
- **WHEN** Keycloak invia l'invito, il collegamento per reimpostare la password o l'avviso di cambio
- **THEN** l'oggetto inizia con «PORTALE NEXUS - » (es. «PORTALE NEXUS - Reimposta la password») e l'email finisce con
  «Portale Nexus», nel testo semplice e nell'HTML

#### Scenario: Avviso di cambio della password
- **WHEN** l'utente `mario.rossi` cambia la password alle 12:46 italiane del 7 novembre 2026
- **THEN** riceve «mario.rossi, le tue credenziali sono state cambiate il 07/11/2026 alle 12:46 da questo indirizzo
  IP: …», con l'invito a contattare l'amministratore e la firma

