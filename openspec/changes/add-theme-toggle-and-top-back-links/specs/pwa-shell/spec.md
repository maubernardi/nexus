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

### Requirement: Ritorno all'elenco in cima e in fondo alle schede
Le schede con un collegamento per tornare all'elenco o alla pagina di partenza SHALL averlo sia in cima, sopra il
titolo, sia in fondo, con lo stesso testo e la stessa destinazione. Valgono le schede di aziende, segnalazioni,
beneficiari e loro modifica, annunci della bacheca, tirocinio, report di restituzione e pagine degli utenti.

#### Scenario: Scheda dell'azienda
- **WHEN** l'operatore apre la scheda di un'azienda
- **THEN** sopra il titolo c'è «Torna all'elenco delle aziende», e lo stesso collegamento c'è in fondo alla scheda

#### Scenario: Beneficiario aperto da una segnalazione
- **WHEN** il Tutor apre un beneficiario dalla segnalazione n. 24
- **THEN** in cima e in fondo c'è «Torna alla segnalazione n. 24»
