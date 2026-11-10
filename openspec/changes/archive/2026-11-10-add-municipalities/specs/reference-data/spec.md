## ADDED Requirements

### Requirement: Comuni e province
Il sistema SHALL conoscere i comuni e le province italiane con il loro periodo di validità. L'ADMIN SHALL caricarli dal
file dell'ISTAT «Elenco dei comuni italiani», indicando la data da cui il file vale, e SHALL vedere l'anteprima delle
differenze (comuni e province nuovi, soppressi, cambiati di nome, codice o provincia) prima di confermare. Alla conferma
ogni versione soppressa o cambiata MUST chiudersi alla data indicata e la nuova versione MUST aprirsi da quella data;
nessuna versione MUST essere cancellata. Il comune MUST essere identificato dal codice catastale. Un file non
riconosciuto o incompleto, o una data non successiva all'ultimo caricamento, MUST essere rifiutato con il motivo. Ogni
caricamento SHALL essere registrato nell'audit. Gli utenti autenticati SHALL poter cercare i comuni validi per nome,
dalla seconda lettera, con la sigla della provincia.

#### Scenario: Primo caricamento
- **WHEN** l'ADMIN controlla il file ISTAT su un elenco vuoto
- **THEN** l'anteprima indica tutti i comuni e le province come nuovi e, alla conferma, la ricerca «sesto f» trova
  «Sesto Fiorentino (FI)»

#### Scenario: Comune cambiato
- **WHEN** l'ADMIN carica un file in cui un comune ha un nuovo nome o una nuova provincia
- **THEN** l'anteprima lo indica tra i cambiati; dopo la conferma la versione precedente vale fino alla data del
  caricamento e la ricerca trova solo il nuovo nome

#### Scenario: Comune soppresso
- **WHEN** l'ADMIN carica un file senza un comune presente nell'elenco
- **THEN** il comune è tra i soppressi e, dopo la conferma, non compare più nella ricerca ma la sua versione resta

#### Scenario: File sbagliato
- **WHEN** l'ADMIN carica un file che non è l'elenco dell'ISTAT, oppure un elenco con pochi comuni
- **THEN** il caricamento è rifiutato con il motivo e l'elenco non cambia

#### Scenario: Data non valida
- **WHEN** l'ADMIN indica una data non successiva a quella dell'ultimo caricamento
- **THEN** il caricamento è rifiutato

#### Scenario: Solo l'ADMIN carica
- **WHEN** un Tutor chiede di caricare il file
- **THEN** la risposta è `403`; la ricerca invece gli è permessa
