## Context

- La convenzione iniziale (US-1008) è nella riga di `internship`: numero, data e data di stipula, con il periodo che va
  da `start_date` a `expected_end_date`. I progetti formativi sono righe di `internship_training_project`, numerate
  nell'ordine delle date e sostituite in blocco quando la scheda le cambia.
- La scheda si salva tutta insieme, con una versione per i conflitti. Si corregge in qualunque stato con la scheda.
- Il report di restituzione confronta la fine effettiva con la fine prevista (US-1103).
- Se il deploy fallisce, la release precedente riparte sullo stesso database con `ddl-auto=validate` (A16).

## Goals / Non-Goals

**Goals:** proroghe come convenzioni, ognuna con i suoi progetti, e la fine prevista che le comprende.

**Non-Goals:** i documenti delle proroghe (US-1011).

## Decisions

### D1 — La convenzione iniziale resta dov'è, le proroghe in una tabella nuova
Tabella `internship_extension` (`agreement_number` 50, `agreement_date`, `agreement_signed_date`, `date_from`,
`date_to`, tutti obbligatori), con questi vincoli:
- `ck_..._dates` e `ck_..._agreement_number`;
- l'esclusione `ex_internship_extension_overlap`, sul modello delle sospensioni.

La convenzione iniziale continua a vivere sulla scheda, con il suo periodo `start_date`–`expected_end_date`.
*Alternativa scartata:* spostare tutte le convenzioni in una tabella sola. Bisognerebbe riscrivere i dati di US-1008
appena pubblicati, e la release precedente (A16) leggerebbe colonne non più aggiornate.

### D2 — Progetti con la loro convenzione
`internship_training_project.extension_id` è facoltativo: vuoto vuol dire convenzione iniziale. Il numero resta la
posizione nell'ordine delle date, ma dentro la stessa convenzione. Il vincolo di esclusione dei progetti resta sul
tirocinio intero, che va bene perché le convenzioni non si sovrappongono.

### D3 — Tutto nel salvataggio della scheda
Le proroghe arrivano nel modulo come elenco `extensions`, ognuna con i suoi `trainingProjects`, e si controllano
insieme alle date del tirocinio. Se proroghe o progetti cambiano, il servizio li sostituisce tutti insieme: cancella
prima i progetti e poi le proroghe, con un flush, e li reinserisce nell'ordine inverso.

### D4 — Quando si registrano
Le proroghe cambiano solo con la segnalazione `IN_TIROCINIO`; `InternshipDTO.extensible` lo dice al modulo. Negli altri
stati un elenco diverso da quello salvato è rifiutato sul campo `extensions`, mentre la scheda resta modificabile come
oggi.

### D5 — Controlli sulle date
Gli errori vanno sul campo (`extensions[i].from` ecc.):
- la fine della proroga non precede l'inizio;
- l'inizio è dopo la fine della convenzione precedente, cioè la fine prevista iniziale per la prima proroga e la fine
  della proroga precedente per le altre;
- i progetti stanno dentro le date della loro proroga e ognuno inizia dopo la fine del precedente.

Con le proroghe, la fine prevista iniziale deve precedere l'inizio della prima proroga. L'errore va su
`expectedEndDate`, perché è lì che il Tutor l'ha cambiata.

I periodi di sospensione possono arrivare fino alla fine dell'ultima proroga.

### D6 — Fine prevista e monte ore
`Internship.currentEndDate()` è la fine dell'ultima proroga, oppure la fine prevista iniziale. Va in
`InternshipDTO.currentEndDate` e il report di restituzione la usa al posto di `expectedEndDate` (US-1103).

Il monte ore del tirocinio è la somma dei monte ore dei periodi delle convenzioni, con la stessa griglia, le stesse
sospensioni e le stesse festività. I giorni tra due convenzioni non contano (PO, 15/11).

### D7 — Audit
- `extensions` con i valori, per esempio «1 2027/200 2027-06-15/2027-09-30 (data 2027-06-01, stipula 2027-06-10)»;
- `extensionTrainingProjects`, per esempio «1.1 2027-06-15/2027-09-30 Cuoco»;
- il dettaglio della mansione di tutti i progetti, solo come «cambiato».

### D8 — Interfaccia
La sezione «Proroghe» viene dopo i progetti formativi, e c'è solo se il tirocinio è avviato o ha già proroghe.
- Ogni proroga è un gruppo «Proroga N» (`h3`), con:
  - i dati della convenzione e le date;
  - i progetti formativi, «Proroga N, progetto formativo M» (`h4`), con le stesse righe della convenzione iniziale;
  - il pulsante «Togli la proroga N».
- «Aggiungi proroga» propone come inizio il giorno dopo la fine prevista attuale; il primo progetto di una proroga
  propone la mansione dell'ultimo progetto della convenzione precedente.
- La convenzione iniziale mostra anche la fine prevista con le proroghe.
- I campi delle proroghe hanno etichette complete (es. «Proroga 1: numero della convenzione»), così si capiscono anche
  nel riepilogo degli errori.

## Risks / Trade-offs

- [La scheda si allunga ancora] → le proroghe compaiono solo a tirocinio avviato; la revisione UX/UI (US-1305) valuterà
  come dividere la pagina.
- [Ripristino della release precedente (A16)] → la release precedente ignora tabella e colonna nuove. Se però salva la
  scheda, sostituisce i progetti senza conoscere `extension_id`. Nella breve finestra di un ripristino conviene non
  salvare schede con proroghe.

## Migration Plan

Deploy normale: V42 crea la tabella vuota e una colonna facoltativa. Le schede attuali non hanno proroghe.
