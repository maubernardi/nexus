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

### Requirement: Aziende dimostrative da completare
I dati dimostrativi SHALL comprendere due aziende arrivate come dal database attuale:
- una con il nome e senza partita IVA;
- una senza nome e senza partita IVA.

Servono a provare l'etichetta «Da completare», il filtro e il completamento dalla scheda.

#### Scenario: Elenco filtrato
- **WHEN** nella demo l'operatore filtra le aziende da completare
- **THEN** l'elenco mostra le due aziende dimostrative da completare

### Requirement: Categoria delle aziende dimostrative
Le aziende dimostrative SHALL avere una categoria merceologica e una descrizione dell'attività. Le aziende dimostrative
già modificate a mano MUST NOT cambiare.

#### Scenario: Elenco con le categorie
- **WHEN** il backend locale si avvia con i dati dimostrativi
- **THEN** l'elenco delle aziende mostra una categoria per ogni azienda dimostrativa

#### Scenario: Categoria già scelta a mano
- **WHEN** un'azienda dimostrativa ha già una categoria o un'attività scelta a mano
- **THEN** il riavvio non la cambia

### Requirement: Referenti delle aziende dimostrative
Le aziende dimostrative senza referenti SHALL ricevere referenti con il ruolo, e una di loro SHALL avere anche la sede
operativa quando l'elenco dei comuni è caricato. Le aziende dimostrative che hanno già referenti o una sede operativa
MUST NOT cambiare.

#### Scenario: Demo nuova
- **WHEN** il backend locale si avvia su un database vuoto con i dati dimostrativi
- **THEN** ogni azienda dimostrativa ha almeno un referente con il ruolo, e una ne ha due

#### Scenario: Referenti già presenti
- **WHEN** un'azienda dimostrativa ha già dei referenti
- **THEN** l'avvio non li cambia

### Requirement: Affidabilità e interesse delle aziende dimostrative
Alcune aziende dimostrative SHALL avere affidabilità e interesse al progetto, come le aziende convertite dal database
attuale: almeno una con stelle e pollici, una con le sole stelle e una senza valutazioni. Servono a provare nel
collaudo la scheda, le colonne e i filtri. Le aziende dimostrative già valutate a mano MUST NOT cambiare.

#### Scenario: Collaudo nuovo
- **WHEN** il collaudo parte dai dati dimostrativi
- **THEN** nell'elenco delle aziende ce n'è almeno una con stelle e pollici, una con le sole stelle e una senza
  valutazioni

#### Scenario: Valutazione già scelta a mano
- **WHEN** un'azienda dimostrativa ha già affidabilità o interesse scelti a mano
- **THEN** il riavvio non li cambia

