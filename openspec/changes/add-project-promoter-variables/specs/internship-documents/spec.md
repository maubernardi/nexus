## MODIFIED Requirements

### Requirement: Dati del soggetto promotore
I documenti SHALL riportare i dati del soggetto promotore del progetto della segnalazione, indicati dall'ADMIN nei
cataloghi, non dai modelli né dalla configurazione del server:
- denominazione, sede e codice fiscale;
- legale rappresentante con ruolo, luogo e data di nascita;
- telefono, mail e PEC;
- posizione INAIL;
- polizza RC con compagnia, numero e scadenza.

I dati MUST NOT stare nel repository. Un dato non indicato SHALL restare una riga da completare.

#### Scenario: Dati indicati
- **WHEN** per il progetto della segnalazione è indicata la polizza RC con la scadenza 22/07/2027
- **THEN** i documenti riportano polizza e scadenza «22/07/2027»

#### Scenario: Dati non indicati
- **WHEN** per il progetto della segnalazione manca la PEC del promotore
- **THEN** i documenti hanno la riga da completare al suo posto

## ADDED Requirements

### Requirement: Variabili del progetto nei documenti
I documenti SHALL sostituire ogni variabile del progetto della segnalazione con il suo valore.

#### Scenario: Variabile compilata
- **WHEN** GOL ha la variabile `SEDE_OPERATIVA` con valore «Via Roma 1, Firenze» e il modello contiene
  `{SEDE_OPERATIVA}`
- **THEN** il documento di una segnalazione di GOL riporta «Via Roma 1, Firenze»
