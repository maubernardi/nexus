## Why

Storia **US-605** (Sprint 15, PO 15/11). Oggi una mansione abbinata è BLOCCATA da una sola segnalazione fino al
rilascio. Un'azienda però può offrire lo stesso posto per più tipi di tirocinio: la mansione «Cameriere» dell'azienda
Alfa può avere un tirocinio Extracurriculare e uno Di inclusione. Con il blocco unico il Call Center perde il secondo
posto, oppure deve inventare una mansione doppia. Il PO vuole che la stessa mansione si proponga a più segnalazioni,
**una per tipo di tirocinio**, sapendo sempre se è già assegnata.

Il tipo di tirocinio oggi si sceglie solo nella scheda del tirocinio, dopo l'appuntamento. Serve invece dalla
segnalazione: lo indica il Tutor quando la crea.

## What Changes

- **Tipo di tirocinio sulla segnalazione**:
  - il Tutor lo indica quando crea la segnalazione, anche dalla bacheca;
  - è obbligatorio e si sceglie tra i tipi attivi ammessi dal progetto (US-115);
  - «Di inclusione» si sceglie solo se il beneficiario ha l'ente e il case manager (US-206).
- **Correzione del tipo** da parte del Tutor, dalla segnalazione o dalla scheda, fino all'avvio del tirocinio. Si
  può scegliere solo un tipo che sulla mansione abbinata non ha già un'altra segnalazione aperta.
- **Scheda del tirocinio**: parte dal tipo della segnalazione, e i due restano uguali.
- **Segnalazioni già aperte** (V49): prendono il tipo dalla scheda, se c'è. Altrimenti il Call Center vede «tipo non
  indicato» e non può proporre finché il Tutor non lo indica.
- **Assegnazione della mansione per tipo**:
  - una mansione può essere assegnata a più segnalazioni, al massimo una per tipo di tirocinio (vincolo nel database);
  - la proposta di una mansione che ha già una segnalazione dello stesso tipo è rifiutata con `409`;
  - lo stato BLOCCATA della mansione vuol dire «assegnata ad almeno una segnalazione».
- **Avviso nell'abbinamento**: una mansione già assegnata a una segnalazione di un altro tipo resta tra quelle
  proponibili, con l'avviso ben visibile «Già assegnata alla segnalazione n. … (tipo)». L'avviso compare nell'elenco,
  nei dettagli e nella conferma della proposta.
- **Rilasci**: rifiuto della proposta, scadenza del timer, appuntamento fallito e report di restituzione liberano la
  mansione solo per quella segnalazione.
- **Mansioni dell'azienda**: lo stato elenca tutte le segnalazioni a cui la mansione è assegnata, con il tipo.
  Ritirare la mansione e disattivare l'azienda restano vietati finché c'è un'assegnazione, e il messaggio le elenca
  tutte.
- **Bacheca**: resta com'è. L'annuncio si pubblica solo per una mansione non assegnata e si archivia alla prima
  assegnazione.

## Non-goals

- **Più assegnazioni dello stesso tipo** sulla stessa mansione: il PO vuole al massimo una per tipo.
- **Il Call Center che indica o corregge il tipo**: è del Tutor, come la scheda.
- **Un numero di posti per mansione**: la regola è per tipo di tirocinio.
- **Cambiare la bacheca**.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `ticket-submission`: tipo di tirocinio della segnalazione, alla creazione e in correzione.
- `job-matching`: mansioni compatibili e proposta per tipo di tirocinio, avviso delle altre assegnazioni, rilascio
  per una sola segnalazione.
- `domain-model`: assegnazione della mansione al posto del blocco unico.
- `internship-record`: tipo della scheda uguale a quello della segnalazione.

## Impact

- Migrazione **V49**, solo additiva:
  - `ticket.internship_type_id`;
  - `ticket.job_slot_held`;
  - indice univoco parziale per mansione e tipo;
  - dati: le assegnazioni di oggi e i tipi dalle schede.
- `job_slot.status` e `job_slot.blocked_by_ticket_id` restano, aggiornati come riassunto: la release precedente
  (A16) li legge come oggi.
- Backend:
  - creazione e correzione del tipo;
  - proposta e rilasci;
  - mansioni compatibili con le assegnazioni;
  - `GET /reference/internship-types?projectId=…`.
- Frontend:
  - moduli di segnalazione e candidatura;
  - tipo nella segnalazione, con la correzione del Tutor;
  - avvisi nell'abbinamento;
  - stato delle mansioni dell'azienda.
- Accessibilità: l'avviso è testo con un'icona decorativa, non solo colore. Nella conferma della proposta è collegato
  al pulsante con `aria-describedby`. Test con axe.
