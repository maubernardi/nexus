## Context

- La scheda del tirocinio (US-1001) è una riga di `internship` per segnalazione, più alcune righe figlie (tutor
  aziendali, sospensioni, griglia oraria). Le righe figlie arrivano nel modulo come elenchi: a ogni salvataggio il
  servizio le sostituisce in blocco se sono cambiate, cancellando le vecchie prima delle nuove (per i vincoli di
  esclusione). La scheda ha una sola versione per i conflitti.
- Le sospensioni hanno già la stessa regola di date: dentro il tirocinio, senza sovrapposizioni, con un vincolo di
  esclusione GiST che non richiede `btree_gist`.
- Il monte ore (`InternshipHours`) conta i giorni della griglia tra due date, saltando sospensioni e festività.
- Se il deploy fallisce, la release precedente riparte sullo stesso database con `ddl-auto=validate` (A16).

## Goals / Non-Goals

**Goals:**
- Convenzione e progetti formativi nella scheda, con i controlli sulle date, la durata e il monte ore, pronti per i
  documenti di US-1011.

**Non-Goals:**
- Documenti nuovi e requisiti per l'avvio; vedi la proposta.

## Decisions

### D1 — La convenzione sta nella scheda
La relazione è 1:1, quindi bastano tre colonne su `internship`: `agreement_number varchar(50)`, `agreement_date`,
`agreement_signed_date`, tutte facoltative, con `ck_internship_agreement_number` contro il testo di soli spazi. Non c'è un
vincolo tra le due date: il criterio non lo chiede.
*Alternativa scartata:* una tabella `agreement` a parte, che aggiungerebbe un join senza un dato in più.

### D2 — I progetti sono righe figlie della scheda, in un unico salvataggio
Tabella `internship_training_project` (`date_from`, `date_to`, `job_title` 200, `job_details` 2000), con queste
caratteristiche:
- arriva nel modulo come elenco `trainingProjects` ed è sostituita in blocco come le sospensioni;
- vincoli `ck_..._dates`, `ck_..._job_title`, `ck_..._job_details`;
- vincolo di esclusione `ex_internship_training_project_overlap`, sul modello delle sospensioni.

Così date del tirocinio e progetti si controllano insieme. Per allungare il tirocinio e aggiungere il progetto 2 basta
un salvataggio, e accorciare il tirocinio con un progetto ancora in corso dà l'errore sul progetto.
*Alternativa scartata:* un'API a parte per ogni progetto. Servirebbero più salvataggi in un ordine preciso, con una
versione separata dalla scheda.

### D3 — Numero = posizione nell'ordine delle date
Il numero non si salva: è la posizione del progetto nell'elenco, e l'elenco deve essere in ordine di date, perché ogni
progetto inizia **dopo** la fine del precedente. Nel modulo, quindi, il numero di una riga è anche il numero del
progetto, e il controllo «dopo la fine del precedente» copre sia l'ordine sia le sovrapposizioni.

Togliere un progetto rinumera quelli dopo. È voluto: un progetto tolto era un errore, e la numerazione resta 1, 2, 3
senza buchi.

### D4 — Controlli nel servizio, con l'errore sul campo
Gli errori seguono il modello delle sospensioni (`trainingProjects[i].from` / `.to`):
- fine prima dell'inizio;
- inizio prima dell'inizio del tirocinio;
- fine dopo la fine prevista;
- inizio non successivo alla fine del precedente: «Deve iniziare dopo la fine del progetto formativo 1 (28/02/2027)».

Con almeno un progetto servono le date del tirocinio; se mancano, l'errore va su `startDate` o `expectedEndDate`. Il
modulo ripete gli stessi controlli nel browser.

### D5 — Mesi e monte ore calcolati in uscita
`InternshipDTO.trainingProjects` porta, per ogni progetto salvato, `number`, `months` e `totalMinutes`.
- **Monte ore:** `InternshipHours.totalMinutes` con le date del progetto e la griglia, le sospensioni e le festività
  della scheda.
- **Mesi:** `InternshipMonths.rounded(dal, al)`. È il periodo dal primo giorno al giorno dopo l'ultimo, in mesi
  interi, più uno se restano 15 giorni o più. Le sospensioni sono comprese. La regola è quella di US-1011 e servirà
  anche lì.

Il modulo mostra i valori calcolati solo per i progetti che non sono cambiati dal salvataggio; per gli altri dice che
si calcolano al salvataggio, come il monte ore del tirocinio.

### D6 — Audit
Nella voce `INTERNSHIP_SAVE`:
- `agreementNumber`, `agreementDate` e `agreementSignedDate` si registrano con i valori;
- `trainingProjects` si registra come elenco «1 2026-12-01/2027-02-28 Aiuto cuoco», con prima e dopo;
- il dettaglio della mansione è testo libero e potrebbe descrivere adattamenti legati alla persona, quindi
  `trainingProjectDetails` si registra solo come «cambiato» (come le note).

Nessun campo per il motivo della riformulazione.

### D7 — Appuntamento fallito
`Internship.clearCompanyData()` svuota anche la convenzione e i progetti, e il controllo «c'è qualcosa da azzerare»
li considera.

L'anonimizzazione (US-1303) non li tocca:
- la convenzione non contiene dati del beneficiario;
- il dettaglio della mansione descrive il lavoro. Come per gli obiettivi del report di restituzione, resta fuori
  dall'audit ma non si cancella.

### D8 — Mansione proposta
`InternshipDTO.jobSlotTitle` è il titolo della mansione abbinata alla segnalazione. «Aggiungi progetto formativo» crea
una riga precompilata così:
- l'inizio è il giorno dopo la fine del progetto precedente (per il primo, l'inizio del tirocinio);
- la fine è la fine prevista del tirocinio;
- la mansione è quella del progetto precedente (per il primo, `jobSlotTitle`).

Sono solo proposte, che il Tutor cambia.

### D9 — Interfaccia
Le due sezioni nuove della scheda stanno dopo le sospensioni, in un componente a parte (`TrainingProjectsFields`), come
la griglia oraria.
- **Convenzione:** numero, data e data di stipula, più una riga che riporta la durata («dal … al …, come il
  tirocinio») e «per n. 1 tirocinio».
- **Progetti formativi:** ogni progetto è un gruppo con il titolo «Progetto formativo N» (`h3`). Contiene:
  - le date «Dal» e «Al», la mansione e il dettaglio (area di testo);
  - la durata e il monte ore calcolati;
  - il pulsante «Togli il progetto formativo N».

  Gli errori passano dal riepilogo, nell'ordine della pagina.

## Risks / Trade-offs

- [La scheda diventa più lunga] → sezioni con titoli e collegamenti dal riepilogo degli errori; la revisione UX/UI
  (US-1305) valuterà come dividerla.
- [Ripristino della release precedente (A16)] → la release precedente ignora tabella e colonne nuove. Un appuntamento
  fallito in quella finestra non azzererebbe i progetti; al salvataggio successivo il controllo delle date lo
  segnalerebbe.
- [Togliere un progetto rinumera i successivi] → voluto (D3); i documenti per progetto arrivano con US-1011, dopo.

## Migration Plan

Deploy normale: V39 aggiunge colonne vuote e una tabella nuova. In caso di ripristino la release precedente le ignora.
