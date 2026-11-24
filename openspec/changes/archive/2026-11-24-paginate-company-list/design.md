## Context

- `GET /api/v1/companies` restituiva tutte le aziende che rispondono ai filtri (ricerca, disattivate, da completare,
  categoria), già filtrate dal server, con comune, categoria e referenti (`@BatchSize`).
- Dal passaggio in produzione (24/11) le aziende sono 13.799: la risposta pesa diversi MB e la tabella ha 13.799 righe.
- L'elenco della PWA è l'unico client dell'endpoint.

## Goals / Non-Goals

**Goals:** elenco veloce con qualunque numero di aziende; stessi filtri e stesso ordine; 10, 25, 50 o 100 aziende per
pagina, scelte dall'operatore.

**Non-Goals:**
- scorrimento infinito o «carica altre»;
- ordinamento per colonna;
- ricordare la scelta tra una visita e l'altra, oltre l'indirizzo.

## Decisions

### D1. Paginazione nel server, con il totale
`Pageable` di Spring Data sulla query esistente, che tiene il suo `order by lower(name), id`: l'ordine è stabile tra le
pagine. La query di conteggio è scritta a parte, senza ordinamento né caricamenti. La risposta è
`CompanyPageDTO(companies, total, page, size)`, con `page` da 0.

Alternativa scartata: paginare nel browser. Non risolve il peso della risposta.

### D2. Dimensioni ammesse
Solo 10, 25, 50 o 100 (`CompanyService.PAGE_SIZES`); 25 se manca. Un'altra dimensione o una pagina negativa danno
`400`: nessuno può chiedere tutte le aziende insieme. Una pagina oltre l'ultima risponde vuota, con il totale.

### D3. Pagina nell'indirizzo
`?pagina=3&perPagina=50`, da 1 per chi legge l'indirizzo; i valori predefiniti restano fuori. Un valore non valido vale
la prima pagina da 25. Un filtro nuovo toglie la pagina e tiene le aziende per pagina. Se la pagina è oltre l'ultima,
per esempio un link vecchio, si passa all'ultima senza una voce in più nella cronologia.

### D4. Molecola `Pagination`
Menu delle aziende per pagina con etichetta visibile, e una `nav` con nome («Pagine dell'elenco delle aziende»). Dentro:
Prima, Precedente, «Pagina X di Y», Successiva, Ultima. I testi sono visibili, non solo icone; i pulsanti che non
portano altrove sono disattivati. Sta sotto la tabella e compare solo con più di 10 aziende.

Dopo un cambio di pagina il focus va alla regione della tabella: si riparte a leggere dall'inizio, e lo stato sopra la
tabella annuncia l'intervallo nuovo.

## Risks / Trade-offs

- [La ricerca `like '%…%'` legge tutta la tabella] → con circa 14.000 righe bastano pochi millisecondi; il costo era il
  trasferimento e il disegno, non la query.
- [Un'azienda disattivata mentre si sfoglia sposta le altre di una posizione] → accettabile per un elenco di lavoro.
