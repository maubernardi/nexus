## ADDED Requirements

### Requirement: Bacheca del Tutor
Il Tutor SHALL vedere in bacheca gli annunci pubblicati aperti a tutti e quelli riservati ai suoi progetti, dal più
recente, filtrabili per zona e tipologia e consultabili da smartphone. La risposta MUST NOT contenere l'azienda (né
nome né identificativo), la mansione, il titolo o le note dell'annuncio. Un annuncio riservato SHALL indicare in testo
il progetto del Tutor a cui è riservato.

#### Scenario: Annunci visibili
- **WHEN** un Tutor del progetto GOL apre la bacheca
- **THEN** vede gli annunci pubblicati per tutti e quelli riservati a GOL, non quelli di altri progetti, le bozze o
  gli archiviati

#### Scenario: Nessun dato dell'azienda
- **WHEN** titolo e note di un annuncio citano l'azienda
- **THEN** la risposta al Tutor non contiene né il nome né l'identificativo dell'azienda

#### Scenario: Filtri
- **WHEN** il Tutor sceglie una zona
- **THEN** vede solo gli annunci di quella zona, e il filtro resta nell'indirizzo

### Requirement: Annuncio legato alla disponibilità della mansione
Quando una mansione smette di essere disponibile il suo annuncio attivo (bozza o pubblicato) SHALL essere archiviato
nella stessa operazione, con il motivo nell'audit. Succede in tre casi: la mansione è abbinata a una segnalazione,
la mansione è ritirata, l'azienda è disattivata.

#### Scenario: Abbinamento
- **WHEN** l'operatore abbina a una segnalazione una mansione con un annuncio pubblicato
- **THEN** l'annuncio è archiviato con il motivo "Mansione abbinata alla segnalazione n. N"

#### Scenario: Ritiro della mansione
- **WHEN** l'operatore ritira una mansione con un annuncio
- **THEN** l'annuncio è archiviato e l'esito lo dice all'operatore
