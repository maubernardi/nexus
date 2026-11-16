## ADDED Requirements

### Requirement: Formattazione dei modelli conservata
La compilazione SHALL cambiare solo il testo dei segnaposto: grassetti, tabulazioni, simboli e campi del modello MUST
restare come sono, anche nelle righe con segnaposto. Un segnaposto spezzato in più parti di formattazione diversa
SHALL prendere la formattazione della parte in cui comincia; un a capo nel valore SHALL restare con quella
formattazione.

#### Scenario: Riga con parti in grassetto
- **WHEN** il modello ha la riga «{PROMOTORE} d'ora in poi denominato “**soggetto promotore**”»
- **THEN** il documento ha la denominazione del promotore e «soggetto promotore» ancora in grassetto

#### Scenario: Caselle e tabulazioni
- **WHEN** il modello ha una tabulazione, «Lunedì» in grassetto e `{GIORNO_LUNEDI}` nella stessa riga
- **THEN** il documento ha la tabulazione, «Lunedì» in grassetto e la casella al posto del segnaposto

### Requirement: Tipologia dell'ente a caselle
I documenti SHALL poter riportare la tipologia dell'ente di presa in carico a caselle, con un segnaposto per ogni
tipologia (Comune, Unione dei Comuni, Comunità montana, Società della Salute, Azienda Sanitaria, Ufficio
interdistrettuale di esecuzione penale esterna, Altro soggetto): `☒` sulla tipologia dell'ente del beneficiario, `☐`
sulle altre. Senza ente le caselle MUST essere tutte `☐`.

#### Scenario: Ente comunale
- **WHEN** il beneficiario è in carico al Comune di Firenze, di tipologia Comune
- **THEN** il documento ha `☒` su «Comune» e `☐` su «Società della Salute» e sulle altre

#### Scenario: Senza ente
- **WHEN** il tirocinio è Extracurriculare e il beneficiario non ha un ente di presa in carico
- **THEN** le caselle della tipologia sono tutte `☐`
