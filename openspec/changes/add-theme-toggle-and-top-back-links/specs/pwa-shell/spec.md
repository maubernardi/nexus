## ADDED Requirements

### Requirement: Tema chiaro o scuro a scelta
La barra in alto SHALL avere, accanto all'utente, un pulsante che cambia il tema: con il tema chiaro mostra la luna e
passa al tema scuro; con il tema scuro mostra il sole e passa al chiaro. Il pulsante SHALL avere un nome che dice che
cosa fa («Passa al tema scuro», «Passa al tema chiaro»). La scelta SHALL restare nel browser anche dopo la chiusura e
valere su tutte le pagine; senza una scelta SHALL valere il tema del sistema operativo. All'apertura di una pagina il
tema scelto MUST valere dal primo disegno, senza passare dall'altro.

#### Scenario: Passaggio al tema scuro
- **WHEN** con il tema chiaro l'utente preme la luna
- **THEN** l'interfaccia passa ai colori scuri e il pulsante mostra il sole, con il nome «Passa al tema chiaro»

#### Scenario: Scelta ricordata
- **WHEN** l'utente ha scelto il tema scuro, chiude il browser e riapre NEXUS, con il sistema operativo in tema chiaro
- **THEN** NEXUS si apre già nel tema scuro

#### Scenario: Nessuna scelta
- **WHEN** l'utente non ha mai premuto il pulsante e il sistema operativo è in tema scuro
- **THEN** NEXUS usa il tema scuro e il pulsante mostra il sole

### Requirement: Intestazione fissa delle schede
Le schede con un collegamento per tornare all'elenco o alla pagina di partenza SHALL avere in cima un'intestazione con
il ritorno, sopra il titolo, e il titolo. Mentre la scheda scorre, l'intestazione SHALL restare ferma sotto la barra in
alto; il ritorno MUST NOT ripetersi in fondo. Valgono le schede di aziende, segnalazioni, beneficiari e loro modifica,
annunci della bacheca, tirocinio, report di restituzione e pagine degli utenti. Quando la finestra è bassa
l'intestazione SHALL scorrere con la pagina. Barra in alto e intestazione MUST NOT coprire l'elemento con il focus.

#### Scenario: Scheda lunga
- **WHEN** l'operatore apre la scheda di un'azienda e la scorre fino in fondo
- **THEN** «Torna all'elenco delle aziende» e il nome dell'azienda restano visibili sotto la barra in alto, e in fondo
  non c'è un altro ritorno

#### Scenario: Focus da tastiera
- **WHEN** l'operatore scorre i campi del modulo con il tasto Tab
- **THEN** il campo con il focus resta visibile sotto l'intestazione fissa

#### Scenario: Beneficiario aperto da una segnalazione
- **WHEN** il Tutor apre un beneficiario dalla segnalazione n. 24
- **THEN** l'intestazione fissa ha «Torna alla segnalazione n. 24»

#### Scenario: Finestra bassa
- **WHEN** la finestra è alta meno di 32 rem, per esempio uno smartphone in orizzontale o il testo molto ingrandito
- **THEN** l'intestazione della scheda scorre con la pagina
