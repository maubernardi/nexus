## ADDED Requirements

### Requirement: Valutazioni dei tirocini nella scheda dell'azienda
La scheda dell'azienda SHALL avere la sezione «Valutazioni dei tirocini». La sezione SHALL mostrare la media delle
valutazioni dei tirocini dell'azienda, cioè la media delle medie dei tre voti di ogni report, come faccina con il
numero, e quanti tirocini sono stati valutati. Sotto SHALL esserci l'elenco delle valutazioni, a pagine e dalla più
recente: data di fine del tirocinio, faccina della media, nota e nome del Tutor che l'ha data. Il nome del beneficiario
MUST NOT comparire. La vedono operatori del Call Center, ADMIN e Tutor. Senza valutazioni la sezione SHALL dirlo.

#### Scenario: Media delle medie
- **WHEN** un'azienda ha due tirocini valutati, con medie 4,3 e 5
- **THEN** la sezione mostra «Ottima, 4,7 su 5 · 2 tirocini» e le due valutazioni, dalla più recente

#### Scenario: Nome del Tutor
- **WHEN** l'operatore guarda una valutazione
- **THEN** vede la data di fine del tirocinio, la faccina, la nota e il nome del Tutor, e nessun dato del beneficiario

#### Scenario: Molte valutazioni
- **WHEN** un'azienda ha più di 10 valutazioni
- **THEN** l'elenco mostra le 10 più recenti, con i pulsanti per le pagine successive

#### Scenario: Nessuna valutazione
- **WHEN** l'azienda non ha tirocini valutati
- **THEN** la sezione dice «Nessun tirocinio valutato»

### Requirement: Scheda dell'azienda per il Tutor
Nella lista delle aziende del Tutor il nome di ogni azienda SHALL aprire una scheda in sola lettura. La scheda contiene
i dati della lista (categoria, zona, indirizzo, stelle e pollici) e la sezione «Valutazioni dei tirocini». La lista
SHALL avere anche una colonna con la faccina della media e il numero di tirocini valutati. L'API della scheda del
Tutor MUST restituire solo i dati della lista, e solo per le aziende attive (altrimenti `404`).

#### Scenario: Scheda del Tutor
- **WHEN** il Tutor apre un'azienda dalla sua lista
- **THEN** vede categoria, zona, indirizzo, stelle, pollici e le valutazioni, senza poter modificare nulla

#### Scenario: Azienda disattivata
- **WHEN** il Tutor apre l'indirizzo della scheda di un'azienda disattivata
- **THEN** vede «Azienda non trovata»
