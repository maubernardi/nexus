## ADDED Requirements

### Requirement: Azienda nave scuola
L'azienda SHALL avere l'indicazione «Nave scuola», falsa se non segnata. Operatori del Call Center e ADMIN SHALL
poterla segnare o togliere dal modulo dell'azienda, anche senza una proposta di un Tutor; il cambio SHALL entrare
nell'audit con il valore di prima e di dopo. Accanto al nome di un'azienda nave scuola SHALL comparire il simbolo 🚢,
un'icona con il testo «Nave scuola» per il lettore di schermo, dove si sceglie l'azienda:
- l'elenco delle aziende del Call Center, che ha anche il filtro «Solo le navi scuola», nell'indirizzo;
- la lista e la scheda del Tutor;
- le mansioni compatibili dell'abbinamento e la proposta della segnalazione;
- la bacheca del Call Center.

Il simbolo MUST NOT comparire nella bacheca dei Tutor, che è anonima.

#### Scenario: Segnata dall'operatore
- **WHEN** l'operatore spunta «Nave scuola» nella scheda di un'azienda e salva
- **THEN** l'azienda è una nave scuola, l'audit registra il cambio e nell'elenco accanto al nome c'è il 🚢

#### Scenario: Filtro
- **WHEN** l'operatore spunta «Solo le navi scuola» nell'elenco delle aziende
- **THEN** vede, dalla prima pagina, solo le navi scuola

#### Scenario: Abbinamento
- **WHEN** l'operatore cerca le mansioni compatibili per una segnalazione
- **THEN** le mansioni delle navi scuola hanno il 🚢 accanto al nome dell'azienda

#### Scenario: Tutor
- **WHEN** il Tutor apre la sua lista delle aziende
- **THEN** le navi scuola hanno il 🚢 accanto al nome, e nella bacheca dei Tutor il simbolo non c'è
