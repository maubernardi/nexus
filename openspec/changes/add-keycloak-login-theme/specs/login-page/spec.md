## ADDED Requirements

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
