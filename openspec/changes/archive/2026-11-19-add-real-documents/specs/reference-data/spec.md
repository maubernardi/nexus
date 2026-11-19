## ADDED Requirements

### Requirement: Dati dei documenti per progetto
L'ADMIN SHALL poter indicare per ogni progetto, nei cataloghi:
- il riferimento all'avviso, che parte da un testo predefinito (l'Avviso pubblico n. 10 di GOL);
- la riga di intestazione con codice e CUP, per esempio «Firenze in Rete 2 – Codice 9014358 – CUP B81D24000440006».

Entrambi sono facoltativi: senza riferimento vale il testo predefinito, senza intestazione il documento ha la riga da
completare. Ogni modifica SHALL essere nell'audit del progetto. Gli altri utenti MUST NOT modificarli.

#### Scenario: Intestazione del progetto
- **WHEN** l'ADMIN indica per GOL l'intestazione «Firenze in Rete 2 – Codice 9014358 – CUP B81D24000440006»
- **THEN** il testo è salvato, la modifica è nell'audit e i documenti delle segnalazioni di GOL lo riportano

#### Scenario: Testo predefinito dell'avviso
- **WHEN** l'ADMIN svuota il riferimento all'avviso di un progetto e salva
- **THEN** il progetto torna al testo predefinito, e i cataloghi lo indicano come predefinito

#### Scenario: Solo l'ADMIN
- **WHEN** un Tutor chiede di modificare i dati dei documenti di un progetto
- **THEN** la risposta è `403`
