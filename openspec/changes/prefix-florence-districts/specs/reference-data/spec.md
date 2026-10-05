## MODIFIED Requirements

### Requirement: Zone iniziali
Il sistema SHALL fornire in ogni ambiente, come zone iniziali attive, i quartieri di Firenze con il nome preceduto da
"Firenze " (Firenze Novoli, Firenze Isolotto, Firenze Legnaia, Firenze Rifredi, Firenze Campo di Marte, Firenze Santa
Croce, Firenze Oltrarno, Firenze Galluzzo, Firenze Le Piagge, Firenze Rovezzano, Firenze Le Cure, Firenze Trespiano,
Firenze Sorgane, Firenze Casellina, Firenze Soffiano, Firenze Serpiolle, Firenze Castello, Firenze San Niccolò, Firenze
Varlungo, Firenze Santo Spirito, Firenze Due Strade, Firenze Porta al Prato, Firenze San Giovanni, Firenze Statuto,
Firenze Europa, Firenze Gavinana, Firenze San Lorenzo, Firenze San Frediano, Firenze San Miniato, Firenze San Marco) e i
comuni dell'hinterland senza prefisso (Scandicci, Sesto Fiorentino, Campi Bisenzio, Borgo San Lorenzo, Scarperia e San
Piero, Vicchio, Dicomano, Bagno a Ripoli, Impruneta, Fiesole).

#### Scenario: Ambiente nuovo
- **WHEN** l'applicazione parte su un database vuoto, senza dati demo
- **THEN** l'elenco delle zone contiene le 40 zone iniziali, con i quartieri preceduti da "Firenze "

#### Scenario: Demo esistente
- **WHEN** l'applicazione parte su un database demo con le zone segnaposto
- **THEN** le zone segnaposto sono disattivate e nessun beneficiario o mansione demo vi resta collegato

#### Scenario: Quartiere e comune omonimi
- **WHEN** l'utente apre l'elenco delle zone
- **THEN** distingue "Firenze San Lorenzo" (quartiere) da "Borgo San Lorenzo" (comune)
