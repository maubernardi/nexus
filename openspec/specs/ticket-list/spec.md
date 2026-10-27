# ticket-list Specification

## Purpose
Elenco delle segnalazioni come pagina iniziale di tutti i ruoli, con viste adatte al lavoro di ciascuno.
## Requirements
### Requirement: Home delle segnalazioni
La pagina iniziale di Tutor, Call Center e ADMIN SHALL essere l'elenco delle segnalazioni, con viste per ruolo, filtri
per stato e progetto riportati nell'indirizzo, numero di risultati annunciato e stato indicato in testo.

#### Scenario: Tutor
- **WHEN** un Tutor apre l'applicazione
- **THEN** vede "Le mie segnalazioni" (le proprie, dalla più recente), può passare a "Del mio progetto" e trova
  "Nuova segnalazione"

#### Scenario: Call Center
- **WHEN** un operatore apre l'applicazione
- **THEN** vede "Da prendere in carico" e può passare a "In carico a me" e a "Tutte", dove compare l'operatore che ha
  in carico ciascuna segnalazione

#### Scenario: Vecchi indirizzi
- **WHEN** si apre `/coda` o `/lavorazioni`
- **THEN** si arriva rispettivamente alle viste "Da prendere in carico" e "In carico a me"

### Requirement: Segnalazioni del proprio progetto
Il Tutor SHALL vedere, in sola lettura e complete, le segnalazioni dei progetti a cui è assegnato, anche dei colleghi, e
SHALL NOT vedere segnalazioni di altri progetti.

#### Scenario: Collega dello stesso progetto
- **WHEN** un collega dello stesso progetto ha inviato una segnalazione
- **THEN** il Tutor la vede in "Del mio progetto", con nome e cognome del beneficiario e il tutor che l'ha inviata

#### Scenario: Altro progetto
- **WHEN** una segnalazione appartiene a un progetto a cui il Tutor non è assegnato
- **THEN** non compare in nessuna sua vista

#### Scenario: Vista di un altro ruolo
- **WHEN** un Tutor chiede la vista "Da prendere in carico" all'API
- **THEN** la risposta è `400`

### Requirement: Comandi di creazione negli elenchi
Il menu del Tutor SHALL contenere solo "Segnalazioni" e "Beneficiari"; i comandi "Nuova segnalazione" e "Nuovo
beneficiario" SHALL essere pulsanti in cima ai rispettivi elenchi.

#### Scenario: Menu del Tutor
- **WHEN** un Tutor apre l'applicazione
- **THEN** il menu mostra "Segnalazioni" e "Beneficiari", e l'elenco delle segnalazioni ha in cima "Nuova segnalazione"

### Requirement: Segnalazioni da approvare
Il Call Center e l'ADMIN SHALL avere nella Home la vista "Da approvare", con le segnalazioni in
`IN_ATTESA_APPROVAZIONE` dalla più vecchia e la tipologia proposta indicata come nuova.

#### Scenario: Vista da approvare
- **WHEN** un operatore apre la vista "Da approvare"
- **THEN** vede le segnalazioni con una nuova tipologia proposta, con l'indicazione "(nuova, da approvare)"

### Requirement: Riaperte in cima
Nella vista "In carico a me" le segnalazioni `RIAPERTO` SHALL comparire per prime, con lo stato evidenziato in testo,
seguite dalle altre dalla più vecchia.

#### Scenario: Riaperta in cima
- **WHEN** una segnalazione dell'operatore viene riaperta
- **THEN** compare in cima a "In carico a me" con lo stato "Riaperto" evidenziato

