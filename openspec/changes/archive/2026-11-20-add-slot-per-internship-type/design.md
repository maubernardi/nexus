## Context

Vedi proposal.md, «Why». Oggi:
- la mansione abbinata ha `status = BLOCCATA` e `blocked_by_ticket_id`, univoco, con il vincolo
  `ck_job_slot_blocked`;
- la segnalazione ha `job_slot_id`, che resta anche dopo il report di restituzione, per lo storico;
- `proposeSlot` blocca la mansione nella transizione `MATCH` (e nella corsia veloce dalla bacheca). Rifiuto, timer,
  appuntamento fallito e report la liberano con `release()`;
- il tipo di tirocinio c'è solo nella scheda (`internship.internship_type_id`), con i controlli di progetto (US-115) e
  di inclusione (US-206) in `InternshipServiceImpl`.

## Goals / Non-Goals

**Goals:** una mansione assegnata a più segnalazioni, una per tipo di tirocinio, con il vincolo nel database, gli
avvisi nell'abbinamento e il tipo indicato dal Tutor fin dalla segnalazione.

**Non-Goals:** vedi proposal.md.

## Decisions

### D1. Tipo di tirocinio sulla segnalazione (V49)

- `ticket.internship_type_id` (FK `fk_ticket_internship_type`): facoltativo nel database, perché le segnalazioni di
  prima possono non averlo. È obbligatorio per le segnalazioni nuove.
- **Dati**: ogni segnalazione con la scheda prende il tipo della scheda.
- **Regole in un posto solo** (`InternshipTypeRules`, estratte da `InternshipServiceImpl`):
  - tipo attivo del catalogo, oppure quello che la segnalazione ha già;
  - ammesso dal progetto, salvo quello già scelto;
  - «Di inclusione» solo con ente e case manager completi.
  Valgono per la creazione, la correzione e la scheda.
- **Creazione**: `TicketCreateDTO` e `TicketSpecialCreateDTO` hanno `internshipTypeId` obbligatorio. Gli errori sono
  sul campo.
- **Tipi proponibili** nei moduli: `GET /reference/internship-types?projectId=…` dà i tipi attivi ammessi dal
  progetto. Senza `projectId` resta com'è.

### D2. Assegnazione (V49)

- `ticket.job_slot_held` (booleano): la segnalazione occupa la mansione `job_slot_id` per il suo tipo.
- Vincoli:
  - `ck_ticket_job_slot_held`: l'assegnazione richiede la mansione;
  - **`uq_ticket_job_slot_internship_type`**: indice univoco parziale su (`job_slot_id`,
    `coalesce(internship_type_id, 0)`) con `job_slot_held`. Al massimo una segnalazione per tipo; le segnalazioni
    senza tipo contano come un tipo a sé.
- **Dati**: diventa assegnata ogni segnalazione che oggi blocca una mansione (`blocked_by_ticket_id`).
- **Riassunto sulla mansione**: `status` è `BLOCCATA` se c'è almeno un'assegnazione. `blocked_by_ticket_id` punta
  alla segnalazione assegnata con il numero più basso. Lo aggiorna un metodo solo (`JobSlotAssignments.sync`) dopo
  ogni cambio.
  - Così `ck_job_slot_blocked` e `ck_job_slot_active_free` restano veri.
  - La bacheca e i controlli di ritiro e disattivazione funzionano come oggi.
  - La release precedente (A16) legge la mansione come oggi.
- `Ticket` ha `internshipType` e `jobSlotHeld`; `JobSlot` tiene i metodi di riassunto, non più `blockFor`.

### D3. Proposta

`proposeSlot` controlla, in quest'ordine, e risponde `409` con il messaggio che dice cosa fare:
1. la segnalazione ha il tipo: «La segnalazione n. N non ha il tipo di tirocinio: lo indica il Tutor»;
2. la mansione è a disposizione e l'azienda è attiva;
3. l'azienda non è esclusa per la segnalazione;
4. sulla mansione non c'è un'altra segnalazione dello stesso tipo né senza tipo: «La mansione «X» è già assegnata alla
   segnalazione n. N (tipo)».

Poi assegna, aggiorna il riassunto e incrementa la versione della mansione (`PESSIMISTIC_FORCE_INCREMENT`):
- due proposte contemporanee della stessa mansione si mettono in fila;
- la seconda trova la versione cambiata e riceve il `409` di oggi;
- se una corsa sfuggisse, resta l'indice univoco (`409` generico).

Nell'audit della mansione l'azione resta `BLOCK`, con la segnalazione, il tipo e lo stato prima e dopo.

### D4. Rilasci

Un metodo solo libera l'assegnazione di una segnalazione, aggiorna il riassunto e scrive `RELEASE` nell'audit della
mansione:
- **rifiuto, timer, appuntamento fallito**: assegnazione tolta e `job_slot_id` azzerato, come oggi;
- **report con la mansione ancora disponibile**: assegnazione tolta, `job_slot_id` resta per lo storico;
- **report con la mansione non più disponibile**: l'assegnazione resta per quel tipo, come oggi resta il blocco. Gli
  altri tipi non cambiano. Se l'azienda non offre più il posto per nessun tipo, il Call Center ritira la mansione
  quando non ha altre assegnazioni.

### D5. Mansioni compatibili

- La ricerca esclude le mansioni con un'assegnazione dello stesso tipo della segnalazione o senza tipo. Se la
  segnalazione non ha il tipo, esclude ogni mansione assegnata.
- `JobSlotMatchDTO` ha `assignments`: numero della segnalazione e tipo, per le mansioni assegnate ad altri tipi. Le
  assegnazioni delle mansioni dell'elenco si leggono con una query sola.

### D6. Correzione del tipo

- `PUT /tickets/{id}/internship-type` (`{internshipTypeId, version}`), solo per il Tutor della segnalazione.
- Si può fino all'avvio, cioè negli stati prima di `IN_TIROCINIO`; altrimenti `409`.
- Regole di D1. Se la segnalazione è assegnata a una mansione, il tipo nuovo non deve averci un'altra segnalazione:
  `400` su `internshipTypeId` con il numero.
- Cambia anche il tipo della scheda, se c'è, e la versione della mansione.
- Audit della segnalazione `INTERNSHIP_TYPE` con il tipo prima e dopo, anche nella cronologia.
- **Scheda**:
  - una scheda nuova parte dal tipo della segnalazione;
  - salvarla con un altro tipo applica la stessa correzione;
  - dopo l'avvio un tipo diverso dà `400` sul campo, «Il tipo di tirocinio non si cambia dopo l'avvio»;
  - se la segnalazione ha il tipo, la scheda non lo può togliere.

### D7. Interfaccia del Tutor

- **Moduli di segnalazione e di candidatura dalla bacheca**: menu «Tipo di tirocinio» obbligatorio dopo il progetto,
  con i tipi del progetto. Senza progetto il menu è disattivato.
- **Segnalazione**:
  - riga «Tipo di tirocinio» nel dettaglio, con «Non indicato» se manca;
  - fino all'avvio, il pulsante «Cambia il tipo» (o «Indica il tipo») apre un piccolo modulo: menu, «Salva»,
    «Annulla»;
  - dopo il salvataggio il focus va all'esito.

### D8. Interfaccia del Call Center

- **Dettaglio della segnalazione**: riga «Tipo di tirocinio».
- **Senza tipo**: sopra le mansioni compatibili un avviso dice che il tipo lo indica il Tutor, e i pulsanti «Proponi»
  sono disattivati.
- **Avviso «Già assegnata alla segnalazione n. N (tipo)»**:
  - testo con un'icona decorativa, nel colore di avviso;
  - nella riga della mansione, nel dettaglio (`MatchDetailsDialog`) e nella conferma della proposta;
  - nella conferma è nella descrizione del dialogo, quindi si legge prima di confermare.

### D9. Mansioni dell'azienda

- `JobSlotDTO` ha `assignments` (numero e tipo) al posto di `blockedByTicketNumber`.
- La colonna dello stato dice «Assegnata – segnalazione n. 12 (Extracurriculare), n. 15 (Di inclusione)».
- I messaggi che vietano di ritirare la mansione o disattivare l'azienda elencano tutte le segnalazioni.

## Risks / Trade-offs

- **[Segnalazioni aperte senza tipo]**: finché il Tutor non lo indica non si propongono, e una loro assegnazione
  vecchia blocca la mansione per tutti i tipi → è il comportamento di oggi. Il Call Center vede «tipo non indicato» e
  sa a chi chiedere.
- **[Riassunto da tenere allineato]**: `status` e `blocked_by_ticket_id` derivano dalle assegnazioni.
  - Un metodo solo li aggiorna, e i test controllano assegnazione e rilascio.
  - I vincoli di V4 restano a guardia.
  - Si potranno togliere quando la release precedente non servirà più.
- **[Versione della mansione]**: ogni assegnazione la incrementa, e un operatore con l'elenco vecchio riceve
  «non è più disponibile» anche per un altro tipo → ricarica e la trova con l'avviso. Una mansione non si propone a
  due tipi nello stesso istante.

## Migration Plan

V49 aggiunge due colonne e un indice e copia i dati (tipi dalle schede, assegnazioni dai blocchi). In caso di
ripristino (A16) la release precedente ignora le colonne nuove e trova `status` e `blocked_by_ticket_id` coerenti.
Una mansione con due assegnazioni per lei è bloccata dalla prima: le assegnazioni restano e tornano con la release
nuova.
