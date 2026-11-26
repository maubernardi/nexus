## ADDED Requirements

### Requirement: Affidabilità e interesse al progetto dell'azienda
L'azienda SHALL avere due valutazioni facoltative del Call Center: l'**affidabilità**, da 1 a 4 stelle, e l'**interesse
al progetto**, da 1 a 2 pollici alzati. Operatori del Call Center e ADMIN SHALL poterle scegliere, cambiare e togliere
dal modulo dell'azienda, nuova o esistente. Il sistema MUST rifiutare con `400` un valore fuori da questi intervalli,
e il database MUST rifiutarlo con un vincolo. Ogni cambio SHALL entrare nell'audit dell'azienda con il valore di prima
e di dopo.

Nel modulo ogni valutazione SHALL essere un gruppo di opzioni: tutte le icone vuote finché non si sceglie, e piene fino
a quella scelta. Il gruppo SHALL funzionare da tastiera e avere un nome per ogni opzione, per esempio «3 stelle su 4»;
lo stato MUST NOT dipendere solo dal colore.

L'elenco delle aziende SHALL mostrare stelle e pollici in due colonne, e offrire due filtri che si sommano agli altri:
«almeno N stelle» (da 1 a 4) e «interesse al progetto» (almeno un pollice, oppure due). I filtri SHALL restare
nell'indirizzo come gli altri.

#### Scenario: Scelta delle stelle
- **WHEN** l'operatore apre la scheda di un'azienda senza affidabilità, clicca la terza stella e salva
- **THEN** prima del clic vede 4 stelle vuote; dopo, le prime tre piene. L'azienda ha affidabilità 3 e l'audit
  registra il cambio da nessuna a 3

#### Scenario: Valutazione tolta
- **WHEN** l'operatore toglie l'interesse al progetto di un'azienda con due pollici e salva
- **THEN** l'azienda resta senza interesse al progetto e l'audit registra il cambio da 2 a nessuno

#### Scenario: Da tastiera
- **WHEN** l'operatore arriva alle stelle con il tasto Tab e preme la freccia a destra
- **THEN** si sceglie la stella successiva, e il lettore di schermo legge per esempio «Affidabilità, 2 stelle su 4»

#### Scenario: Valore fuori intervallo
- **WHEN** una richiesta all'API salva un'azienda con affidabilità 5 o interesse 3
- **THEN** la risposta è `400`, con l'errore sul campo, e l'azienda non cambia

#### Scenario: Colonne dell'elenco
- **WHEN** il Call Center apre l'elenco delle aziende
- **THEN** ogni riga mostra le stelle e i pollici dell'azienda, con il testo per il lettore di schermo; un trattino
  per quelle senza valutazione

#### Scenario: Filtro per affidabilità
- **WHEN** l'operatore sceglie «Almeno 3 stelle»
- **THEN** vede, dalla prima pagina, solo le aziende con 3 o 4 stelle, e l'indirizzo ricorda il filtro

#### Scenario: Filtro per interesse
- **WHEN** l'operatore sceglie «Due pollici»
- **THEN** vede solo le aziende con interesse al progetto 2

#### Scenario: Ruoli
- **WHEN** un Tutor chiede l'elenco o salva un'azienda dall'API
- **THEN** la risposta è `403`

### Requirement: Nome senza i simboli del database attuale
Nel database attuale affidabilità e interesse si segnano all'inizio del nome: ogni `$` è un punto di affidabilità,
ogni `£` di interesse; una `&` o un `^` iniziali sono refusi. Le aziende già presenti SHALL essere convertite senza
perdere il dato:
- ogni `$` all'inizio del nome vale una stella, al massimo 4 (cinque `$` valgono 4 stelle);
- ogni `£` all'inizio del nome vale un pollice, al massimo 2;
- `&` e `^` all'inizio del nome si tolgono;
- i simboli si contano in qualunque ordine, anche separati da spazi; poi il nome si ripulisce.

La `&` dentro il nome, come in «Rossi & C.», MUST restare. Un nome che resterebbe vuoto SHALL diventare il segnaposto
dei nomi da completare.

Dopo la conversione il nome di un'azienda MUST NOT iniziare con `$`, `£`, `&` o `^`: il modulo SHALL dirlo, indicando
stelle e pollici; l'API SHALL rispondere `400` con l'errore sul campo; il database MUST rifiutarlo con un vincolo.

#### Scenario: Stelle e pollici dal nome
- **WHEN** la conversione trova l'azienda «£$$ Officina Rossi»
- **THEN** l'azienda si chiama «Officina Rossi», ha 2 stelle e un pollice

#### Scenario: Cinque simboli
- **WHEN** la conversione trova «$$$$$ Bar Centrale»
- **THEN** l'azienda si chiama «Bar Centrale» e ha 4 stelle

#### Scenario: Refusi
- **WHEN** la conversione trova «&Ferramenta Bianchi» e «£& Forno Verdi»
- **THEN** le aziende si chiamano «Ferramenta Bianchi», senza valutazioni, e «Forno Verdi», con un pollice

#### Scenario: & dentro il nome
- **WHEN** la conversione trova «Rossi & C. S.n.c.»
- **THEN** il nome non cambia e l'azienda resta senza valutazioni

#### Scenario: Nome fatto solo di simboli
- **WHEN** la conversione trova l'azienda importata n. 1234 chiamata «$$»
- **THEN** l'azienda si chiama «Azienda senza nome n. 1234», ha 2 stelle ed è tra le aziende da completare

#### Scenario: Traccia della conversione
- **WHEN** la conversione cambia un'azienda
- **THEN** l'audit dell'azienda registra il nome di prima e di dopo, e le stelle e i pollici ricevuti

#### Scenario: Simbolo scritto nel modulo
- **WHEN** l'operatore salva un'azienda chiamata «$$ Officina Rossi»
- **THEN** il modulo non la salva e dice che il nome non può iniziare con `$`, `£`, `&` o `^`, e che affidabilità e
  interesse si segnano con stelle e pollici
