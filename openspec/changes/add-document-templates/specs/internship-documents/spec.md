## ADDED Requirements

### Requirement: Scelta del modello caricato
Quando il Tutor scarica un documento, NEXUS SHALL cercare i modelli caricati dall'ADMIN per quel documento che:
- sono attivi;
- sono del progetto della segnalazione;
- sono del tipo di tirocinio della scheda;
- hanno l'ente di presa in carico del beneficiario oppure nessun ente.

Se ce n'è almeno uno con l'ente del beneficiario, MUST contare solo quelli con l'ente. Poi:
- con un solo modello, il documento MUST usare quello;
- con più modelli, il Tutor SHALL sceglierlo da un elenco con i loro nomi, e il download MUST essere rifiutato finché
  non sceglie;
- senza modelli, il documento MUST usare il modello generico che resta nel codice.

Un modello che non è tra quelli validi MUST essere rifiutato. Il download SHALL registrare nella cronologia anche il
modello usato, oppure «modello generico».

#### Scenario: Un modello con l'ente
- **WHEN** per la convenzione di GOL Inclusione ci sono un modello senza ente e uno per l'ente del beneficiario
- **THEN** il Tutor scarica la convenzione con il modello dell'ente, senza scegliere

#### Scenario: Più modelli
- **WHEN** per il progetto formativo ci sono due modelli attivi senza ente per lo stesso progetto e tipo
- **THEN** la scheda chiede quale usare e il download parte con il modello scelto

#### Scenario: Nessun modello
- **WHEN** non c'è un modello caricato per il documento
- **THEN** il documento usa il modello generico, come prima di questa funzione
