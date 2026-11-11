## Context

- I comuni (US-114) sono versionati: una riga di `municipality` per versione, chiave naturale il codice catastale,
  versione attuale con `valid_to` vuoto. `GET /municipalities?q=` cerca solo le versioni valide oggi.
- Oggi `company.legal_address`, `internship.site` e `internship.residence` sono testi liberi (300 caratteri), usati
  così nei documenti (`SEDE_LEGALE`, `SEDE_TIROCINIO`, `RESIDENZA_TIROCINANTE`) e nei controlli per l'avvio.
- Se il deploy fallisce, il workflow riavvia la release precedente sullo stesso database (A16), con `ddl-auto=validate`:
  lo schema nuovo deve restare compatibile con il codice vecchio.
- Il frontend non ha una casella con suggerimenti: i moduli usano `FormField`, `Input`, `NativeSelect` e
  react-hook-form.

## Goals / Non-Goals

**Goals:**
- Comune dall'elenco ufficiale in tre indirizzi, salvato come versione, senza perdere i testi già inseriti.
- Una casella con suggerimenti riutilizzabile, conforme al pattern combobox delle WAI-ARIA APG.

**Non-Goals:**
- Altri indirizzi (legale rappresentante, enti, luogo di nascita) e nuovi segnaposto: vedi la proposta.

## Decisions

### D1 — Riferimento alla versione del comune
Colonne `*_municipality_id` → `municipality(id)`: la versione scelta, con il nome e la provincia di quel momento.
*Alternativa scartata:* il codice catastale, con la versione risolta alla data: un cambio di provincia cambierebbe i
documenti già prodotti, contro la decisione di US-114.

### D2 — Migrazione solo additiva
V37 aggiunge `company.legal_municipality_id`, `internship.site_municipality_id`, `internship.residence_municipality_id`
e `internship.residence_street_number` (20), con FK e indici con nome. La colonna `internship.residence` **non si
rinomina**: nel codice diventa il campo `residenceStreet` (via), mappato sulla stessa colonna, e conserva il testo già
inserito. *Alternativa scartata:* rinominarla in `residence_street`: la release precedente non ripartirebbe dopo un
deploy fallito (A16). Allo stesso modo `legal_address` e `site` restano l'indirizzo, ora senza il comune.

### D3 — Validazione del comune
Il servizio accetta l'id di una versione esistente; se è diverso da quello salvato MUST essere valida oggi
(`valid_to` vuoto), altrimenti `400` con errore sul campo (`legalMunicipalityId`, `siteMunicipalityId`,
`residenceMunicipalityId`). Così un comune salvato e poi chiuso resta, come i tipi di tirocinio non più ammessi (US-115).
Un id inesistente è `400` sul campo.

### D4 — API
In uscita il comune è un `MunicipalityDTO` annidato (`legalMunicipality`, `siteMunicipality`, `residenceMunicipality`),
così il frontend mostra «Firenze (FI)» e sa se la versione è chiusa; in entrata solo gli id. Nella scheda `residence`
diventa `residenceStreet` più `residenceStreetNumber`. Il cambio di contratto riguarda solo il nostro frontend.

### D5 — Completezza per l'avvio
`site` manca se manca l'indirizzo o il comune; `residence` manca se manca uno tra comune, via e numero civico. Il
numero civico è testo (es. «12/A», «snc»): il suggerimento del campo dice di scrivere «snc» se non c'è.

### D6 — Testo composto per i documenti
Un'unica funzione compone «indirizzo, Comune (PR)» saltando le parti vuote; per la residenza l'indirizzo è
«via numero». I segnaposto restano gli stessi, così i modelli attuali funzionano; US-1011 aggiungerà comune e
provincia separati.

### D7 — Audit
Azienda: `legalAddress` e `legalMunicipality` con i valori («Firenze (FI)»). Scheda: `site` e `siteMunicipality` con i
valori; la residenza resta una sola voce `residence` registrata come cambiata se cambia una delle tre parti, senza
valori (dato personale).

### D8 — Casella con suggerimenti (`MunicipalityField`)
Molecola che segue il pattern APG «combobox con lista, scelta manuale»:
- `input` con `role="combobox"`, `aria-autocomplete="list"`, `aria-expanded`, `aria-controls`,
  `aria-activedescendant`; lista `role="listbox"` con `role="option"` e `aria-selected`;
- frecce su e giù per scorrere, Invio per scegliere, Esc per chiudere (o svuotare se è già chiusa), Tab esce senza
  scegliere; il clic su un'opzione non toglie il focus alla casella;
- ricerca dalla seconda lettera con l'hook di US-114 (`useMunicipalitySearch`) e un ritardo di 300 ms, come la
  ricerca dei Cataloghi; un messaggio di stato (`role="status"`) annuncia «N comuni» o «Nessun comune trovato»;
- il server manda al massimo 20 comuni: con l'elenco al limite, in fondo ai suggerimenti si legge «Solo i primi 20
  comuni: scrivi altre lettere…» (emerso nel controllo in Chrome: con «pra» Prato non compariva);
- opzioni alte almeno 44 px (WCAG 2.5.8, oltre il minimo di 24 px).

Il valore per react-hook-form è `{ text, municipality }`: scrivere svuota la scelta, scegliere imposta il testo
«Nome (PR)». Il modulo valida «testo senza comune» con «Scegli il comune dall'elenco» e invia solo l'id.
*Alternative scartate:* `<datalist>` (non porta l'id e lo screen reader lo annuncia in modo diverso nei browser);
casella di ricerca più tendina (due controlli per una scelta, più lento sullo smartphone); una libreria esterna (una
dipendenza in più per un solo componente).

### D9 — Demo
`MunicipalitySeed` (profilo `seed`), dopo aver garantito l'elenco, esegue `seed/demo-addresses.sql` a ogni avvio:
assegna Firenze alle aziende dimostrative (P.IVA `99…`) che non hanno un comune e toglie dal loro indirizzo il suffisso
fittizio «, Città Demo». Idempotente e non tocca le aziende già corrette. *Alternativa scartata:* uno script Flyway
ripetibile `R__`: gira prima che l'elenco dei comuni sia caricato, quindi su un database nuovo non troverebbe Firenze.

## Risks / Trade-offs

- [La casella con suggerimenti è il pezzo più delicato per l'accessibilità] → test di tastiera e axe nel componente,
  controllo in Chrome prima del merge (A26).
- [I tirocini già avviati hanno sede e residenza senza comune] → i documenti riportano il testo di prima finché il Tutor
  non sceglie il comune; la scheda elenca la sede e la residenza tra i dati mancanti.
- [Ricerca a ogni lettera] → ritardo di 300 ms e risultati in cache per testo (React Query).

## Migration Plan

Deploy normale: V37 aggiunge colonne vuote; i dati esistenti restano dove sono. In caso di ripristino la release
precedente ignora le colonne nuove.
