## ADDED Requirements

### Requirement: Zona dell'azienda
L'azienda SHALL avere una zona facoltativa, scelta dal catalogo delle zone (US-106) tra le voci attive, o quella che
l'azienda ha già anche se nel frattempo disattivata. La sceglie il Call Center o l'ADMIN dal modulo dell'azienda, e il
cambio SHALL entrare nell'audit con il nome della zona di prima e di dopo. L'elenco delle aziende del Call Center SHALL
mostrare la zona in una colonna e filtrare per zona; il filtro SHALL restare nell'indirizzo. Il database MUST
rifiutare una zona inesistente con un vincolo.

#### Scenario: Zona scelta
- **WHEN** l'operatore sceglie la zona «Rifredi» per un'azienda e salva
- **THEN** l'azienda ha la zona Rifredi e l'audit registra il cambio da nessuna a «Rifredi»

#### Scenario: Filtro per zona
- **WHEN** l'operatore sceglie la zona «Rifredi» nel filtro dell'elenco
- **THEN** vede, dalla prima pagina, solo le aziende di Rifredi

#### Scenario: Zona non valida
- **WHEN** una richiesta all'API salva un'azienda con una zona inesistente o disattivata che l'azienda non ha
- **THEN** la risposta è `400`, con l'errore sul campo della zona

### Requirement: Elenco delle aziende per il Tutor
Il Tutor SHALL avere nel menu la voce «Aziende», con l'elenco in sola lettura delle sole aziende attive, una pagina
alla volta come l'elenco del Call Center. Per ogni azienda SHALL vedere soltanto ragione sociale, settore, zona,
indirizzo con il comune (la sede operativa se indicata, altrimenti la legale), affidabilità e interesse al progetto.
SHALL poter cercare per ragione sociale e filtrare per settore e zona, con i filtri nell'indirizzo.

L'API per il Tutor MUST restituire soltanto quei campi: niente partita IVA, recapiti, referenti, note, mansioni o
storico. La ricerca MUST NOT guardare la partita IVA. Il Tutor MUST NOT creare, modificare, attivare o disattivare
aziende, né usare l'API completa delle aziende.

#### Scenario: Elenco del Tutor
- **WHEN** il Tutor apre «Aziende»
- **THEN** vede le aziende attive in ordine di ragione sociale, con settore, zona, indirizzo con il comune, stelle e
  pollici, e nessun altro dato

#### Scenario: Sede operativa
- **WHEN** un'azienda ha la sede legale a Firenze e la sede operativa a Prato
- **THEN** nell'elenco del Tutor l'indirizzo è quello di Prato

#### Scenario: Ricerca e filtri
- **WHEN** il Tutor cerca «officina» e sceglie la zona «Rifredi»
- **THEN** vede, dalla prima pagina, le aziende attive di Rifredi con «officina» nel nome, e l'indirizzo ricorda la
  ricerca e il filtro

#### Scenario: Partita IVA esclusa
- **WHEN** il Tutor cerca la partita IVA di un'azienda
- **THEN** non trova l'azienda, e nella risposta dell'API la partita IVA non c'è mai

#### Scenario: Aziende disattivate
- **WHEN** un'azienda è disattivata
- **THEN** non compare nell'elenco del Tutor

#### Scenario: Solo lettura
- **WHEN** un Tutor chiama l'API completa delle aziende, o prova a crearne o modificarne una
- **THEN** la risposta è `403`

### Requirement: Azzeramento dei filtri dell'elenco delle aziende
Negli elenchi delle aziende, del Call Center e del Tutor, quando è attiva almeno una ricerca o un filtro SHALL esserci
il pulsante «Azzera i filtri». Il pulsante SHALL togliere ricerca e filtri e riportare l'elenco alla prima pagina,
lasciando le aziende per pagina scelte; poi il focus SHALL andare al campo di ricerca. Senza filtri il pulsante MUST NOT
esserci.

#### Scenario: Azzeramento
- **WHEN** l'operatore ha una ricerca, il filtro «Almeno 2 stelle» e 50 aziende per pagina, e preme «Azzera i filtri»
- **THEN** vede la prima pagina di tutte le aziende attive, 50 per pagina, con il cursore nel campo di ricerca

#### Scenario: Nessun filtro
- **WHEN** l'elenco non ha ricerca né filtri
- **THEN** il pulsante «Azzera i filtri» non c'è
