# login-page Specification

## Purpose
Pagina di accesso di Keycloak con l'aspetto di NEXUS (tema `nexus`), solo in italiano e conforme a WCAG 2.2 AA,
come il resto dell'app.
## Requirements
### Requirement: Pagina di accesso con l'aspetto del sito
La pagina di accesso di Keycloak per il realm `nexus` SHALL usare il tema `nexus`, con:
- l'intestazione del sito (fondo verde, filetto oro, logo e nome);
- il titolo "Accedi a NEXUS";
- i colori, i font e il piè di pagina del sito.

Il tema SHALL seguire la preferenza chiaro/scuro del sistema e MUST rispettare WCAG 2.2 AA: contrasti, focus visibile,
controlli alti almeno 44 px, nessuno scorrimento orizzontale su schermi stretti.

#### Scenario: Accesso alla PWA
- **WHEN** un utente apre NEXUS senza una sessione
- **THEN** Keycloak mostra "Accedi a NEXUS" con l'intestazione e i colori del sito

#### Scenario: Credenziali errate
- **WHEN** l'utente invia credenziali non valide
- **THEN** il campo è evidenziato con il rosso del sito e il messaggio d'errore è in italiano

### Requirement: Solo italiano
Il realm `nexus` SHALL offrire la sola lingua italiana, che è anche quella predefinita. La pagina di accesso MUST NOT
mostrare il selettore della lingua.

#### Scenario: Browser in inglese
- **WHEN** l'utente apre la pagina di accesso con un browser che preferisce l'inglese
- **THEN** la pagina è in italiano

### Requirement: Tema distribuito con l'immagine di Keycloak
Il tema SHALL far parte dell'immagine `nexus-keycloak` pubblicata a ogni deploy. La CI SHALL verificare che la pagina
di accesso del realm di sviluppo usi il tema, sia in italiano e non abbia il selettore della lingua.

#### Scenario: Deploy successivo
- **WHEN** si pubblica un nuovo commit sulla demo
- **THEN** Keycloak parte dall'immagine con il tema, senza file da copiare sul server

### Requirement: Recupero della password
La pagina di accesso SHALL offrire il collegamento «Password dimenticata?». Indicando username o email, l'utente SHALL
ricevere per email, dal mittente di NEXUS, un collegamento per impostare una nuova password, valido 30 minuti. La pagina
di recupero SHALL usare il tema di NEXUS, in italiano. Quando la password cambia, l'utente SHALL ricevere un'email di
sicurezza; le password sbagliate MUST NOT generare email (richiesta del PO del 07/11).

#### Scenario: Collegamento per email
- **WHEN** un utente sceglie «Password dimenticata?» e scrive la sua email
- **THEN** riceve un'email con il collegamento; aprendolo entro 30 minuti imposta una nuova password ed entra in NEXUS

#### Scenario: Collegamento scaduto
- **WHEN** l'utente apre il collegamento dopo 30 minuti
- **THEN** Keycloak lo rifiuta e propone di chiederne uno nuovo

#### Scenario: Email di sicurezza
- **WHEN** un utente imposta una nuova password con il collegamento
- **THEN** riceve una sola email che lo avvisa del cambio; una password sbagliata all'accesso non manda email

#### Scenario: Pagina con il tema
- **WHEN** si apre la pagina di recupero dalla pagina di accesso
- **THEN** la pagina ha l'intestazione e lo stile di NEXUS

### Requirement: Password comuni rifiutate
Quando un utente imposta o cambia la password, Keycloak SHALL rifiutare una password che compare nell'elenco delle
password più usate, senza distinguere maiuscole e minuscole, con un messaggio in italiano che inviti a sceglierne
un'altra. La regola si aggiunge a quelle esistenti (almeno 12 caratteri, diversa da username ed email) e MUST valere per
la prima password dell'invito, il recupero e ogni aggiornamento.

#### Scenario: Password comune
- **WHEN** un utente sceglie `Password1234` come nuova password
- **THEN** la password è rifiutata con «Password troppo comune» e l'utente resta nella pagina per sceglierne un'altra

#### Scenario: Password non comune
- **WHEN** l'utente sceglie una password lunga che non è nell'elenco
- **THEN** la password è accettata

