## ADDED Requirements

### Requirement: Avvio del tirocinio
Il Tutor della segnalazione SHALL poter avviare il tirocinio di una segnalazione con l'appuntamento fissato. La
segnalazione passa in `IN_TIROCINIO` solo se la scheda del tirocinio è completa. L'avvio MUST essere registrato:
- nella cronologia e nell'audit;
- nello storico dell'azienda (`TIROCINIO_AVVIATO`);
- con una notifica all'operatore che ha in carico la segnalazione.

Gli altri utenti MUST NOT poter avviare il tirocinio.

#### Scenario: Avvio con la scheda completa
- **WHEN** il Tutor avvia il tirocinio di una segnalazione con l'appuntamento fissato e la scheda completa
- **THEN** la segnalazione è «In tirocinio», lo storico dell'azienda registra l'avvio e l'operatore riceve la notifica

#### Scenario: Scheda incompleta
- **WHEN** il Tutor avvia il tirocinio con la scheda senza griglia oraria
- **THEN** l'avvio è rifiutato con l'elenco di ciò che manca e la segnalazione resta con l'appuntamento fissato

#### Scenario: Un altro utente
- **WHEN** un collega di progetto o il Call Center provano ad avviare il tirocinio
- **THEN** la richiesta è rifiutata
