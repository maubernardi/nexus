## ADDED Requirements

### Requirement: Avvisi di fine tirocinio
Ogni mattina alle 8, ora italiana, per ogni tirocinio in corso il Tutor della segnalazione SHALL ricevere una notifica
in app e un'email, in giorni di calendario dalla fine prevista, che tiene conto delle proroghe:
- 7 e 3 giorni prima: la convenzione è in chiusura, va prorogata o, se no, il tirocinio va chiuso;
- 3 e 7 giorni dopo: il tirocinio va chiuso con il report di restituzione o prorogato, e finché i tirocini finiti non
  sono chiusi il Tutor non apre nuove segnalazioni.

Per la stessa fine prevista ogni avviso MUST partire una volta sola; se un giorno il controllo non gira, SHALL partire
solo l'avviso più recente dovuto. Una proroga SHALL far ripartire gli avvisi dalla nuova fine. Un tirocinio chiuso MUST
NOT ricevere avvisi. Ogni avviso SHALL entrare nella cronologia della segnalazione. Il controllo MUST NOT modificare la
versione della scheda del tirocinio.

#### Scenario: Una settimana alla fine
- **WHEN** il tirocinio della segnalazione n. 12 finisce il 10/12 e il controllo gira il 03/12
- **THEN** il Tutor riceve la notifica e l'email «la convenzione è in chiusura» per la segnalazione n. 12

#### Scenario: Stesso giorno, di nuovo
- **WHEN** il controllo gira due volte lo stesso giorno
- **THEN** l'avviso parte una volta sola

#### Scenario: Tirocinio finito e non chiuso
- **WHEN** il tirocinio è finito il 10/12, non è chiuso e il controllo gira il 13/12 e poi il 17/12
- **THEN** il Tutor riceve due volte l'avviso «chiudilo o prorogalo», che ricorda il blocco delle nuove segnalazioni

#### Scenario: Proroga
- **WHEN** dopo l'avviso dei 3 giorni il Tutor registra una proroga fino al 10/02
- **THEN** gli avvisi della vecchia fine non partono più e ripartono dal 03/02

#### Scenario: Controllo saltato
- **WHEN** il server è spento dal 12/12 al 14/12 e il controllo gira il 15/12, con il tirocinio finito il 10/12
- **THEN** parte un solo avviso, quello dei 3 giorni dopo la fine

#### Scenario: Tutor che sta modificando la scheda
- **WHEN** alle 8 parte un avviso mentre il Tutor ha aperta la scheda del tirocinio
- **THEN** il suo salvataggio riesce, senza il messaggio di modifica concorrente
