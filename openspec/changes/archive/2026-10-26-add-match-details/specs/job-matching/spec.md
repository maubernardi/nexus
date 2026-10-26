## ADDED Requirements

### Requirement: Dettagli di azienda e mansione durante l'abbinamento
Per ogni mansione compatibile il sistema SHALL offrire "Dettagli", che apre un dialogo modale con la scheda
dell'azienda (ragione sociale, partita IVA, sede, referente e contatti, stato), la scheda della mansione (titolo,
tipologia, zona, descrizione), le note più recenti e lo storico con il riepilogo dei pregressi. Dal dialogo SHALL essere
possibile proporre la mansione con la stessa conferma della tabella. Il dialogo SHALL tenere il focus al suo interno,
chiudersi con Esc o "Chiudi" riportando il focus su "Dettagli", e occupare l'intero schermo sui dispositivi piccoli.

#### Scenario: Consultazione
- **WHEN** l'operatore preme "Dettagli" su una mansione compatibile
- **THEN** il dialogo mostra azienda, mansione, note recenti e pregressi, con il link alla scheda completa dell'azienda

#### Scenario: Chiusura
- **WHEN** l'operatore preme Esc nel dialogo
- **THEN** il dialogo si chiude e il focus torna sul pulsante "Dettagli" di quella mansione

#### Scenario: Proposta dal dialogo
- **WHEN** l'operatore preme "Proponi" nel dialogo
- **THEN** il dialogo si chiude e compare la conferma della proposta, con il focus sul suo titolo, senza proposta
  inviata finché non conferma
