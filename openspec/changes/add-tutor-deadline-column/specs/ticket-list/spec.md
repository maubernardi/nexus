## ADDED Requirements

### Requirement: Scadenza nella Home del Tutor
Nelle viste del Tutor ("Le mie segnalazioni" e "Del mio progetto") l'elenco SHALL mostrare la colonna "Scadenza" con
l'ultimo giorno utile del timer attivo, vuota quando non c'è scadenza. Dopo il promemoria del 4° giorno lavorativo la
scadenza SHALL essere evidenziata con la scritta "in scadenza", non con il solo colore. Le segnalazioni con una
scadenza SHALL comparire per prime, dalla più vicina; le altre seguono dalla più recente.

#### Scenario: Proposta accettata
- **WHEN** il Tutor ha accettato una proposta e il timer scade alla fine di mercoledì 4 novembre
- **THEN** in "Le mie segnalazioni" la segnalazione compare in cima con la scadenza "entro mer 04/11"

#### Scenario: Dopo il promemoria
- **WHEN** il Tutor ha ricevuto il promemoria del timer
- **THEN** la scadenza della segnalazione è in grassetto, preceduta da "in scadenza"

#### Scenario: Timer fermo
- **WHEN** il Tutor ha registrato l'appuntamento
- **THEN** la segnalazione non ha più scadenza e torna nell'ordine dalla più recente
