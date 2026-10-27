## MODIFIED Requirements

### Requirement: Segnalazione (Ticket)
Ogni ticket SHALL riferire tutor, progetto e beneficiario, avere tipo `NORMAL` o `SPECIAL` e uno stato tra `NUOVA`,
`IN_ATTESA_APPROVAZIONE`, `IN_LAVORAZIONE`, `PROPOSTA_AZIENDA`, `PROPOSTA_ACCOLTA`, `APPUNTAMENTO`, `IN_TIROCINIO`,
`FORM_RESTITUZIONE`, `RIAPERTO`. La mansione richiesta MUST essere indicata da una tipologia del catalogo oppure da un
testo libero, mai entrambi e mai nessuno dei due. Un ticket `SPECIAL` MUST riferire il post di bacheca da cui è nato;
un ticket fast-track MUST essere di tipo `SPECIAL`. Il timer SHALL registrare avvio, invio del promemoria e scadenza;
scadenza e avvio MUST essere presenti insieme. Azienda e zona della proposta si ricavano dalla mansione abbinata.

#### Scenario: Mansione richiesta doppia
- **WHEN** si salva un ticket con sia una tipologia del catalogo sia un testo libero
- **THEN** l'operazione viene rifiutata

#### Scenario: Segnalazione speciale senza post
- **WHEN** si salva un ticket `SPECIAL` senza riferimento a un post di bacheca
- **THEN** l'operazione viene rifiutata

#### Scenario: Stato non previsto
- **WHEN** si salva un ticket con uno stato non elencato
- **THEN** l'operazione viene rifiutata
