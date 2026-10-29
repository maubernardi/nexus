## ADDED Requirements

### Requirement: Griglia oraria del tirocinio
La scheda del tirocinio SHALL avere due fasce orarie, precompilate con 9:00–13:00 e 14:00–18:00 e modificabili. Per
ogni giorno della settimana il Tutor SHALL poter scegliere la prima fascia, la seconda o entrambe, e indicare le ore
del giorno. Le ore indicate MUST NOT superare la durata delle fasce scelte; senza ore valgono le fasce intere. Le
fasce MUST essere in ordine e il totale settimanale MUST NOT superare le 40 ore.

#### Scenario: Fasce intere e ore ridotte
- **WHEN** il Tutor sceglie per il lunedì la prima fascia e per il martedì entrambe le fasce con 3 ore
- **THEN** la scheda conta 4 ore il lunedì e 3 il martedì, e il totale settimanale è di 7 ore

#### Scenario: Ore oltre le fasce
- **WHEN** il Tutor indica 5 ore per un giorno con la sola prima fascia di 4 ore
- **THEN** il campo delle ore di quel giorno lo segnala e nulla è salvato

#### Scenario: Fasce modificate
- **WHEN** il Tutor cambia la prima fascia in 8:30–12:30
- **THEN** i giorni con la prima fascia contano le ore della nuova fascia

### Requirement: Monte ore del tirocinio
La scheda SHALL mostrare il monte ore complessivo. Il calcolo va dalla data di inizio alla fine prevista con la
griglia oraria e salta i giorni dei periodi di sospensione. Salta anche le festività nazionali, se la scheda lo
chiede: la scelta è attiva di default. Il monte ore SHALL ricalcolarsi quando cambiano griglia, date o sospensioni. Senza
date o senza griglia, il monte ore MUST NOT essere indicato.

#### Scenario: Festività e sospensioni
- **WHEN** la griglia ha 4 ore il lunedì e il martedì, il tirocinio va da lunedì 7 a domenica 13 dicembre 2026 e le
  festività nazionali sono escluse
- **THEN** il monte ore è di 4 ore, perché martedì 8 dicembre è festivo

#### Scenario: Festività incluse
- **WHEN** nella stessa scheda il Tutor toglie l'esclusione delle festività
- **THEN** il monte ore diventa di 8 ore

#### Scenario: Ricalcolo con una sospensione
- **WHEN** il Tutor aggiunge una sospensione per lunedì 7 dicembre
- **THEN** il monte ore scende di 4 ore
