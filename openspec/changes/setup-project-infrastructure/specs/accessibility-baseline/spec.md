## Purpose

Stabilisce i requisiti minimi di accessibilità (WCAG 2.2 livello AA) validi per ogni schermata di NEXUS
e il modo in cui vengono verificati, così che nessuna feature successiva li degradi.

## ADDED Requirements

### Requirement: Conformità WCAG 2.2 AA
Ogni schermata dell'interfaccia SHALL rispettare i criteri di successo WCAG 2.2 livello A e AA.

#### Scenario: Verifica automatica
- **WHEN** vengono eseguiti i test automatici del frontend
- **THEN** l'analisi axe delle schermate e dei componenti renderizzati non riporta violazioni

### Requirement: Lingua e struttura semantica
Il documento SHALL dichiarare `lang="it"`, avere un titolo di pagina descrittivo che cambia a ogni route,
e usare i landmark `header`, `nav`, `main` e `footer` con un unico `h1` per pagina.

#### Scenario: Cambio pagina
- **WHEN** l'utente naviga verso una nuova route
- **THEN** il titolo del documento si aggiorna e il focus viene spostato sull'intestazione principale della nuova pagina

### Requirement: Navigazione da tastiera
Tutte le funzionalità SHALL essere utilizzabili solo con la tastiera, con ordine di focus logico, senza
trappole di focus, e con un link "Salta al contenuto principale" come primo elemento focalizzabile.

#### Scenario: Skip link
- **WHEN** l'utente preme Tab al caricamento della pagina
- **THEN** il primo elemento focalizzato è il link "Salta al contenuto principale" che porta il focus al `main`

### Requirement: Focus visibile e dimensione dei target
Ogni elemento interattivo SHALL mostrare un indicatore di focus visibile con contrasto almeno 3:1 e
non oscurato da altri elementi, e SHALL avere un'area di attivazione di almeno 24x24 CSS px.

#### Scenario: Focus su pulsante
- **WHEN** un pulsante riceve il focus da tastiera
- **THEN** è visibile un contorno di focus conforme

### Requirement: Contrasto e colore
I testi SHALL avere contrasto almeno 4.5:1 (3:1 per testo grande e componenti UI) sia in tema chiaro sia
scuro, e le informazioni MUST NOT essere veicolate solo tramite il colore.

#### Scenario: Stato espresso con badge
- **WHEN** viene mostrato uno stato (ad esempio online/offline)
- **THEN** lo stato è comunicato anche da testo o icona con etichetta, non solo dal colore

### Requirement: Movimento ridotto e ridimensionamento
L'interfaccia SHALL rispettare `prefers-reduced-motion` disattivando le animazioni non essenziali e SHALL
restare utilizzabile con zoom del testo al 200% e reflow a 320px.

#### Scenario: Preferenza di movimento ridotto
- **WHEN** il sistema operativo dell'utente richiede movimento ridotto
- **THEN** transizioni e animazioni non essenziali sono disattivate

### Requirement: Messaggi di stato annunciati
Messaggi di stato, errori e notifiche (toast, avvisi offline, aggiornamenti disponibili) SHALL essere
annunciati alle tecnologie assistive tramite regioni live, senza spostare il focus.

#### Scenario: Avviso offline
- **WHEN** la connessione cade
- **THEN** uno screen reader annuncia l'avviso di stato offline
