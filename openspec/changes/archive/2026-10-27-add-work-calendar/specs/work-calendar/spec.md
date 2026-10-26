## Purpose

Calendario unico dei giorni lavorativi, valido per tutti i progetti: patrono e chiusure impostati dall'ADMIN; le
festività nazionali sono calcolate dal sistema.

## ADDED Requirements

### Requirement: Patrono
Il sistema SHALL mantenere un solo calendario, valido per tutti i progetti, con il patrono (giorno e mese),
inizialmente il 24 giugno. L'ADMIN SHALL poterlo cambiare; una data inesistente MUST essere rifiutata; la modifica SHALL
essere registrata nell'audit con prima e dopo.

#### Scenario: Cambio del patrono
- **WHEN** l'ADMIN imposta il patrono al 29 giugno
- **THEN** il calendario lo mostra e l'audit registra `PATRON_UPDATE` da 24 giugno a 29 giugno

#### Scenario: Data inesistente
- **WHEN** l'ADMIN imposta il 30 febbraio
- **THEN** il sistema rifiuta la modifica indicando il giorno

#### Scenario: Modifica concorrente
- **WHEN** l'ADMIN salva partendo da una versione superata del calendario
- **THEN** il sistema risponde `409`

### Requirement: Chiusure
L'ADMIN SHALL poter aggiungere chiusure (un giorno o un periodo, estremi compresi, con descrizione obbligatoria) e
rimuoverle; ogni aggiunta e rimozione SHALL essere registrata nell'audit con i valori. Una fine precedente all'inizio
MUST essere rifiutata, anche dal database.

#### Scenario: Periodo
- **WHEN** l'ADMIN aggiunge la chiusura dal 10 al 21 agosto "Ferie estive"
- **THEN** compare nell'elenco delle chiusure, ordinato dalla prima

#### Scenario: Un solo giorno
- **WHEN** l'ADMIN aggiunge una chiusura indicando solo la data di inizio
- **THEN** la chiusura vale per quel giorno

#### Scenario: Altri ruoli
- **WHEN** un operatore del Call Center o un Tutor chiede il calendario
- **THEN** la risposta è `403` e il menu non mostra la voce "Calendario"
