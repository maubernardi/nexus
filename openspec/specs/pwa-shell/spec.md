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

### Requirement: Tema chiaro o scuro a scelta
La barra in alto SHALL avere, accanto all'utente, un pulsante che cambia il tema: con il tema chiaro mostra la luna e
passa al tema scuro; con il tema scuro mostra il sole e passa al chiaro. Il pulsante SHALL avere un nome che dice che
cosa fa («Passa al tema scuro», «Passa al tema chiaro»). La scelta SHALL restare nel browser anche dopo la chiusura e
valere su tutte le pagine; senza una scelta SHALL valere il tema del sistema operativo. All'apertura di una pagina il
tema scelto MUST valere dal primo disegno, senza passare dall'altro.

#### Scenario: Passaggio al tema scuro
- **WHEN** con il tema chiaro l'utente preme la luna
- **THEN** l'interfaccia passa ai colori scuri e il pulsante mostra il sole, con il nome «Passa al tema chiaro»

#### Scenario: Scelta ricordata
- **WHEN** l'utente ha scelto il tema scuro, chiude il browser e riapre NEXUS, con il sistema operativo in tema chiaro
- **THEN** NEXUS si apre già nel tema scuro

#### Scenario: Nessuna scelta
- **WHEN** l'utente non ha mai premuto il pulsante e il sistema operativo è in tema scuro
- **THEN** NEXUS usa il tema scuro e il pulsante mostra il sole

### Requirement: Intestazione fissa delle schede
Le schede con un collegamento per tornare all'elenco o alla pagina di partenza SHALL avere in cima un'intestazione con
il ritorno, sopra il titolo, e il titolo. Mentre la scheda scorre, l'intestazione SHALL restare ferma sotto la barra in
alto; il ritorno MUST NOT ripetersi in fondo. Valgono le schede di aziende, segnalazioni, beneficiari e loro modifica,
annunci della bacheca, tirocinio, report di restituzione e pagine degli utenti. Quando la finestra è bassa
l'intestazione SHALL scorrere con la pagina. Barra in alto e intestazione MUST NOT coprire l'elemento con il focus.

#### Scenario: Scheda lunga
- **WHEN** l'operatore apre la scheda di un'azienda e la scorre fino in fondo
- **THEN** «Torna all'elenco delle aziende» e il nome dell'azienda restano visibili sotto la barra in alto, e in fondo
  non c'è un altro ritorno

#### Scenario: Focus da tastiera
- **WHEN** l'operatore scorre i campi del modulo con il tasto Tab
- **THEN** il campo con il focus resta visibile sotto l'intestazione fissa

#### Scenario: Beneficiario aperto da una segnalazione
- **WHEN** il Tutor apre un beneficiario dalla segnalazione n. 24
- **THEN** l'intestazione fissa ha «Torna alla segnalazione n. 24»

#### Scenario: Finestra bassa
- **WHEN** la finestra è alta meno di 32 rem, per esempio uno smartphone in orizzontale o il testo molto ingrandito
- **THEN** l'intestazione della scheda scorre con la pagina

