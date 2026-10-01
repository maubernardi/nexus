## MODIFIED Requirements

### Requirement: Identità utente nella shell
La shell SHALL mostrare l'utente autenticato e il suo ruolo, recuperati dall'API identità. Se l'API identità risponde
che l'utente non è abilitato (`USER_NOT_ENABLED`), l'app SHALL mostrare al posto della shell una pagina dedicata che
spiega la situazione, indica di contattare l'amministratore e offre solo il pulsante Esci, senza riprovare in automatico
né proporre "Riprova".

#### Scenario: Utente caricato
- **WHEN** l'app si avvia con un utente autenticato
- **THEN** l'intestazione mostra nome e ruolo dell'utente

#### Scenario: Utente non abilitato
- **WHEN** un utente autenticato ma non censito o disattivato apre l'app
- **THEN** vede la pagina "Account non abilitato" con l'indicazione di contattare l'amministratore e il pulsante Esci,
  senza "Riprova", e il focus è sul titolo

#### Scenario: Disattivato durante la sessione
- **WHEN** un'API risponde `USER_NOT_ENABLED` mentre l'utente sta usando l'app
- **THEN** l'app mostra la pagina "Account non abilitato"

#### Scenario: Altro errore del profilo
- **WHEN** il profilo non si carica per un altro motivo (es. rete)
- **THEN** l'app mostra il messaggio di errore generico con "Riprova"
