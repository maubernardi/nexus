# reference-data Specification

## Purpose
Fornisce ai moduli dell'applicazione gli elenchi di riferimento condivisi (zone, tipologie di mansione), limitati alle
voci attive.

## Requirements

### Requirement: Elenchi di riferimento
Il sistema SHALL esporre agli utenti autenticati gli elenchi delle zone e delle tipologie di mansione **attive**, con
identificativo, codice e nome, ordinati per nome.

#### Scenario: Voce disattivata
- **WHEN** una zona è disattivata
- **THEN** non compare nell'elenco delle zone

### Requirement: Zone iniziali
Il sistema SHALL fornire in ogni ambiente, come zone iniziali attive, i quartieri di Firenze (Novoli, Isolotto,
Legnaia, Rifredi, Campo di Marte, Santa Croce, Oltrarno, Galluzzo, Le Piagge, Rovezzano, Le Cure, Trespiano, Sorgane,
Casellina, Soffiano, Serpiolle, Castello, San Niccolò, Varlungo, Santo Spirito, Due Strade, Porta al Prato, San
Giovanni, Statuto, Europa, Gavinana, San Lorenzo, San Frediano, San Miniato, San Marco) e i comuni dell'hinterland
(Scandicci, Sesto Fiorentino, Campi Bisenzio, Borgo San Lorenzo, Scarperia e San Piero, Vicchio, Dicomano, Bagno a
Ripoli, Impruneta, Fiesole).

#### Scenario: Ambiente nuovo
- **WHEN** l'applicazione parte su un database vuoto, senza dati demo
- **THEN** l'elenco delle zone contiene le 40 zone iniziali

#### Scenario: Demo esistente
- **WHEN** l'applicazione parte su un database demo con le zone segnaposto
- **THEN** le zone segnaposto sono disattivate e nessun beneficiario o mansione demo vi resta collegato
