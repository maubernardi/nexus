## ADDED Requirements

### Requirement: Testi dei messaggi modificabili dall'ADMIN
L'ADMIN SHALL poter cambiare, da una pagina «Testi dei messaggi», i testi delle notifiche e delle email di NEXUS. Per
ogni tipo di notifica, separatamente: il testo nell'app, la parte distintiva dell'oggetto dell'email e il testo
dell'email (PO, 22/11). Per le due email del cambio di indirizzo: oggetto e testo. La pagina SHALL dire per ogni
messaggio a chi va e quando parte, elencare i segnaposto ammessi e mostrare l'anteprima composta con le parti fisse.
Un testo vuoto, troppo lungo, con un segnaposto non ammesso o con parentesi graffe fuori da un segnaposto MUST essere
rifiutato con il motivo sul campo. L'ADMIN SHALL poter ripristinare il testo predefinito; finché non lo cambia valgono
i predefiniti. Restano fissi, MUST NOT essere modificabili:
- l'inizio dell'oggetto, «PORTALE NEXUS - » e, per le notifiche, «Segnalazione n. N - »;
- il collegamento alla segnalazione e l'avvertenza del messaggio automatico;
- la firma «Portale Nexus».

Ogni modifica e ogni ripristino SHALL entrare nell'audit, con il testo prima e dopo. Solo l'ADMIN MAY vedere e cambiare
i testi. Le email di Keycloak restano fuori dalla pagina.

#### Scenario: Testo nell'app cambiato
- **WHEN** l'ADMIN scrive per «Proposta di tirocinio», nel testo nell'app, «Proposta per la n. {numero}: guardala»
- **THEN** la notifica della segnalazione n. 12 nell'app dice «Proposta per la n. 12: guardala», e l'email resta con
  il suo testo

#### Scenario: Oggetto con la parte distintiva
- **WHEN** l'ADMIN scrive come parte distintiva «Valuta la proposta»
- **THEN** l'email della segnalazione n. 12 ha l'oggetto «PORTALE NEXUS - Segnalazione n. 12 - Valuta la proposta»

#### Scenario: Segnaposto sconosciuto
- **WHEN** l'ADMIN scrive «Ciao {nome}» nel testo di una notifica
- **THEN** il testo non si salva e il campo dice che si può usare solo {numero}

#### Scenario: Ripristino
- **WHEN** l'ADMIN ripristina il testo predefinito di una parte cambiata
- **THEN** torna il testo di prima della modifica, e l'audit registra il ripristino

#### Scenario: Testo uguale al predefinito
- **WHEN** l'ADMIN salva un testo identico al predefinito
- **THEN** non risulta cambiato e l'audit non registra nulla

#### Scenario: Altri ruoli
- **WHEN** un Tutor o un operatore apre la pagina o chiama l'API dei testi
- **THEN** l'accesso è negato

## MODIFIED Requirements

### Requirement: Notifiche anche via email
Ogni notifica in app SHALL arrivare anche via email alla stessa persona, all'indirizzo dell'anagrafica utenti. L'email
SHALL avere il testo dell'email del suo tipo, che può essere diverso da quello nell'app (US-1204), con il numero della
segnalazione e il collegamento alla segnalazione: nessun altro dato personale. L'invio SHALL avvenire dopo il
salvataggio dell'operazione, tramite l'SMTP IONOS, da un indirizzo del dominio. Un invio fallito MUST NOT annullare
l'operazione né la notifica in app. La password SMTP MUST NOT stare nel repository; senza password le email restano
spente.

#### Scenario: Email della notifica
- **WHEN** il Tutor invia il report di restituzione e l'operatore riceve la notifica in app
- **THEN** l'operatore riceve anche un'email da `noreply@portalenexus.it` con oggetto «PORTALE NEXUS - Segnalazione n.
  N - Percorso concluso», il testo dell'email e il collegamento alla segnalazione

#### Scenario: Server di posta irraggiungibile
- **WHEN** l'invio dell'email fallisce
- **THEN** l'operazione resta salvata, la notifica in app c'è e il log registra l'errore senza l'indirizzo

#### Scenario: Password non configurata
- **WHEN** il backend parte senza la password SMTP
- **THEN** le email restano spente, il log lo segnala e il resto dell'app funziona

### Requirement: Firma e testi delle email
Ogni email inviata da NEXUS, dal backend o da Keycloak, SHALL avere l'oggetto che inizia con «PORTALE NEXUS - » e
chiudersi con la firma «Portale Nexus». Le email delle notifiche SHALL avere nell'oggetto, dopo «Segnalazione n. N - »,
la parte distintiva del loro tipo (PO, 22/11). L'avviso di cambio della password SHALL dire «<username>, le tue
credenziali sono state cambiate il <data> alle <ora> da questo indirizzo IP: <ip>» e invitare a contattare
l'amministratore se non è stato l'utente; data e ora MUST essere quelle italiane (fuso di Roma).

#### Scenario: Firma nelle email del backend
- **WHEN** NEXUS invia una notifica, un codice per il cambio di indirizzo o l'avviso al vecchio indirizzo
- **THEN** l'oggetto inizia con «PORTALE NEXUS - » (es. «PORTALE NEXUS - Segnalazione n. 34 - Percorso concluso») e
  l'email finisce con «Portale Nexus»

#### Scenario: Firma nelle email di Keycloak
- **WHEN** Keycloak invia l'invito, il collegamento per reimpostare la password o l'avviso di cambio
- **THEN** l'oggetto inizia con «PORTALE NEXUS - » (es. «PORTALE NEXUS - Reimposta la password») e l'email finisce con
  «Portale Nexus», nel testo semplice e nell'HTML

#### Scenario: Avviso di cambio della password
- **WHEN** l'utente `mario.rossi` cambia la password alle 12:46 italiane del 7 novembre 2026
- **THEN** riceve «mario.rossi, le tue credenziali sono state cambiate il 07/11/2026 alle 12:46 da questo indirizzo
  IP: …», con l'invito a contattare l'amministratore e la firma
