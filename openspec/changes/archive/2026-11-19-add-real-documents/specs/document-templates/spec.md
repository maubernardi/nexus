## MODIFIED Requirements

### Requirement: Controllo del file e dei segnaposto
Al caricamento e alla sostituzione NEXUS MUST controllare che il file sia un documento Word (.docx) leggibile e che
usi solo i segnaposto che sa compilare. Altrimenti il file è rifiutato e il messaggio elenca i segnaposto sconosciuti.
La pagina SHALL mostrare l'elenco dei segnaposto che si possono usare, divisi per gruppo (per esempio soggetto
promotore, tirocinante, convenzione), ognuno con il suo significato.

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
