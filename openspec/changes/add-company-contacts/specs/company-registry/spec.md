## ADDED Requirements

### Requirement: Referenti dell'azienda
Il Call Center e l'ADMIN SHALL poter registrare nella scheda dell'azienda **fino a 10 referenti**, in ordine. Ogni
referente ha nome, ruolo, telefono, cellulare ed email, tutti facoltativi. Ogni referente MUST avere almeno il nome o il
ruolo. Il ruolo si sceglie tra le voci attive del catalogo. Un referente con un ruolo poi disattivato SHALL mantenerlo
quando si salva la scheda. Un ruolo disattivato o inesistente MUST NOT essere scelto altrimenti (`400` sul campo).

Il referente registrato prima di questa funzione SHALL diventare il primo referente, senza ruolo. I referenti sono
dati personali: l'audit dell'azienda MUST registrarli solo come «cambiati», senza valori. L'elenco delle aziende SHALL
mostrare il primo referente e quanti sono gli altri. Il dettaglio dell'abbinamento SHALL mostrare tutti i referenti con
ruolo e recapiti.

#### Scenario: Due referenti con il ruolo
- **WHEN** l'operatore aggiunge «Paola Neri», Titolare, con il cellulare, e un secondo referente, Responsabile del
  personale, con l'email, e salva
- **THEN** la scheda mostra i due referenti in quell'ordine e l'audit registra i referenti come cambiati, senza valori

#### Scenario: Solo il ruolo
- **WHEN** l'operatore aggiunge un referente con il solo ruolo «Responsabile del personale»
- **THEN** il salvataggio riesce

#### Scenario: Né nome né ruolo
- **WHEN** l'operatore aggiunge un referente con il solo telefono
- **THEN** la risposta è `400` con l'errore sul nome di quel referente

#### Scenario: Referente di prima
- **WHEN** l'operatore apre un'azienda che aveva il referente «Mario Rossi»
- **THEN** «Mario Rossi» è il primo referente, senza ruolo

#### Scenario: Ruolo disattivato sul referente
- **WHEN** l'operatore salva la scheda di un'azienda con un referente il cui ruolo è stato disattivato
- **THEN** il ruolo resta e il salvataggio riesce

#### Scenario: Ruolo non valido
- **WHEN** il modulo invia un ruolo disattivato che nessun referente dell'azienda ha, o un ruolo inesistente
- **THEN** la risposta è `400` con l'errore sul ruolo di quel referente

#### Scenario: Troppi referenti
- **WHEN** il modulo invia 11 referenti
- **THEN** la risposta è `400`

### Requirement: Recapiti e sede operativa dell'azienda
L'azienda SHALL avere, oltre a telefono ed email, il **cellulare**, il **fax** e una **seconda email**, facoltativi e
con gli stessi controlli di formato di telefono ed email. L'azienda SHALL avere anche una **sede operativa**
facoltativa, con l'indirizzo e il comune scelto dall'elenco ufficiale, come la sede legale. L'audit MUST registrare i
recapiti nuovi solo come «cambiati», come telefono ed email, e SHALL registrare la sede operativa con i valori prima e
dopo. Il dettaglio dell'abbinamento SHALL mostrare i recapiti nuovi e la sede operativa.

#### Scenario: Recapiti nuovi
- **WHEN** l'operatore indica cellulare, fax e seconda email e salva
- **THEN** la scheda li mostra e l'audit li registra come cambiati, senza valori

#### Scenario: Sede operativa
- **WHEN** l'operatore indica «Via dei Mille 3» e sceglie «Prato (PO)» come sede operativa e salva
- **THEN** il dettaglio dell'abbinamento mostra la sede operativa «Via dei Mille 3, Prato (PO)» e l'audit registra
  indirizzo e comune

#### Scenario: Fax non valido
- **WHEN** l'operatore scrive lettere nel fax
- **THEN** la risposta è `400` con l'errore sul campo `fax`
