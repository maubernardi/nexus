## Context

- Il report di restituzione (US-1101) è una riga di `closure_report` per segnalazione, scritta all'invio e mai più
  modificata: trigger su UPDATE, DELETE e TRUNCATE, entità `@Immutable`. L'invio è la transizione `CLOSE`, da
  `IN_TIROCINIO`, che richiede la scheda completa (con inizio e fine prevista).
- La scheda del tirocinio (US-1001) si corregge in qualunque momento, anche dopo la chiusura; i tutor aziendali sono
  righe di `internship_company_tutor` sostituite in blocco quando cambiano.
- Se il deploy fallisce, la release precedente riparte sullo stesso database con `ddl-auto=validate` (A16): lo schema
  nuovo deve restare compatibile con il codice vecchio.

## Goals / Non-Goals

**Goals:**
- Data di fine effettiva nel report di restituzione e ruolo dei tutor aziendali, pronti per i documenti di US-1011.

**Non-Goals:**
- Segnaposto nuovi e relazione finale (US-1011); vedi la proposta.

## Decisions

### D1 — La fine effettiva sta nel report di restituzione
Colonna `closure_report.actual_end_date`: si inserisce all'invio e, come il resto del report, non si modifica più. «Vale
solo per la relazione finale» (PO, 09/11): la scheda e i progetti formativi (US-1008) restano con le loro date.
*Alternativa scartata:* `internship.actual_end_date`. La scheda si modifica sempre, quindi la data resterebbe
correggibile dopo la chiusura, e starebbe accanto alle date che i progetti formativi useranno.

### D2 — Colonna facoltativa nel database, obbligatoria nel servizio
V38 aggiunge la colonna **senza** `NOT NULL`, per due motivi:
- i report già inviati non hanno la data, e la fine prevista non è la fine effettiva;
- in caso di ripristino la release precedente inserisce report senza la colonna (A16).

La regola «obbligatoria» sta nella validazione del modulo (`@NotNull`). Anche «non prima dell'inizio» resta nel
servizio, perché confronta due tabelle e un `CHECK` non può farlo. Un trigger per un controllo su una riga scritta una
sola volta non vale la complessità. Il servizio legge l'inizio dalla scheda del tirocinio. Se nel frattempo il Tutor
l'ha svuotato (la scheda si salva anche incompleta), non c'è un inizio con cui confrontare la data.

### D3 — Nessun limite verso il futuro
Il criterio chiede solo «non prima dell'inizio». Ci sono due casi reali:
- la fine effettiva può essere dopo la fine prevista: una proroga non riportata nella scheda;
- può essere dopo oggi, se il Tutor manda il report nell'ultima settimana.

Per questo non si confronta la data né con la fine prevista né con oggi.

### D4 — Precompilazione dal modulo
La pagina del report legge la scheda (`GET /tickets/{id}/internship`, già del solo Tutor) e propone la fine prevista;
l'inizio serve anche al controllo nel browser, con lo stesso messaggio del server. Se la scheda non si legge, il campo
parte vuoto e il controllo resta al server.

### D5 — Audit e visualizzazione
- L'audit della chiusura registra `actualEndDate` con il valore: è una data del tirocinio, non un dato personale, come
  `startDate` nella scheda.
- `ClosureReportDTO.actualEndDate` è assente per i report di prima (l'API omette i null).
- Il report inviato mostra la data come prima riga.
- L'evento `TIROCINIO_CONCLUSO` nella cronologia dell'azienda non cambia.

### D6 — Ruolo del tutor aziendale
- Colonna `internship_company_tutor.role varchar(200)`, facoltativa, con
  `ck_internship_company_tutor_role CHECK (role IS NULL OR btrim(role) <> '')`.
- Il servizio salva come vuoto un testo di soli spazi, come per il recapito.
- Nell'API è `CompanyTutorDTO.role`.
- La riga resta immutabile e sostituita in blocco: cambiare il ruolo cambia i tutor. L'audit registra
  `companyTutors` solo come cambiato, come oggi.

Nel segnaposto di oggi `TUTOR_AZIENDALI` ogni tutor diventa «Nome, ruolo (recapito)», saltando le parti vuote. US-1011
aggiungerà i segnaposto separati.

### D7 — Interfaccia
- **Report:** primo campo «Fine effettiva del tirocinio», `type="date"`, obbligatorio. L'aiuto dice che è precompilato
  con la fine prevista e che vale solo per la relazione finale. Gli errori passano dal riepilogo, come gli altri campi.
- **Scheda:** in ogni riga dei tutor, tra nome e recapito, il campo «Funzione o ruolo del tutor N» con un esempio
  nell'aiuto. Le tre caselle stanno su una riga negli schermi larghi e una sotto l'altra sullo smartphone.

## Risks / Trade-offs

- [La regola «non prima dell'inizio» non è nel database] → test di integrazione sul servizio; la riga si scrive solo
  dalla transizione `CLOSE`.
- [I report già inviati non hanno la data] → la vista la omette e la relazione finale di quei tirocini (US-1011) avrà
  la riga da completare a mano.

## Migration Plan

Deploy normale: V38 aggiunge due colonne vuote e un vincolo che i dati attuali rispettano. In caso di ripristino la
release precedente ignora le colonne nuove.
