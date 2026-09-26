## Purpose

Rende il frontend di NEXUS una Progressive Web App installabile e resiliente alla connettività
intermittente, tipica dei Tutor sul territorio, con una shell di navigazione comune a tutti i ruoli.

## ADDED Requirements

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
La shell SHALL mostrare l'utente autenticato e il suo ruolo, recuperati dall'API identità.

#### Scenario: Utente caricato
- **WHEN** l'app si avvia con un utente autenticato
- **THEN** l'intestazione mostra nome e ruolo dell'utente
