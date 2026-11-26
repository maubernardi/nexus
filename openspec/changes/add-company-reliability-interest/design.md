## Context

- L'azienda (`company`) si crea e si modifica con un solo modulo (`POST`/`PUT /api/v1/companies`), con il blocco
  ottimistico (`version`) e l'audit `CREATE`/`UPDATE` con prima e dopo. L'elenco è a pagine (US-513), con i filtri
  nell'indirizzo.
- I nomi importati il 24/11 (US-1309) contengono ancora i simboli del database attuale: 343 su 13.799. Dai conteggi sul
  file Access:
  - con le stelle: 143 con un `$`, 107 con due, 20 con tre, una con cinque;
  - con i pollici: 89 con una `£`, una con due;
  - con un refuso all'inizio: 4 `&` e un `^`;
  - simboli mescolati (`£$$`, `$$£`) e a volte separati da uno spazio; nessun nome resta vuoto.
- L'importazione è ripetibile e verrà rifatta quando si azzera la pre-produzione.

## Goals / Non-Goals

**Goals:**
- Una sola regola per la conversione, applicata nello stesso modo dalla migrazione e dall'importazione, e verificata
  dagli stessi esempi nei test.
- Nessun dato perso: la conversione tocca solo i nomi con i simboli all'inizio e le aziende senza valutazioni.

**Non-Goals:**
- Un endpoint a parte per le valutazioni.
- Stelle e pollici per il Tutor (US-511).

## Decisions

1. **Due colonne `smallint` facoltative**:
   - `reliability`, con `ck_company_reliability` (da 1 a 4);
   - `project_interest`, con `ck_company_project_interest` (da 1 a 2).

   `NULL` vuol dire «non valutata», non zero: il filtro «almeno N» e il trattino nell'elenco vengono da soli.
   *Alternativa scartata:* zero per «non valutata». Avrebbe due modi di dire la stessa cosa.

2. **Conversione in SQL nella V52, la stessa regola in Java per l'importazione.** La V52:
   - conta `$` e `£` nel prefisso fatto di `[[:space:]$£&^]`;
   - scrive le valutazioni solo se l'azienda non ne ha già;
   - toglie il prefisso;
   - mette il vincolo sul nome solo dopo la conversione.

   L'importazione usa una piccola classe Java (`LegacyCompanyMarks`), che pulisce il nome prima dell'insert.
   *Alternativa scartata:* una funzione SQL condivisa, chiamata anche dall'importazione dopo l'insert. Il vincolo sul
   nome impedisce di inserire prima il nome grezzo. I casi d'esempio della spec stanno sia nel test della migrazione
   sia nel test della classe Java.

3. **Nome che resterebbe vuoto → segnaposto** «Azienda senza nome n. ‹numero›». Per le aziende importate il numero si
   ricava dall'id, che è la base delle aziende importate più il numero di Access; per le altre è l'id. Non succede con i
   dati di oggi, ma la migrazione non deve fallire in produzione.

4. **Vincolo sul nome**:
   - nel database, `ck_company_name_legacy_marks CHECK (name !~ '^[[:space:]]*[$£&^]')`;
   - nell'API, un `@Pattern` su `CompanyFormDTO.name`, con un messaggio che indica stelle e pollici;
   - nel modulo, lo stesso controllo, per l'errore prima dell'invio.

   I nomi sono già salvati senza spazi intorno: lo spazio nella regola serve solo a non lasciare scappatoie.

5. **Valutazioni nel modulo dell'azienda, salvate con «Salva»**, insieme al resto. Riusano blocco ottimistico,
   validazione e audit (`reliability`, `projectInterest`, con il valore in chiaro: non sono dati personali).
   *Alternativa scartata:* salvataggio immediato al clic, con un endpoint dedicato. Avrebbe due modi di salvare la
   stessa scheda e un `409` in più da gestire.

6. **Componente `RatingInput` (molecola), riusabile da US-1104**:
   - un `fieldset` con la legenda e N pulsanti di opzione nativi, nascosti alla vista; le icone fanno da etichetta;
   - le frecce e il lettore di schermo funzionano già con i pulsanti nativi;
   - ogni opzione ha il nome «N stelle su 4»;
   - l'icona scelta e le precedenti sono piene, le altre vuote;
   - un pulsante «Togli» compare quando c'è un valore.

   Accanto, `RatingDisplay` (atomo) mostra il valore nell'elenco, con il testo per il lettore di schermo. Icone lucide
   `Star` e `ThumbsUp`: piene con `fill`, vuote con il solo contorno. Colori dai token del tema, con contrasto di
   almeno 3:1 nei due temi.

7. **Filtri `minReliability` (1–4) e `minProjectInterest` (1–2)** su `GET /api/v1/companies`. Fuori intervallo la
   risposta è `400`; si sommano agli altri filtri. Nell'indirizzo: `stelle=N` e `interesse=N`.

8. **Dati dimostrativi**: un `UPDATE` nel seed dà stelle e pollici a tre aziende dimostrative, solo se non hanno
   valutazioni. È idempotente e non tocca le valutazioni scelte a mano.

## Risks / Trade-offs

- [La regola SQL e quella Java potrebbero divergere] → stessi esempi nei due test, presi dalla spec.
- [Ripristino della release precedente dopo la V52] → le colonne nuove sono facoltative e la release precedente le
  ignora. Un nome con `$` all'inizio verrebbe rifiutato dal database con un errore generico. È un caso raro, e il
  ripristino si fa solo in emergenza.
- [Encoding della `£`, un carattere non ASCII] → il database è UTF-8. Il test della migrazione usa la `£` vera, e la
  prova in locale sul file Access confronta i conteggi.

## Migration Plan

1. Il deploy applica la V52: aggiunge le colonne, converte i nomi e mette i vincoli, in una transazione.
2. Controllo in pre-produzione, solo con i conteggi dell'elenco:
   - «Almeno 1 stella» dà circa 271 aziende, «Almeno 3 stelle» circa 21, «Due pollici» circa 1;
   - la ricerca di `$` non trova più nomi che iniziano con il simbolo.
3. Per tornare indietro: deploy del commit precedente. Le colonne restano, i nomi restano ripuliti.
