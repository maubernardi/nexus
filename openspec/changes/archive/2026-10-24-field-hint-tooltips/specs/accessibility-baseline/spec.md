## ADDED Requirements

### Requirement: Suggerimenti dei campi
I suggerimenti dei campi dei moduli SHALL essere accessibili da un'icona "i" accanto all'etichetta, che li mostra al
passaggio del mouse, al focus da tastiera e al tocco, senza spostare il campo; il riquadro SHALL restare aperto mentre il
puntatore vi passa sopra, chiudersi con Esc senza spostare il focus (WCAG 1.4.13) e non uscire dallo schermo. Il testo
SHALL restare associato al campo per le tecnologie assistive.

#### Scenario: Campi affiancati
- **WHEN** un campo con suggerimento è affiancato a uno senza
- **THEN** i due controlli sono allineati

#### Scenario: Tastiera
- **WHEN** l'utente raggiunge l'icona con Tab
- **THEN** il suggerimento si apre, e con Esc si chiude lasciando il focus sull'icona

#### Scenario: Lettore di schermo
- **WHEN** il campo riceve il focus
- **THEN** il suggerimento è annunciato come descrizione del campo, anche a riquadro chiuso
