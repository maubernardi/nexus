## ADDED Requirements

### Requirement: Indirizzi composti nei documenti
Nella Convenzione e nel Progetto Formativo la sede legale dell'azienda, la sede del tirocinio e la residenza del
tirocinante SHALL comparire come un'unica riga con indirizzo, comune e sigla della provincia, nella forma
«Via Roma 1, Firenze (FI)». Una parte mancante MUST essere omessa senza lasciare separatori vuoti; se mancano tutte
resta la riga vuota da completare.

#### Scenario: Residenza completa
- **WHEN** la residenza è «Via Gramsci» «12» a Sesto Fiorentino
- **THEN** il documento riporta «Via Gramsci 12, Sesto Fiorentino (FI)»

#### Scenario: Comune mancante
- **WHEN** la sede del tirocinio ha l'indirizzo «Via Roma 1» ma non il comune
- **THEN** il documento riporta «Via Roma 1»
