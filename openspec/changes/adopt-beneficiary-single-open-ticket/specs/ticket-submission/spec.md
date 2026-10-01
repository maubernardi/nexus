## ADDED Requirements

### Requirement: Una sola segnalazione aperta per beneficiario
Il sistema SHALL impedire di inviare una segnalazione per un beneficiario che ne ha già una aperta, cioè in qualunque
stato diverso da `FORM_RESTITUZIONE`. Il modulo SHALL mostrare i beneficiari con una segnalazione aperta come non
selezionabili, indicandone il numero.

#### Scenario: Segnalazione già aperta
- **WHEN** il Tutor invia una segnalazione per un beneficiario che ha la segnalazione n. N aperta
- **THEN** la risposta è `400` con un errore sul campo `beneficiaryId` che cita la segnalazione n. N, e nessun ticket
  viene creato

#### Scenario: Percorso concluso
- **WHEN** l'unica segnalazione del beneficiario è in `FORM_RESTITUZIONE`
- **THEN** il Tutor può inviarne una nuova

#### Scenario: Invii concorrenti
- **WHEN** due richieste per lo stesso beneficiario superano insieme il controllo
- **THEN** il database ne accetta una sola e l'altra riceve `409`

#### Scenario: Modulo
- **WHEN** il Tutor apre il modulo e un suo beneficiario ha la segnalazione n. N aperta
- **THEN** quel beneficiario compare come non selezionabile con il testo "segnalazione n. N aperta"; se tutti i suoi
  beneficiari sono in questa situazione, la pagina lo spiega al posto del modulo
