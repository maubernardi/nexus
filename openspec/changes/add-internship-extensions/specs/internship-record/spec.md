## ADDED Requirements

### Requirement: Proroghe del tirocinio
A tirocinio avviato e non ancora chiuso, il Tutor SHALL poter registrare nella scheda una o più proroghe. Una proroga
è una convenzione in tutto e per tutto: numero (al massimo 50 caratteri), data, data di stipula, data di inizio e data
di fine, tutti obbligatori. Tutti gli altri dati sono comuni a tutte le convenzioni. Le proroghe sono numerate 1, 2, 3…
nell'ordine delle date. La data di inizio di una proroga MUST essere successiva alla fine della convenzione precedente,
anche di più di un giorno, e la sua fine MUST NOT precedere l'inizio. Le proroghe MUST NOT sovrapporsi, e il database
MUST impedirlo. Prima dell'avvio e dopo la chiusura le proroghe MUST NOT cambiare.

#### Scenario: Prima proroga
- **WHEN** in un tirocinio avviato, con la convenzione dal 01/12/2026 al 31/05/2027, il Tutor registra la proroga
  «2027/200» dal 15/06/2027 al 30/09/2027
- **THEN** la scheda salvata ha la proroga 1 con quei dati e la fine prevista del tirocinio è il 30/09/2027

#### Scenario: Proroga che non segue la convenzione precedente
- **WHEN** la proroga inizia il 31/05/2027, giorno di fine della convenzione
- **THEN** il salvataggio è rifiutato e la data di inizio della proroga lo spiega

#### Scenario: Prima dell'avvio
- **WHEN** il Tutor prova a registrare una proroga in una segnalazione con l'appuntamento fissato
- **THEN** il salvataggio è rifiutato: le proroghe si registrano a tirocinio avviato

### Requirement: Progetti formativi delle proroghe
Ogni proroga SHALL avere i suoi progetti formativi, con le stesse regole di quelli della convenzione iniziale:
numerati al suo interno nell'ordine delle date, dentro le date della proroga, ognuno dopo la fine del precedente. Un
progetto MUST NOT passare da una convenzione all'altra.

#### Scenario: Progetto della proroga fuori dalle sue date
- **WHEN** un progetto della proroga 1 inizia prima della data di inizio della proroga
- **THEN** il salvataggio è rifiutato e la data del progetto lo spiega

### Requirement: Fine prevista con le proroghe
La fine prevista del tirocinio SHALL essere la fine dell'ultima convenzione: quella iniziale senza proroghe, altrimenti
quella dell'ultima proroga. La scheda SHALL mostrarla. Con le proroghe, la fine della convenzione iniziale MUST
precedere l'inizio della prima proroga. Il monte ore del tirocinio SHALL contare solo i giorni coperti da una
convenzione: i giorni tra una convenzione e la successiva non contano. I periodi di sospensione MAY arrivare fino alla
fine dell'ultima proroga.

#### Scenario: Sospensione durante la proroga
- **WHEN** il Tutor registra un periodo di sospensione dentro le date della proroga 1
- **THEN** la scheda è salvata e il monte ore non conta quei giorni

#### Scenario: Monte ore con l'intervallo tra le convenzioni
- **WHEN** la convenzione finisce venerdì e la proroga inizia il lunedì di due settimane dopo
- **THEN** il monte ore del tirocinio non conta i giorni lavorativi tra le due convenzioni

#### Scenario: Convenzione iniziale allungata sopra la proroga
- **WHEN** il Tutor sposta la fine della convenzione iniziale dopo l'inizio della prima proroga
- **THEN** il salvataggio è rifiutato e la fine prevista lo spiega

### Requirement: Audit e azzeramento delle proroghe
Ogni salvataggio che cambia le proroghe o i loro progetti SHALL essere registrato nell'audit della segnalazione: le
proroghe con numero e date, i progetti con numero, date e mansione, il dettaglio della mansione solo come «cambiato».
Se l'appuntamento con l'azienda non va a buon fine, le proroghe MUST essere azzerate con gli altri dati del tirocinio.

#### Scenario: Audit della proroga
- **WHEN** il Tutor registra una proroga con un progetto formativo
- **THEN** l'audit registra la proroga e il progetto con i valori
