## MODIFIED Requirements

### Requirement: Controllo del file e dei segnaposto
Al caricamento e alla sostituzione NEXUS MUST controllare che il file sia un documento Word (.docx) leggibile e che
usi solo i segnaposto che sa compilare: quelli di NEXUS e le variabili del progetto del modello. Altrimenti il file è
rifiutato e il messaggio elenca i segnaposto sconosciuti. Lo stesso controllo SHALL valere alla riattivazione di un
modello. La pagina SHALL mostrare l'elenco dei segnaposto che si
possono usare, divisi per gruppo (per esempio soggetto promotore, tirocinante, convenzione), ognuno con il suo
significato, e le variabili di ogni progetto.

#### Scenario: Segnaposto sconosciuto
- **WHEN** l'ADMIN carica un modello che contiene `{CUP}` e `{DATA_INIZIO}`
- **THEN** il caricamento è rifiutato con il messaggio che `{CUP}` non è un segnaposto noto

#### Scenario: File non Word
- **WHEN** l'ADMIN carica un PDF o un .doc del vecchio formato
- **THEN** il caricamento è rifiutato: il file deve essere un documento Word .docx

#### Scenario: Segnaposto descritti
- **WHEN** l'ADMIN apre l'elenco dei segnaposto nella pagina «Modelli dei documenti»
- **THEN** vede per esempio `{NUMERO_CONVENZIONE}` nel gruppo «Convenzione» con la descrizione «Numero della
  convenzione: quella iniziale o la proroga del documento»

#### Scenario: Riattivazione con una variabile tolta
- **WHEN** l'ADMIN riattiva un modello che usa `{SEDE_OPERATIVA}`, tolta dal progetto mentre il modello era disattivato
- **THEN** la riattivazione è rifiutata con il messaggio che `{SEDE_OPERATIVA}` non è più un segnaposto noto

#### Scenario: Variabile del progetto
- **WHEN** GOL ha la variabile `SEDE_OPERATIVA` e l'ADMIN carica per GOL un modello che contiene `{SEDE_OPERATIVA}`
- **THEN** il modello è accettato; lo stesso file per un altro progetto è rifiutato con `{SEDE_OPERATIVA}` sconosciuto
