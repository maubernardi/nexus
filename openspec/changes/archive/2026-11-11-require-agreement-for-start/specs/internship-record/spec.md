## ADDED Requirements

### Requirement: Convenzione e progetti formativi per l'avvio
Per avviare il tirocinio la scheda MUST avere anche la convenzione completa (numero, data e data di stipula) e almeno
un progetto formativo. Se mancano, la scheda SHALL elencarli tra i dati che mancano per l'avvio, ciascuno con il
collegamento alla sua sezione, e l'avvio MUST essere rifiutato con l'elenco di ciò che manca.

#### Scenario: Convenzione senza data di stipula
- **WHEN** la scheda è completa tranne la data di stipula della convenzione
- **THEN** la scheda elenca la convenzione tra i dati mancanti e l'avvio è rifiutato

#### Scenario: Nessun progetto formativo
- **WHEN** la scheda ha la convenzione ma nessun progetto formativo
- **THEN** la scheda elenca «almeno un progetto formativo» tra i dati mancanti, con il collegamento alla sezione dei
  progetti

#### Scenario: Scheda completa
- **WHEN** la scheda ha anche la convenzione completa e un progetto formativo
- **THEN** il tirocinio si può avviare
