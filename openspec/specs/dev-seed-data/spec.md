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

### Requirement: Zona delle aziende dimostrative
Alcune aziende dimostrative SHALL avere una zona del catalogo, e almeno una SHALL restare senza, per provare nel
collaudo la colonna, il filtro e l'elenco del Tutor. Le aziende dimostrative con una zona scelta a mano MUST NOT
cambiare.

#### Scenario: Collaudo nuovo
- **WHEN** il collaudo parte dai dati dimostrativi
- **THEN** nell'elenco delle aziende alcune hanno una zona e almeno una no

#### Scenario: Zona già scelta a mano
- **WHEN** un'azienda dimostrativa ha già una zona scelta a mano
- **THEN** il riavvio non la cambia

### Requirement: Tirocini dimostrativi con le valutazioni
I dati dimostrativi SHALL comprendere due tirocini già chiusi con la valutazione dell'azienda, su «Demo Logistica
S.r.l.», di due Tutor diversi, e una segnalazione in tirocinio del Tutor `tutor1`, con il report di restituzione da
compilare. Servono a provare nel collaudo il report con le faccine e la sezione delle valutazioni.

#### Scenario: Collaudo nuovo
- **WHEN** il collaudo parte dai dati dimostrativi
- **THEN** «Demo Logistica S.r.l.» ha due valutazioni, e `tutor1` ha una segnalazione in tirocinio con il report da
  compilare

### Requirement: Nave scuola tra le aziende dimostrative
Un'azienda dimostrativa SHALL essere già nave scuola, per provare nel collaudo il simbolo e il filtro. Un'azienda
dimostrativa cambiata a mano MUST NOT cambiare al riavvio.

#### Scenario: Collaudo nuovo
- **WHEN** il collaudo parte dai dati dimostrativi
- **THEN** «Demo Ristorazione S.n.c.» è nave scuola e ha il 🚢 accanto al nome

### Requirement: Richieste di contatto dimostrative
I dati dimostrativi SHALL avere due richieste di contatto di `tutor1` per aziende non ancora in anagrafica, per il
collaudo: una aperta e una già in carico all'operatore. Le richieste dimostrative cambiate a mano MUST NOT cambiare
al riavvio.

#### Scenario: Collaudo nuovo
- **WHEN** il collaudo parte dai dati dimostrativi
- **THEN** l'operatore vede due richieste di contatto, una aperta e una in carico, e nel menu il numero 1

