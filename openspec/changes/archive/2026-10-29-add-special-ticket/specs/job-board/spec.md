## ADDED Requirements

### Requirement: Candidatura a un annuncio
Il Tutor SHALL poter candidare dalla bacheca un suo beneficiario senza segnalazioni aperte a un annuncio pubblicato,
aperto a tutti o riservato a un suo progetto. Nasce una segnalazione speciale con la corsia veloce, in lavorazione,
collegata all'annuncio e con la tipologia della mansione. Il progetto è quello dell'annuncio se riservato, altrimenti
uno dei progetti del Tutor. Un annuncio MUST NOT avere più di una candidatura in valutazione: la prima lo blocca finché
il Call Center non decide, e in bacheca l'annuncio mostra «Candidatura in valutazione», senza il comando.

#### Scenario: Candidatura
- **WHEN** il Tutor sceglie "Candida un beneficiario" su un annuncio libero e invia la candidatura
- **THEN** nasce la segnalazione speciale n. N, in cima alla coda del Call Center con la corsia veloce, e la cronologia
  riporta "Candidatura all'annuncio #M della bacheca"

#### Scenario: Annuncio bloccato
- **WHEN** un altro beneficiario è già candidato all'annuncio
- **THEN** in bacheca l'annuncio mostra «Candidatura in valutazione» e una nuova candidatura è rifiutata con il motivo

#### Scenario: Annuncio non visibile
- **WHEN** l'annuncio è una bozza o è riservato a un progetto del quale il Tutor non fa parte
- **THEN** per il Tutor l'annuncio non esiste

#### Scenario: Gestione
- **WHEN** l'operatore apre un annuncio bloccato
- **THEN** vede quale segnalazione lo blocca, con il link
