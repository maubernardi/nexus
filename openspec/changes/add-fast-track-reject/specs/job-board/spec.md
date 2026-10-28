## ADDED Requirements

### Requirement: Rifiuto della corsia veloce
Il Call Center e l'ADMIN SHALL poter rifiutare, con un motivo obbligatorio, la corsia veloce di una segnalazione
speciale presa in carico. La segnalazione resta SPECIAL e in lavorazione, senza corsia veloce, e si lavora come le
altre; l'annuncio resta pubblicato e torna candidabile; il Tutor riceve la notifica e il motivo è in cronologia.

#### Scenario: Rifiuto
- **WHEN** l'operatore rifiuta la corsia veloce scrivendo il motivo
- **THEN** la segnalazione continua in lavorazione normale, l'annuncio #N torna candidabile e il Tutor è avvisato

#### Scenario: Motivo mancante
- **WHEN** l'operatore conferma il rifiuto senza motivo
- **THEN** il motivo è segnalato come obbligatorio e nulla cambia

### Requirement: Corsia veloce che decade con l'annuncio
Quando un annuncio con una candidatura in attesa viene archiviato, la segnalazione SHALL perdere la corsia veloce
nella stessa operazione, con il motivo in cronologia e la notifica al Tutor. Vale sia per l'archiviazione a mano, sia
per quella automatica (mansione abbinata o ritirata, azienda disattivata). La conferma dell'archiviazione SHALL
avvisare l'operatore.

#### Scenario: Archiviazione a mano
- **WHEN** l'operatore archivia un annuncio con una candidatura in attesa
- **THEN** la conferma lo avvisa, e dopo la segnalazione è in lavorazione normale con il motivo "Annuncio #N archiviato
  dal Call Center"

#### Scenario: Mansione abbinata a un'altra segnalazione
- **WHEN** la mansione dell'annuncio viene abbinata a un'altra segnalazione
- **THEN** la candidatura in attesa perde la corsia veloce con il motivo
