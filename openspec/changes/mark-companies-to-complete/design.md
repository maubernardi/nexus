## Context

Vedi proposal.md, «Why». Oggi `company.vat_code` è obbligatoria, di 11 cifre e unica (`uq_company_vat_code`, V4); il
nome è obbligatorio. Il modulo dell'azienda (US-501) valida con Bean Validation sul DTO e con lo stesso controllo nel
frontend. I documenti mettono la riga `____________` al posto di ogni valore `null` (US-1004).

## Goals / Non-Goals

**Goals:** riconoscere i segnaposto ovunque si mostri l'azienda, filtrarli e impedire di salvarli, prima che
l'importazione (US-1309) li scriva.

**Non-Goals:** l'importazione; altri dati da completare oltre a nome e partita IVA (vedi proposal.md).

## Decisions

### D1. I segnaposto in un solo posto

Una classe del dominio (`CompanyPlaceholders`) tiene la partita IVA «00000000000» e il prefisso del nome «Azienda
senza nome n. ». Il metodo `name(numero)` costruisce il nome e altri due metodi riconoscono i segnaposto. Le stesse
costanti servono:
- alla validazione;
- alla ricerca;
- ai documenti;
- all'importazione di US-1309.

### D2. Riconoscere stretto, rifiutare largo

- **Riconosciuta da completare**: la partita IVA è esattamente «00000000000», oppure il nome comincia esattamente
  con «Azienda senza nome n. », come lo scrive l'importazione.
- **Rifiutata dal modulo**: la partita IVA «00000000000», oppure un nome che, tolti gli spazi iniziali, comincia con
  «azienda senza nome n.» senza distinguere maiuscole e minuscole.

L'insieme dei rifiutati contiene quello dei riconosciuti. Così un'azienda da completare non si può salvare così com'è,
e un'azienda salvata dal modulo non risulta mai da completare. Il confronto sul prefisso funziona uguale in Java e nella
query (`like`), senza espressioni regolari nel database.

### D3. Unicità con un indice parziale con lo stesso nome

V45 toglie il vincolo `uq_company_vat_code` e crea l'indice univoco `uq_company_vat_code` su `vat_code`, con la
condizione `WHERE vat_code <> '00000000000'`. PostgreSQL riporta il nome dell'indice come nome del vincolo violato:
- `DomainConstraintsIT` continua a riconoscerlo;
- il controllo dei duplicati nel servizio non cambia.

Il divieto di salvare «00000000000» non è un `CHECK` del database, perché l'importazione deve poterlo scrivere: è nel
programma (decisione del PO). La release precedente funziona con l'indice nuovo, perché non scrive mai il segnaposto
(A16).

Il servizio cerca i duplicati con `findByVatCode` solo dopo la validazione, che rifiuta il segnaposto: la ricerca non
trova mai più righe.

### D4. API

- **`CompanyDTO.missingData`**: elenco ordinato di `NAME` e `VAT_CODE`, calcolato dall'entità; vuoto se l'azienda è
  completa. I valori restano quelli salvati (il nome segnaposto è anche l'identificativo leggibile dell'azienda); è il
  frontend a non mostrare gli zeri.
- **`GET /companies?toComplete=true`**: solo le aziende con la partita IVA segnaposto o il nome segnaposto, con le
  costanti di D1 passate come parametri alla query.
- **Validazione**: un secondo `@Pattern` su `vatCode` e uno su `name`, con un'anticipazione negativa costruita dalle
  costanti di D1. Bean Validation restituisce gli errori di tutti i campi insieme, come gli altri errori del modulo.
  - Su `vatCode`: «Inserisci la partita IVA vera: «00000000000» vuol dire che manca».
  - Su `name`: «Inserisci la ragione sociale vera: «Azienda senza nome n. …» vuol dire che manca».

### D5. Frontend

- **Elenco**:
  - «Da completare» è un'etichetta di testo nella cella del dato che manca: al posto degli zeri nella colonna della
    partita IVA, accanto al nome segnaposto nella colonna della ragione sociale;
  - la casella «Solo le aziende da completare» funziona come «Mostra anche le aziende disattivate» e resta
    nell'indirizzo (`daCompletare=1`).
- **Scheda**:
  - sotto il titolo, la riga della partita IVA dice «P.IVA da completare»;
  - un avviso elenca i dati che mancano;
  - il modulo apre vuoti i campi con il segnaposto e ripete la validazione del server con gli stessi messaggi.
- **Dettaglio dell'abbinamento**: «Da completare» al posto della partita IVA segnaposto.
- **Codice**: un piccolo modulo (`companyCompletion`) riconosce i dati mancanti da `missingData` e non dai valori, così
  la regola resta nel server.

### D6. Documenti

`DocumentValues` mette `null` al posto di `PARTITA_IVA` e `RAGIONE_SOCIALE` quando sono segnaposto: diventano la riga
da completare come ogni dato mancante. Il nome segnaposto viene tolto anche dalla ragione sociale, perché
«Azienda senza nome n. 1234» in una convenzione sarebbe sbagliato quanto gli zeri.

## Risks / Trade-offs

- **[Ricerca per partita IVA meno veloce]**: con la condizione, l'indice parziale non serve alle ricerche per
  partita IVA con un parametro → sono circa 14.000 aziende, la lettura sequenziale basta. La ricerca dell'elenco usa
  già `like` sul nome.
- **[Un nome vero che comincia con «Azienda senza nome n.»]** → improbabile; il messaggio spiega perché è rifiutato.
