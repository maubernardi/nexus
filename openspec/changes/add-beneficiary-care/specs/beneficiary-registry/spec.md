## ADDED Requirements

### Requirement: Contatti e presa in carico
Il Tutor proprietario SHALL poter indicare per il beneficiario, tutti facoltativi: mail e telefono, l'ente che lo ha in
carico (dal catalogo degli enti attivi) e il case manager (nome, sede operativa, telefono, mail). L'ente MUST NOT
cambiare mentre il beneficiario ha un tirocinio aperto, cioè una segnalazione non in FORM_RESTITUZIONE con la scheda del
tirocinio. Il case manager SHALL potersi sostituire anche durante il tirocinio, ma durante un tirocinio di inclusione
aperto nome, mail e telefono MUST NOT essere svuotati. Nell'audit SHALL comparire solo quali campi sono cambiati;
l'anonimizzazione MUST cancellare contatti, ente e case manager.

#### Scenario: Ente e case manager
- **WHEN** il Tutor indica l'ente «Comune di Firenze» e il case manager con nome, mail e telefono
- **THEN** la scheda del beneficiario li mostra e l'audit registra i campi cambiati senza i valori

#### Scenario: Ente bloccato
- **WHEN** il Tutor cambia l'ente di un beneficiario con un tirocinio aperto
- **THEN** il campo segnala che l'ente non si può cambiare durante il tirocinio e nulla è salvato

#### Scenario: Case manager durante l'inclusione
- **WHEN** il Tutor svuota la mail del case manager durante un tirocinio di inclusione aperto
- **THEN** il campo segnala che è obbligatoria durante il tirocinio di inclusione

#### Scenario: Anonimizzazione
- **WHEN** un beneficiario con contatti, ente e case manager viene anonimizzato
- **THEN** questi dati sono cancellati
