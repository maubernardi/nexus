# dev-seed-data Specification

## Purpose
Fornisce dati dimostrativi coerenti per lo sviluppo e la prova manuale delle funzionalità, senza mai raggiungere
ambienti diversi da quello locale.
## Requirements
### Requirement: Dati dimostrativi solo in locale
Con il profilo di sviluppo locale il sistema SHALL caricare dati dimostrativi. Con qualunque altro profilo, test
automatici inclusi, MUST NOT caricarli.

#### Scenario: Avvio locale
- **WHEN** il backend si avvia con il profilo `local` su un database vuoto
- **THEN** sono presenti progetti, utenti, zone, tipologie di mansione, aziende, mansioni, beneficiari, ticket e post dimostrativi

#### Scenario: Avvio in altri profili
- **WHEN** il backend si avvia con un profilo diverso da `local`
- **THEN** il database non contiene dati dimostrativi

### Requirement: Scenari del seed
I dati dimostrativi SHALL comprendere:
- i progetti `GOL` e `POLIS`;
- gli utenti tutor1 (assegnato a `GOL`), tutor2 (assegnato a `GOL` e `POLIS`), un operatore Call Center e un amministratore,
  con gli stessi identificativi usati dall'autenticazione di sviluppo;
- ticket in più stati, tra cui almeno una segnalazione speciale;
- un post pubblico, uno riservato a un progetto e uno in bozza.

I beneficiari MUST avere dati palesemente fittizi.

#### Scenario: Visibilità incrociata provabile
- **WHEN** si consultano i dati dimostrativi
- **THEN** esiste almeno un ticket `GOL` di tutor1 e almeno un ticket `POLIS` di tutor2

#### Scenario: Riavvio
- **WHEN** il backend locale viene riavviato più volte
- **THEN** i dati dimostrativi non vengono duplicati

### Requirement: Comune delle aziende dimostrative
Quando l'elenco dei comuni è caricato, le aziende dimostrative SHALL avere la sede legale con indirizzo e comune, senza
toccare le aziende modificate a mano.

#### Scenario: Demo con l'elenco dei comuni
- **WHEN** il backend locale si avvia con l'elenco dei comuni caricato
- **THEN** le aziende dimostrative hanno la sede legale con un comune dell'elenco

#### Scenario: Azienda già corretta a mano
- **WHEN** un'azienda dimostrativa ha già un comune scelto a mano
- **THEN** l'avvio non lo cambia

