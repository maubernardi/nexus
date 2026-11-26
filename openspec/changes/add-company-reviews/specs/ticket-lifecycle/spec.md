## ADDED Requirements

### Requirement: Valutazione dell'azienda nel report di restituzione
Il report di restituzione SHALL chiedere al Tutor tre voti da 1 a 5, obbligatori, ognuno con cinque faccine e una
parola (Pessima, Scarsa, Sufficiente, Buona, Ottima):
- soddisfazione nell'interazione con l'azienda;
- capacità dell'azienda di accogliere il beneficiario;
- capacità formativa dell'azienda e del tutor aziendale.

Con i tre voti scelti il modulo SHALL mostrare la media come faccina, quella del numero intero più vicino. La nota
SHALL esserci sempre, anche con la media migliore: una sola per i tre voti, facoltativa, al massimo 500 caratteri, con
l'invito a non scrivere dati personali. Il sistema MUST rifiutare con `400` un voto mancante o fuori da 1–5 e una
nota più lunga di 500 caratteri, e il database MUST rifiutarli con un vincolo. La valutazione SHALL restare legata
all'azienda del tirocinio al momento della chiusura. Dopo l'invio MUST NOT cambiare. I voti SHALL entrare nell'audit
della chiusura; la nota, testo libero, MUST NOT entrarci. Il report inviato SHALL mostrare anche la valutazione.

#### Scenario: Media
- **WHEN** il Tutor sceglie 4, 5 e 4
- **THEN** la media è 4,3 e compare la faccina «Buona»

#### Scenario: Valutazione ottima con la nota
- **WHEN** il Tutor sceglie 5, 5 e 4 e scrive una nota
- **THEN** la media è 4,7, la faccina è «Ottima» e la nota si invia con il report

#### Scenario: Voto mancante
- **WHEN** il Tutor invia il report senza la capacità formativa
- **THEN** il modulo non si invia e il riepilogo degli errori indica il voto che manca

#### Scenario: Report già inviati
- **WHEN** si apre un report inviato prima di questa funzione
- **THEN** il report non ha la valutazione e l'azienda non riceve valutazioni da quel tirocinio
