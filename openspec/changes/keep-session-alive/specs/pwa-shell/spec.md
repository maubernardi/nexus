## ADDED Requirements

### Requirement: Sessione di lavoro
Finché l'app è aperta nel browser, anche in secondo piano o con lo schermo bloccato, la sessione SHALL rinnovarsi da
sola, entro la durata massima stabilita da Keycloak (10 ore). La scadenza per inattività di Keycloak (30 minuti) MUST
restare per le sessioni senza l'app aperta. Prima della durata massima l'app SHALL avvisare l'utente, con l'ora della
scadenza e la possibilità di aprire subito una sessione nuova (WCAG 2.2.1). L'avviso MUST NOT coprire il contenuto.

#### Scenario: Pagina aperta in secondo piano
- **WHEN** l'utente lascia l'app aperta in una scheda in secondo piano per un'ora
- **THEN** tornando all'app è ancora collegato e lavora senza inserire la password

#### Scenario: Sessione scaduta mentre il PC era sospeso
- **WHEN** il PC resta in sospensione per più di 30 minuti e al ritorno la sessione è scaduta
- **THEN** l'app torna alla pagina di accesso, senza lasciare dati sullo schermo

#### Scenario: Rete assente
- **WHEN** il rinnovo fallisce perché manca la rete, ma la sessione non è scaduta
- **THEN** l'app non cambia pagina e riprova al controllo successivo

#### Scenario: Avviso prima della durata massima
- **WHEN** mancano cinque minuti alla fine della sessione
- **THEN** sotto l'intestazione compare un avviso, letto dai lettori di schermo, con l'ora della scadenza, «Accedi di
  nuovo adesso» e «Ho capito»

#### Scenario: Nuovo accesso dall'avviso
- **WHEN** l'utente sceglie «Accedi di nuovo adesso»
- **THEN** l'app apre la pagina di accesso con la richiesta della password, e dopo l'accesso la sessione dura di nuovo
  fino al massimo
