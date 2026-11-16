## Context

Motivazione: vedi `proposal.md`.

`DocxTemplate` (US-1004) cerca i segnaposto `{NOME}` nel testo intero di ogni paragrafo, perché Word li spezza in più
run. Quando ne trova uno, riscrive il paragrafo in un solo run con la formattazione del primo. Con i modelli generici
andava bene: le righe con segnaposto avevano una sola formattazione. I modelli reali di GOL no:
- nella convenzione la riga del promotore ha «soggetto promotore» in grassetto;
- molte righe cominciano con una tabulazione;
- i giorni dell'orario sono caselle disegnate con simboli (`w:sym`), in mezzo a nomi in grassetto;
- i piè di pagina hanno il numero di pagina come campo.

`DocumentPlaceholders` (US-1011) ha 120 segnaposto; la tipologia dell'ente (`CareAgencyKind`, US-115) non ne ha uno.

## Goals / Non-Goals

**Goals:**
- Documenti compilati con la stessa impaginazione del modello caricato.
- Un modello di Allegato B per tutti gli enti.
- I 6 modelli di GOL pronti da caricare, fuori dal repository.

**Non-Goals:** vedi `proposal.md`.

## Decisions

### D1 — Sostituzione nei soli nodi di testo

Il motore raccoglie i nodi `w:t` dei run del paragrafo, cerca i segnaposto nel loro testo concatenato e cambia solo i
nodi che li contengono:
- il valore va nel nodo in cui comincia il segnaposto, che quindi ne prende la formattazione;
- i nodi successivi del segnaposto perdono la loro parte;
- le sostituzioni vanno dall'ultima alla prima, così le posizioni delle precedenti non cambiano;
- un `\n` nel valore diventa `w:br` + `w:t` nello stesso run (come prima, ma senza toccare gli altri run).

I segnaposto senza valore restano come sono (come prima). Il controllo dei segnaposto al caricamento non cambia: usa
il testo del paragrafo.

Alternativa scartata: chiedere modelli con ogni segnaposto in un run solo e una sola formattazione per riga. Chi
modifica un modello in Word non controlla i run, e le righe reali mescolano formattazioni.

### D2 — Tipologia dell'ente: un segnaposto a casella per tipologia

`{TIPO_ENTE_<TIPOLOGIA>}` per ogni valore di `CareAgencyKind`, come `SESSO_M`/`SESSO_F` e `GIORNO_*`: `☒` o `☐`. I nomi
vengono dall'enumerazione, così una tipologia nuova ha subito il suo segnaposto. Senza ente (Extracurriculare) sono
tutte `☐`.

### D3 — Modelli preparati da uno script, fuori dal repository

Lo script `documenti_base/modelli-nexus/prepara_modelli.py` parte dai file della cooperativa e scrive i 6 modelli:
- sostituisce il testo come il motore (D1), quindi la formattazione originale resta;
- toglie l'evidenziatore giallo con cui il PO ha segnato i campi;
- si ferma se un testo cercato non c'è o compare più volte, così un file di partenza cambiato non passa in silenzio.

Lo script contiene i testi originali da sostituire, compresi dati della cooperativa e degli enti: per questo non entra
nel repository.

Scelte sui contenuti (vedi anche la proposta):
- progetto formativo e Allegato B usano date, mesi, ore e mansione del progetto formativo del documento (US-1011);
- l'Allegato A riporta luogo e data di nascita del rappresentante dell'ente, come i modelli delle SdS;
- «Nominativo responsabile dell'Ente per la Convenzione» = referente dell'ente, con il suo telefono e la sua mail;
- relazione finale Extracurriculare: motivo di chiusura e descrizione dell'interruzione in testa alla «Valutazione
  complessiva»; Allegato C: luogo e data = sede di competenza e chiusura (PO, 09/11);
- nella griglia delle fasce le celle dei giorni perdono il rientro destro dello stile e vanno a 9 punti, così
  «09:00–13:00» sta su una riga.

## Risks / Trade-offs

- [Un valore prende la formattazione di dove comincia il segnaposto] → nei modelli il segnaposto si scrive con la
  formattazione voluta, come qualunque testo.
- [Word rifiuta un file con elementi fuori ordine] → lo script inserisce le proprietà nell'ordine dello schema e il
  controllo finale lo verifica; T-24.5 apre i documenti in Word.
- [I modelli fuori dal repository possono andare persi] → lo script li rigenera dai file della cooperativa; l'ADMIN li
  scarica anche da «Modelli dei documenti».
