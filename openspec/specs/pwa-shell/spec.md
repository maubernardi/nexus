# pwa-shell Specification

## Purpose
Rende il frontend di NEXUS una Progressive Web App installabile e resiliente alla connettività
intermittente, tipica dei Tutor sul territorio, con una shell di navigazione comune a tutti i ruoli.
## Requirements
### Requirement: Installabilità
L'applicazione SHALL essere installabile come PWA: deve servire un web app manifest con nome, nome breve,
colori tema, `display: standalone`, `lang: it` e icone almeno 192x192, 512x512 e maskable, e deve registrare un service worker.

#### Scenario: Manifest valido
- **WHEN** il browser carica l'applicazione in produzione
- **THEN** trova un manifest collegato e un service worker attivo che soddisfano i criteri di installabilità

### Requirement: Funzionamento offline della shell
L'applicazione SHALL mettere in cache la shell (HTML, JS, CSS, icone) e SHALL mostrare la shell con un
avviso di stato offline quando la rete non è disponibile, invece dell'errore del browser.

#### Scenario: Apertura senza rete
- **WHEN** un utente che ha già visitato l'app la riapre senza connessione
- **THEN** l'app si carica e mostra un avviso accessibile che segnala l'assenza di connessione

### Requirement: Aggiornamento dell'applicazione
Quando è disponibile una nuova versione, l'applicazione SHALL avvisare l'utente e permettergli di
aggiornare con un'azione esplicita, senza ricaricare la pagina di sua iniziativa durante una compilazione.

#### Scenario: Nuova versione disponibile
- **WHEN** viene pubblicata una nuova versione mentre l'app è aperta
- **THEN** l'utente vede un avviso con un pulsante "Aggiorna" e la pagina si ricarica solo dopo il click

### Requirement: Layout responsive per ruolo
La shell SHALL adattarsi da 320px di larghezza (mobile-first, uso Tutor) fino al desktop (uso Call Center)
senza scroll orizzontale e senza perdita di contenuto o funzionalità.

#### Scenario: Viewport mobile
- **WHEN** l'app è visualizzata a 320px di larghezza
- **THEN** la navigazione è raggiungibile tramite menu e nessun contenuto richiede scroll orizzontale

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

