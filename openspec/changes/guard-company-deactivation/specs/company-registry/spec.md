## ADDED Requirements

### Requirement: Disattivazione con proposte in corso
Il sistema SHALL rifiutare con `409` la disattivazione di un'azienda che ha mansioni BLOCCATE da segnalazioni, indicando
per ciascuna il titolo della mansione e il numero della segnalazione; l'azienda resta attiva.

#### Scenario: Mansione bloccata
- **WHEN** un operatore disattiva un'azienda la cui mansione «Magazziniere» è bloccata dalla segnalazione n. 30
- **THEN** la risposta è `409` con il messaggio "Non si può disattivare l'azienda: la mansione «Magazziniere» è bloccata
  dalla segnalazione n. 30" e la scheda azienda lo mostra
