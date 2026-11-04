## Decisions

### D1 — Apache POI, senza motore di template
I segnaposto sono semplici sostituzioni di testo: basta POI (`XWPFDocument`), senza librerie di template sopra. In
Word un segnaposto può essere spezzato in più "run" (pezzi con la stessa formattazione), soprattutto nei modelli
modificati a mano. Per questo la sostituzione lavora sul testo intero del paragrafo e lo riscrive nel primo run, con
la sua formattazione. Copre:
- paragrafi, tabelle (anche annidate), intestazioni e piè di pagina;
- `\n` diventa un a capo nello stesso paragrafo, che serve per la griglia oraria.

Un segnaposto sconosciuto resta com'è: si vede nel documento ed è un errore del modello, che un test controlla per i
modelli forniti. Versione: `poi-ooxml` 6.0.0, l'ultima stabile (uscita il 19/10/2026), a versione esatta come vuole
la regola sulle versioni congelate.

### D2 — Un modello per tipo, con il ripiego
§8.3: un modello per tipo di tirocinio. I tipi però sono un catalogo dell'ADMIN (US-108): un tipo nuovo non ha un suo
modello, quindi si usa `default.docx`, che riporta il nome del tipo. Il codice del tipo è nel nome del file
(`CURRICULARE.docx`, …).

### D3 — Dati mancanti: righe vuote, non un blocco
Il documento si scarica anche con la scheda incompleta: i dati mancanti diventano `____________`, da completare a mano
come su un modulo cartaceo. Non è un limite in più per il Tutor (A14). La pagina avvisa quando la scheda non è
completa.

### D4 — Riuso delle regole della scheda
Accesso e stati sono quelli della scheda (`InternshipService.get`): solo il Tutor della segnalazione,
dall'appuntamento fissato in poi. I dati che la scheda non espone (P. IVA, sede legale dell'azienda, mansione,
progetto, tutor promotore) si leggono dalla segnalazione dopo il controllo di accesso.

### D5 — Download dal frontend con l'autenticazione
Il file arriva da un'API autenticata: un semplice link non porterebbe il token. Il frontend lo scarica con `axios`
(`blob`), ne ricava il nome da `Content-Disposition` e lo salva con un link temporaneo; l'esito si annuncia. Il
service worker non mette in cache le API (dati personali), quindi neanche questo file.

### D6 — Pronta per US-1005
Il tipo di documento è un enum (`InternshipDocumentKind`): il Progetto Formativo aggiungerà il suo valore, la sua
cartella di modelli e il pulsante.
