## ADDED Requirements

### Requirement: Elenco delle aziende a pagine
L'elenco delle aziende SHALL arrivare una pagina alla volta, in ordine di ragione sociale, con il numero delle aziende
che rispondono ai filtri. L'operatore SHALL poter scegliere 10, 25, 50 o 100 aziende per pagina (25 se non sceglie);
il sistema MUST NOT restituire più di 100 aziende per richiesta. Pagina e aziende per pagina SHALL restare
nell'indirizzo, come i filtri; un filtro nuovo SHALL ripartire dalla prima pagina.

#### Scenario: Prima pagina
- **WHEN** il Call Center apre l'elenco con 13.799 aziende
- **THEN** vede le prime 25 in ordine di ragione sociale, «13.799 aziende: dalla 1 alla 25» e «Pagina 1 di 552»

#### Scenario: Pagina successiva
- **WHEN** l'operatore sceglie «Successiva»
- **THEN** vede le aziende dalla 26 alla 50 e l'indirizzo ha `pagina=2`

#### Scenario: Aziende per pagina
- **WHEN** l'operatore sceglie 100 dal menu «Aziende per pagina»
- **THEN** vede le prime 100 aziende, dalla prima pagina

#### Scenario: Filtro nuovo
- **WHEN** alla pagina 3 l'operatore spunta «Solo le aziende da completare»
- **THEN** vede la prima pagina delle aziende da completare, con le stesse aziende per pagina

#### Scenario: Troppe aziende per pagina
- **WHEN** una richiesta all'API chiede 1.000 aziende per pagina, o una pagina negativa
- **THEN** la risposta è `400`

#### Scenario: Pagina oltre l'ultima
- **WHEN** un indirizzo chiede la pagina 9 e le pagine sono 2
- **THEN** l'elenco passa alla pagina 2
