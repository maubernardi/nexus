## Decisions

### D1 — Una scheda per segnalazione, in tabelle proprie
`internship` è legata alla segnalazione: uno a uno, `uq_internship_ticket`. L'azienda è quella della mansione abbinata
alla segnalazione e non si copia. I tutor aziendali e i periodi di sospensione sono righe figlie. I dati del
beneficiario stanno solo qui, non nell'anagrafica: è la minimizzazione chiesta da US-1001. Rientreranno
nell'anonimizzazione (US-1303).

### D2 — Solo il Tutor della segnalazione
Scheda e documenti sono competenza del Tutor (PO, 29/10). Le API sono del ruolo TUTOR, con il controllo che la
segnalazione sia la sua:
- un Tutor dello stesso progetto riceve `403`;
- il Call Center e l'ADMIN non hanno accesso, perché la scheda contiene dati personali che non servono loro.

La scheda c'è dall'appuntamento fissato in poi (`APPUNTAMENTO`, `IN_TIROCINIO`, `FORM_RESTITUZIONE`). In questi
stati il Tutor la legge e la modifica in qualunque momento, anche a tirocinio avviato: lo ha deciso il PO il 29/10,
correggendo la prima versione, che la bloccava all'avvio. Lo stesso vale per legale rappresentante e ATECO aggiunti
dalla scheda. Prima dell'appuntamento fissato, o se la segnalazione è riaperta, la risposta è `409`.

### D3 — Salvataggi parziali
Ogni campo è facoltativo: la scheda si compila un po' alla volta. Si controlla solo ciò che c'è:
- la fine prevista non precede l'inizio;
- ore settimanali da 1 a 40, valore segnaposto da confermare con i moduli (Q5);
- codice fiscale valido, carattere di controllo compreso. Se c'è anche la data di nascita, il codice deve
  corrispondere: anno, mese e giorno, anche per le donne (+40) e con l'omocodia;
- tipo di tirocinio attivo, oppure quello già scelto;
- codice ATECO dell'azienda della segnalazione;
- ogni tutor aziendale ha il nome;
- ogni sospensione ha inizio e fine, nell'ordine giusto, senza sovrapposizioni e dentro il periodo del tirocinio quando
  le date sono note.

La completezza la verificherà l'avvio del tirocinio (US-1003). Ogni salvataggio porta la versione letta: se nel
frattempo la scheda è cambiata, la risposta è `409`.

### D4 — Righe figlie sostituite in blocco
A ogni salvataggio le righe di tutor e sospensioni si cancellano e si reinseriscono, con un `flush` in mezzo.
Hibernate esegue gli inserimenti prima delle cancellazioni: senza `flush`, spostare un periodo violerebbe il vincolo di
esclusione con la riga vecchia. La versione della scheda si incrementa anche quando cambiano solo le righe figlie.

### D5 — Il vincolo di non sovrapposizione senza estensioni
Vincolo `EXCLUDE USING gist` su `int8range(internship_id, internship_id, '[]') WITH =` e
`daterange(date_from, date_to, '[]') WITH &&`. Gli operatori sui range sono nativi in GiST, quindi `btree_gist` non
serve. In produzione Flyway gira come `nexus_owner`, che non è superutente: un'estensione sarebbe stata un rischio
in più.

### D6 — Dati dell'azienda: si aggiungono, non si correggono
Legale rappresentante e codici ATECO restano dell'azienda (US-506): si riusa `CompanyLegalDataService` e l'audit
`COMPANY` registra il Tutor come autore. Dalla scheda il Tutor:
- inserisce il legale rappresentante solo se manca. Se c'è già, la risposta è `409`: "per correggerlo rivolgiti al
  Call Center" (PO, 29/10);
- aggiunge un codice ATECO, che va nella scheda dell'azienda.

La scheda punta al codice scelto (`fk_internship_ateco_code`). Per questo il Call Center non può togliere dall'azienda
un codice usato in una scheda: la risposta è `409` con il numero della segnalazione.

### D7 — Appuntamento fallito dopo la compilazione
`APPOINTMENT_FAILED` parte da `APPUNTAMENTO`: la segnalazione torna al Call Center e potrà andare a un'altra azienda.
- Tipo, date, ore, sede, ATECO, tutor aziendali, sospensioni e note riguardavano quell'azienda: si azzerano.
- Restano i dati del beneficiario, così il Tutor non li riscrive.

L'audit dell'evento lo registra (`internshipReset`).

### D8 — Audit e cronologia
Ogni salvataggio con modifiche scrive `INTERNSHIP_SAVE` sulla segnalazione. Si registrano:
- con i valori: tipo, date, ore, sede, codice ATECO, periodi di sospensione;
- solo come "cambiato": codice fiscale, nascita, residenza, tutor aziendali (persone) e note (testo libero).

Nella cronologia la riga è visibile anche al Call Center ("Scheda del tirocinio aggiornata"), senza dati.

### D9 — Una pagina dedicata
Il modulo è lungo, perciò ha una pagina sua (`/segnalazioni/:id/tirocinio`), con un solo `h1`; la pagina della
segnalazione la collega per il Tutor. Legale rappresentante e ATECO si salvano subito, con moduli propri sopra il
modulo della scheda: i moduli annidati non sono validi in HTML. I loro moduli sono quelli di US-506, resi riusabili.
