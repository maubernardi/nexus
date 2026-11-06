## ADDED Requirements

### Requirement: Progetti nei cataloghi
L'ADMIN SHALL gestire i progetti finanziati nella pagina «Cataloghi», con le stesse regole degli altri cataloghi:
aggiunta, nuovo nome, disattivazione e riattivazione, codice ricavato dal nome, nome unico tra i progetti attivi,
audit di ogni modifica. Un progetto disattivato MUST NOT essere selezionabile per una nuova segnalazione né nei
filtri; le segnalazioni che già lo usano MUST restare consultabili con il loro progetto.

#### Scenario: Nuovo progetto
- **WHEN** l'ADMIN aggiunge il progetto «FSE+ Inclusione 2027»
- **THEN** il progetto è attivo con il codice `FSE_INCLUSIONE_2027`, si può assegnare ai Tutor e la creazione è
  nell'audit

#### Scenario: Progetto disattivato
- **WHEN** l'ADMIN disattiva un progetto che ha già delle segnalazioni
- **THEN** il Tutor non lo trova più tra i progetti della nuova segnalazione e una segnalazione con quel progetto è
  rifiutata, ma le segnalazioni esistenti restano consultabili con il loro progetto
