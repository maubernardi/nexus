# NEXUS — Prove di collaudo

Elenco delle prove manuali da eseguire sulla demo, passo per passo. Ogni prova ha una casella: segnala `[x]` quando
l'esito è quello atteso. Se qualcosa non va, lascia la casella vuota e scrivi una nota sotto la prova
(`> Nota: …`): la raccolgo nella review dello sprint.

Il documento si aggiorna a ogni storia consegnata: le prove nuove sono segnate con lo sprint in cui sono arrivate.

| | |
|---|---|
| **Ambiente** | https://portalenexus.it |
| **Versione della demo** | `ed260ad` (10/11/2026): Sprint 1–12 completi (comuni e province, cataloghi dei documenti, contatti e presa in carico del beneficiario, sede di competenza), con le correzioni del controllo in Chrome |
| **Ultimo aggiornamento** | 11/11/2026 (T-23.1: da provare dopo il deploy di US-1007; T-22.1 … T-22.4 da provare) |

## Come prepararsi

- **Utenti demo** (password come da configurazione del server):

  | Utente | Ruolo | Progetti |
  |---|---|---|
  | `tutor1` | Tutor | GOL |
  | `tutor2` | Tutor | GOL e POLIS |
  | `operatore.cc` | Call Center | — (vede tutto) |
  | `admin` | Amministratore | — (vede tutto, è anche il "secondo operatore") |

- **Più utenti insieme**: usa una finestra normale e una **finestra anonima/privata** (o due browser diversi), così
  restano collegati due utenti contemporaneamente.
- **Numeri delle segnalazioni**: quando una prova crea una segnalazione, annota il numero (es. n. 33): le prove
  successive lo chiamano **«la tua segnalazione»**.
- Le prove sono in ordine: alcune usano i dati creati da quelle precedenti.

---

## 1. Accesso e navigazione

- [ ] **T-1.1 Accesso**
  1. Apri https://portalenexus.it ed entra come `tutor1`.
  2. **Atteso:** si apre la Home "Segnalazioni"; in alto il nome "Tutor Uno" (su smartphone dentro il menu) e "Esci".

- [ ] **T-1.2 Menu per ruolo**
  1. Come `tutor1`: il menu ha **Segnalazioni**, **Beneficiari** e **Bacheca**, più la campanella delle notifiche.
  2. Esci ed entra come `operatore.cc`: il menu ha **Segnalazioni**, **Aziende** e **Bacheca**.
  3. Esci ed entra come `admin`: il menu ha **Segnalazioni**, **Aziende**, **Bacheca**, **Cataloghi** e
     **Calendario**.
  4. **Atteso:** ogni ruolo vede solo le sue voci; la voce della pagina aperta è evidenziata (sottolineata in oro).

- [ ] **T-1.3 Pagina non consentita**
  1. Come `operatore.cc` apri direttamente https://portalenexus.it/calendario.
  2. **Atteso:** "Accesso non consentito", non la pagina del calendario.

- [ ] **T-1.4 Pagina inesistente**
  1. Apri https://portalenexus.it/pagina-che-non-esiste.
  2. **Atteso:** "Pagina non trovata", con il link per tornare alla Home.

- [ ] **T-1.5 Uscita**
  1. Premi **Esci**.
  2. **Atteso:** torni alla pagina di accesso; per rientrare servono di nuovo utente e password.

## 2. Beneficiari (Tutor)

- [ ] **T-2.1 Nuovo beneficiario: campi obbligatori**
  1. Come `tutor1`: **Beneficiari** → **Nuovo beneficiario** → premi **Registra beneficiario** senza compilare.
  2. **Atteso:** in cima un riepilogo "Correggi 5 errori…" con il focus su di esso; ogni voce è un link al campo; i
     campi sbagliati sono evidenziati con il messaggio sotto.

- [ ] **T-2.2 Suggerimenti dei campi ("i")**
  1. Nello stesso modulo, accanto ad "Anno di nascita" c'è l'icona **i**: passaci sopra con il mouse, poi cliccala, poi
     raggiungila con il tasto Tab.
  2. **Atteso:** compare il suggerimento ("Solo l'anno, per esempio 1998"); si chiude con Esc; i campi restano allineati.

- [ ] **T-2.3 Nuovo beneficiario: registrazione**
  1. Compila: nome, cognome (annotali), anno 1998, genere, zona **Firenze Novoli**, una lingua con livello.
  2. Premi **Registra beneficiario**.
  3. **Atteso:** "Beneficiario registrato", con i pulsanti "Segnala questo beneficiario", "Registra un altro
     beneficiario", "Torna alla Home".

- [ ] **T-2.4 I miei beneficiari: elenco e ricerca**
  1. **Beneficiari**: c'è il beneficiario appena creato, con zona e "Segnalazione aperta: Nessuna".
  2. Cerca scrivendo cognome e nome in ordine inverso, senza accenti → **Cerca**.
  3. **Atteso:** compare solo quel beneficiario; il numero dei risultati è indicato sopra la tabella.

- [ ] **T-2.5 Scheda e modifica**
  1. Apri il beneficiario → **Modifica** → cambia l'anno di nascita → **Salva modifiche**.
  2. **Atteso:** torni alla scheda con "Modifiche salvate." e il nuovo anno; la scheda indica "Inserito da Tutor Uno".

- [ ] **T-2.6 Visibilità per progetto**
  1. Come `tutor2` apri **Beneficiari**: il beneficiario di `tutor1` **non** c'è (l'elenco è solo dei propri).
  2. Come `tutor1`, in Home, vista **Del mio progetto**: compaiono anche le segnalazioni di `tutor2` del progetto GOL.
  3. Apri una segnalazione di `tutor2` del progetto GOL e clicca il nome del beneficiario.
  4. **Atteso:** la scheda del beneficiario si apre completa ma **senza "Modifica"**; "Torna alla segnalazione n. …"
     riporta indietro.

## 3. Segnalazioni (Tutor)

- [ ] **T-3.1 Nuova segnalazione**
  1. Come `tutor1`: Home → **Nuova segnalazione** (pulsante in cima all'elenco).
  2. Scegli il beneficiario di T-2.3, il progetto GOL, la mansione **Magazziniere** → **Invia segnalazione**.
  3. **Atteso:** "Segnalazione n. … inviata", stato «Nuova». Annota il numero: è **la tua segnalazione**.

- [ ] **T-3.2 Una sola segnalazione aperta per beneficiario**
  1. Apri di nuovo **Nuova segnalazione**.
  2. **Atteso:** lo stesso beneficiario compare come "segnalazione n. … aperta" e non è selezionabile.

- [ ] **T-3.3 Nuova tipologia di mansione**
  1. Registra un secondo beneficiario (come in T-2.3) e da **Segnala questo beneficiario** apri il modulo.
  2. Spunta **"La mansione non è nell'elenco: la descrivo io"** e scrivi "Fonico per eventi" → **Invia segnalazione**.
  3. **Atteso:** l'esito dice «In attesa di approvazione» e che entrerà in coda dopo la decisione del Call Center.
     Annota il numero (**segnalazione con tipologia nuova**).

- [ ] **T-3.4 Tipologia già nel catalogo**
  1. Ripeti T-3.3 scrivendo "magazziniere" (minuscolo) come nuova tipologia.
  2. **Atteso:** errore "La tipologia «Magazziniere» è già nell'elenco: selezionala".

- [ ] **T-3.5 Home del Tutor: viste e filtri**
  1. Home: vista **Le mie segnalazioni** (dalla più recente), poi **Del mio progetto** (con la colonna Tutor).
  2. Filtra per stato «Nuova», poi togli il filtro.
  3. **Atteso:** l'elenco cambia; l'indirizzo della pagina contiene la vista e il filtro (copiandolo si ritrova la
     stessa vista).

- [ ] **T-3.6 Dettaglio e cronologia**
  1. Apri **la tua segnalazione** dalla Home (clic sul numero).
  2. **Atteso:** dati della segnalazione (beneficiario cliccabile, zona, progetto, mansione, tutor) e la **Cronologia**
     con "Segnalazione inviata", data e autore; nessuna azione del Call Center.

## 4. Call Center: dalla coda alla proposta

- [ ] **T-4.1 Home del Call Center**
  1. Come `operatore.cc`: la Home si apre su **Da prendere in carico** (fast-track in cima, poi dalla più vecchia).
  2. Prova le viste **Da approvare**, **In carico a me**, **Tutte** (con la colonna dell'operatore).
  3. **Atteso:** **la tua segnalazione** è in "Da prendere in carico"; quella con tipologia nuova è in "Da approvare"
     come "Fonico per eventi (nuova, da approvare)".

- [ ] **T-4.2 Approvare una tipologia nuova**
  1. Vista **Da approvare** → apri la segnalazione dei dati demo con «Tecnico del suono».
  2. Nella sezione "Nuova tipologia da approvare" correggi il nome in "Tecnico audio" → **Approva e aggiungi al catalogo**.
  3. **Atteso:** esito "Tipologia «Tecnico audio» approvata…"; stato «Nuova»; in cronologia "Nuova tipologia approvata".
     Nel modulo di nuova segnalazione del Tutor ora compare "Tecnico audio".

- [ ] **T-4.3 Respingere una tipologia nuova**
  1. Apri la **segnalazione con tipologia nuova** (T-3.3) → "Oppure usa una tipologia del catalogo": scegli una tipologia
     e scrivi un motivo → **Usa questa tipologia**.
  2. **Atteso:** esito "Tipologia cambiata in «…»…"; la cronologia mostra proposta → scelta e il motivo.
  3. Come `tutor1`: la campanella segna una notifica; aprila: "Il Call Center ha scelto una tipologia diversa…".

- [ ] **T-4.4 Presa in carico**
  1. Come `operatore.cc`, vista **Da prendere in carico** → **Prendi in carico** su **la tua segnalazione**.
  2. **Atteso:** esito con il link alla segnalazione; la segnalazione passa in **In carico a me**, stato «In lavorazione».

- [ ] **T-4.5 Lavorazione condivisa**
  1. Come `admin` apri **la tua segnalazione**: "in carico a Operatore Call Center", con la ricerca delle compatibili
     disponibile e il pulsante **Prendila in carico tu**.
  2. Premi **Prendila in carico tu**; poi, come `operatore.cc`, fai lo stesso per riprenderla.
  3. **Atteso:** l'assegnazione passa da uno all'altro; la cronologia registra ogni passaggio ("X → Y").

- [ ] **T-4.6 Mansioni compatibili e filtri**
  1. Come `operatore.cc`, nella tua segnalazione: la ricerca parte da zona del beneficiario e mansione richiesta.
  2. Cambia **Zona** in "Tutte le zone" e **Tipologia** in "Tutte le tipologie".
  3. **Atteso:** il numero di mansioni compatibili cambia ed è annunciato; compaiono solo mansioni libere di aziende
     attive.

- [ ] **T-4.7 Dettagli dell'azienda prima di proporre**
  1. Su una mansione premi **Dettagli**.
  2. **Atteso:** si apre una finestra con azienda (contatti, stato), mansione, note recenti e storico con il riepilogo;
     con **Esc** si chiude e il cursore torna su "Dettagli"; su smartphone occupa tutto lo schermo.

- [ ] **T-4.8 Proposta all'azienda**
  1. Nella finestra "Dettagli" (o nella tabella) premi **Proponi** → **Conferma proposta**.
  2. **Atteso:** esito "Proposta inviata: «…» presso …"; stato «Proposta all'azienda»; nella scheda dell'azienda la
     mansione risulta **Bloccata – segnalazione n. …**; `tutor1` riceve la notifica "C'è una proposta di tirocinio…".

## 5. Tutor: esito della proposta e appuntamento

- [ ] **T-5.1 Rifiuto della proposta**
  1. Come `tutor1` apri **la tua segnalazione** → **Rifiuta la proposta** → premi **Conferma rifiuto** senza motivo.
  2. **Atteso:** "Campo obbligatorio".
  3. Scrivi un motivo → **Conferma rifiuto**.
  4. **Atteso:** "Proposta rifiutata: la segnalazione è tornata all'operatore…"; stato «Riaperto»; in cronologia
     "«azienda» esclusa per questa segnalazione" con il motivo.
  5. Come `operatore.cc`: notifica del rifiuto; in **In carico a me** la segnalazione è **in cima** con "Riaperto" in
     evidenza; nelle compatibili **l'azienda rifiutata non compare più**.

- [ ] **T-5.2 Nuova proposta dopo la riapertura**
  1. Come `operatore.cc` proponi un'altra mansione (come T-4.8).
  2. **Atteso:** stato «Proposta all'azienda» di nuovo.

- [ ] **T-5.3 Accettazione della proposta**
  1. Come `tutor1` → **Accetta la proposta** → **Conferma accettazione**.
  2. **Atteso:** "Proposta accettata. Scadenza del timer: <giorno> <data>"; stato «Proposta accolta»; la scadenza è il
     7° giorno lavorativo dopo oggi (salta sabati, domeniche, festività, patrono e chiusure). La vede anche il Call
     Center, che riceve la notifica dell'accettazione.

- [ ] **T-5.4 Solo il Tutor della segnalazione decide**
  1. Come `tutor2` apri una segnalazione di `tutor1` del progetto GOL con una proposta in corso.
  2. **Atteso:** la vede, ma **senza** "Accetta"/"Rifiuta"/"Appuntamento".

- [ ] **T-5.5 Appuntamento fissato**
  1. Come `tutor1` → **Appuntamento fissato** → scrivi una nota (es. data e ora) → **Conferma appuntamento**.
  2. **Atteso:** "Appuntamento registrato: il timer è fermo."; stato «Appuntamento»; la scadenza non c'è più; in
     cronologia "Appuntamento fissato – Timer fermato" con la nota; il Call Center riceve la notifica.

- [ ] **T-5.6 Appuntamento non andato a buon fine**
  1. → **L'appuntamento non è andato a buon fine** → causa **Altro** senza nota → **Conferma**.
  2. **Atteso:** "Campo obbligatorio" sulla nota.
  3. Scegli la causa "L'azienda non ha confermato il tirocinio" → **Conferma**.
  4. **Atteso:** "Esito registrato…"; stato «Riaperto»; cronologia con la causa; la mansione torna **Libera**; l'azienda
     **resta** tra le compatibili (nessuna esclusione automatica); nello storico dell'azienda "Appuntamento fallito" con
     la causa.

- [ ] **T-5.7 Escludere un'azienda (Sprint 5)**
  1. Come `operatore.cc` apri una segnalazione riaperta e in carico (es. quella di T-5.6) → mansioni compatibili →
     **Dettagli** su una mansione → **Escludi questa azienda** → premi **Conferma esclusione** senza motivo.
  2. **Atteso:** "Campo obbligatorio".
  3. Scrivi un motivo → **Conferma esclusione**.
  4. **Atteso:** la finestra si chiude con l'esito "… esclusa per questa segnalazione"; l'azienda non compare più tra le
     compatibili; sotto, l'elenco **Aziende escluse** la mostra con motivo, autore e data; in cronologia "Azienda esclusa
     dal Call Center" con il motivo; nello storico dell'azienda compare l'esclusione.

## 6. Aziende (Call Center)

- [ ] **T-6.1 Ricerca e nuova azienda**
  1. Come `operatore.cc`: **Aziende** → cerca "Demo" → **Nuova azienda**.
  2. Prova una partita IVA di 10 cifre, poi una già usata, poi una nuova di 11 cifre.
  3. **Atteso:** errori chiari sui primi due casi; con l'ultima l'azienda è creata e si apre la scheda.

- [ ] **T-6.2 Modifica**
  1. Nella scheda cambia referente o telefono → **Salva modifiche**.
  2. **Atteso:** "Modifiche salvate."

- [ ] **T-6.3 Mansioni**
  1. **Aggiungi mansione** (titolo, tipologia, zona) → salva; poi modificane il titolo; poi **Ritira**.
  2. **Atteso:** la mansione compare "Libera", si modifica, poi risulta ritirata; una mansione **Bloccata** non si può
     ritirare.

- [ ] **T-6.4 Disattivazione impedita**
  1. Apri l'azienda con una mansione bloccata (es. quella proposta in T-5.2) → **Disattiva azienda**.
  2. **Atteso:** rifiuto con il motivo: la mansione «…» è bloccata dalla segnalazione n. …

- [ ] **T-6.5 Note dell'azienda**
  1. Sezione **Note** → **Aggiungi nota** vuota, poi con un testo.
  2. **Atteso:** la nota vuota è rifiutata; quella con testo compare in cima con data, ora e autore; non ci sono
     comandi per modificarla o cancellarla.

- [ ] **T-6.6 Storico e pregressi**
  1. Sezione **Storico e pregressi** dell'azienda usata nelle prove.
  2. **Atteso:** riepilogo (proposte, accettate, appuntamenti falliti, tirocini, assunzioni, esiti negativi) ed eventi
     dalla più recente, con il link alla segnalazione; anche le esclusioni con il motivo.

## 7. Notifiche

- [ ] **T-7.1 Campanella e pagina**
  1. Come un utente con notifiche (es. `operatore.cc` dopo la sezione 5): la campanella mostra il numero.
  2. Aprila: elenco dalla più recente, le non lette con "Nuova:".
  3. Apri una segnalazione dal link della notifica, poi torna alle notifiche.
  4. **Atteso:** quella notifica risulta letta; il numero sulla campanella scende.

- [ ] **T-7.2 Segna come lette**
  1. **Segna come letta** su una notifica, poi **Segna tutte come lette**.
  2. **Atteso:** "Tutte le notifiche sono state segnate come lette."; la campanella torna a "nessuna da leggere".

- [ ] **T-7.3 Notifiche nuove senza ricaricare**
  1. Tieni aperta la Home come `tutor1`; in un'altra finestra, come `operatore.cc`, fai una proposta a una
     segnalazione di `tutor1`.
  2. **Atteso:** entro un minuto il numero sulla campanella di `tutor1` sale, senza ricaricare la pagina.

## 8. Calendario (ADMIN)

- [ ] **T-8.1 Patrono**
  1. Come `admin`: **Calendario** → il patrono è 24 giugno; imposta 30 febbraio → **Salva patrono**.
  2. **Atteso:** errore (il giorno non esiste); poi rimetti 24 giugno e salva: "Patrono salvato: 24 giugno."

- [ ] **T-8.2 Chiusure**
  1. Aggiungi una chiusura di un giorno (solo "Dal") e una di un periodo ("Dal" e "Al"), con descrizione.
  2. Prova anche un periodo con "Al" prima di "Dal".
  3. **Atteso:** le chiusure compaiono ordinate; il periodo al contrario è rifiutato; **Rimuovi** le toglie.

- [ ] **T-8.3 Prova del calcolo**
  1. **Prova del calcolo** con la data di oggi → **Calcola**: annota promemoria e scadenza.
  2. Aggiungi una chiusura in un giorno lavorativo tra oggi e la scadenza, poi ricalcola.
  3. **Atteso:** la scadenza si sposta avanti di un giorno lavorativo. Rimuovi poi la chiusura di prova.

- [ ] **T-5.8 Scadenza nella Home del Tutor (Sprint 6)**
  1. Dopo T-5.3 (proposta accettata), come `tutor1` apri la Home: vista **Le mie segnalazioni**, poi **Del mio
     progetto**.
  2. **Atteso:** la colonna **Scadenza** mostra "entro <giorno> <gg/mm>" per **la tua segnalazione**, che è **in cima**;
     le segnalazioni senza timer hanno "—". Dal 4° giorno lavorativo (dopo il promemoria, T-9.1) la scadenza è in
     grassetto, preceduta da "in scadenza".
  3. Come `operatore.cc`: **Atteso:** le viste del Call Center non hanno la colonna.

- [ ] **T-5.9 Appuntamento fallito con rilancio in bacheca (Sprint 6)**
  1. Porta una segnalazione di `tutor1` fino all'appuntamento (T-4.8, T-5.3, T-5.5).
  2. → **L'appuntamento non è andato a buon fine** → scegli una causa e spunta **Chiedi il rilancio della mansione in
     bacheca** (sotto c'è la spiegazione: bozza riservata al progetto, la pubblica il Call Center) → **Conferma**.
  3. **Atteso:** "Esito registrato: … e il rilancio in bacheca è stato chiesto."; nella segnalazione la sezione
     **Rilancio in bacheca** dice "annuncio #N (bozza)", senza link; in cronologia la causa è seguita da "rilancio in
     bacheca richiesto".
  4. Come `operatore.cc`: notifica "Il Tutor chiede di rilanciare in bacheca la mansione…"; nella segnalazione il link
     **Annuncio #N · Stato: Bozza** apre la bozza, con il titolo della mansione e il progetto della segnalazione.
  5. Come `tutor1`: **Atteso:** la bozza **non** è in **Bacheca** finché il Call Center non la pubblica (T-11.3).

## 9. Timer: promemoria e scadenza (Sprint 5)

- [ ] **T-9.1 Promemoria al 4° giorno lavorativo**
  1. Accetta una proposta (T-5.3) e **non** registrare l'appuntamento.
  2. Il **4° giorno lavorativo** dopo l'accettazione (vedi "Prova del calcolo", T-8.3), entro un'ora dall'inizio della
     giornata, controlla le notifiche del Tutor.
  3. **Atteso:** una sola notifica "Promemoria: per la segnalazione n. … manca la registrazione dell'appuntamento…";
     in cronologia "Promemoria del timer" con autore "Sistema". Nelle ore successive il promemoria non si ripete.

- [ ] **T-9.2 Scadenza del timer**
  1. Accetta una proposta (T-5.3) e non registrare l'appuntamento fino alla scadenza mostrata nel dettaglio (7° giorno
     lavorativo).
  2. Il giorno dopo la scadenza, entro un'ora dalla mezzanotte, apri la segnalazione.
  3. **Atteso:** stato «Riaperto», la scadenza non c'è più, la mansione è di nuovo **Libera**; in cronologia "Scadenza del
     timer" con autore "Sistema"; il Tutor e l'operatore hanno la notifica "Il timer … è scaduto…"; la segnalazione è in
     cima a "In carico a me" dell'operatore; l'azienda resta tra le compatibili.
  4. Scorciatoia sulla demo: la segnalazione n. 6 dei dati demo (di `tutor2`, accettata a settembre) è scaduta al primo
     controllo dopo il deploy: verifica stato, cronologia e notifiche di `tutor2` e `operatore.cc`.

## 10. Prove trasversali

- [ ] **T-10.1 Smartphone e app (PWA, US-1301)**
  1. Da Android (Chrome) e iPhone (Safari) apri la demo e installala ("Aggiungi a schermata Home").
  2. Apri l'app installata; poi metti il telefono in modalità aereo e riaprila.
  3. **Atteso:** l'app si apre a tutto schermo; offline compare l'avviso, senza dati di segnalazioni in memoria.

- [ ] **T-10.2 Solo tastiera**
  1. Su un computer, senza mouse, percorri con **Tab**, **Invio**, **Esc** una nuova segnalazione e una proposta
     (finestra "Dettagli" compresa).
  2. **Atteso:** ogni comando è raggiungibile, il punto in cui sei è sempre visibile (bordo evidente), nessuna
     trappola (tranne la finestra "Dettagli", da cui si esce con Esc).

- [ ] **T-10.3 Schermi piccoli e zoom**
  1. Sul telefono in verticale, oppure sul computer con lo zoom del browser al 200%, apri Home, dettaglio di una
     segnalazione, scheda azienda e calendario.
  2. **Atteso:** nessuno scorrimento orizzontale della pagina (le tabelle scorrono dentro il loro riquadro); testi e
     pulsanti leggibili.

- [ ] **T-10.4 Tema scuro**
  1. Imposta il tema scuro del sistema operativo e ricarica.
  2. **Atteso:** la demo passa al tema scuro con testi ben leggibili.

- [ ] **T-10.5 Screen reader (facoltativa)**
  1. Con NVDA (Windows), VoiceOver (Mac/iPhone) o TalkBack (Android), ascolta la Home e invia una segnalazione con un
     campo sbagliato.
  2. **Atteso:** titoli e campi sono annunciati con il loro nome; gli errori e gli esiti delle azioni sono letti
     subito.

## 11. Bacheca (Sprint 6)

Dati demo: l'annuncio **#1** è pubblicato e aperto a tutti, il **#2** è pubblicato e riservato a GOL, il **#3** è una
bozza nata da un rilancio.

- [ ] **T-11.1 Gestione della bacheca (Call Center)**
  1. Come `operatore.cc` → menu **Bacheca**.
  2. **Atteso:** viste **Bozze**, **Pubblicati**, **Archiviati**, con il numero di annunci annunciato; la tabella ha
     numero (#N), titolo, azienda, tipologia, zona, progetto ("Tutti i progetti" se aperto a tutti) e data di
     pubblicazione. La vista resta nell'indirizzo.

- [ ] **T-11.2 Modifica di una bozza**
  1. Apri la bozza **#3** → nei **Dati dell'annuncio** scrivi età minima 30 ed età massima 20 → **Salva le
     modifiche**.
  2. **Atteso:** riepilogo errori con "L'età massima non può essere minore della minima"; nulla è salvato.
  3. Correggi (es. 18 e 29), cambia il **Progetto** e le note → **Salva le modifiche**.
  4. **Atteso:** "Annuncio #3 salvato."; titolo e note riportano l'avviso "visibili solo a Call Center e ADMIN".

- [ ] **T-11.3 Pubblicazione**
  1. Sulla bozza **#3** → **Pubblica** → leggi la domanda → **Sì, pubblica**.
  2. **Atteso:** "Annuncio #3 pubblicato: ora è visibile ai Tutor."; stato «Pubblicato» con la data; "Pubblica" non c'è
     più; il **Progetto** non si cambia più ("si sceglie solo nella bozza").
  3. Se la mansione dell'annuncio è bloccata da una segnalazione o ritirata, **Atteso:** "Operazione non riuscita: …"
     con il motivo.

- [ ] **T-11.4 Annuncio da una mansione libera**
  1. Come `operatore.cc` apri un'azienda → nelle **Mansioni**, accanto a una mansione libera → **Crea annuncio**.
  2. **Atteso:** si apre la scheda **Annuncio #N** con "Bozza dell'annuncio #N creata…"; titolo della mansione,
     progetto "Tutti i progetti".
  3. Torna alla scheda dell'azienda. **Atteso:** accanto alla mansione c'è **Annuncio #N** (link) e non più "Crea
     annuncio"; le mansioni bloccate o ritirate non hanno "Crea annuncio".

- [ ] **T-11.5 Bacheca del Tutor**
  1. Come `tutor1` (solo GOL) → menu **Bacheca**.
  2. **Atteso:** schede degli annunci **pubblicati**: "Annuncio #N · tipologia", zona, "Riservato al tuo progetto GOL"
     oppure "Aperto a tutti i progetti", età, orario, durata, data. **Mai** il nome dell'azienda, il titolo o le note;
     niente bozze né archiviati.
  3. Filtra per zona e per tipologia, poi **Azzera i filtri**. **Atteso:** l'elenco cambia e i filtri restano
     nell'indirizzo.
  4. Sul telefono: **Atteso:** schede leggibili, nessuno scorrimento laterale.
  5. Come `tutor2` (GOL e POLIS) e, se c'è, un Tutor solo POLIS: gli annunci riservati a GOL li vede solo chi è in GOL.

- [ ] **T-11.6 Annuncio legato alla disponibilità della mansione (decisione del 28/10)**
  1. Come `operatore.cc` crea un annuncio da una mansione libera (T-11.4), poi nella scheda dell'azienda **Ritira**
     quella mansione.
  2. **Atteso:** "Mansione «…» ritirata: l'annuncio #N è stato archiviato."; l'annuncio è tra gli **Archiviati**.
  3. Rimetti a disposizione la mansione, crea un nuovo annuncio e pubblicalo; poi **abbina** quella mansione a una
     segnalazione (T-4.8).
  4. **Atteso:** l'annuncio passa tra gli **Archiviati** e il Tutor non lo vede più in bacheca.

- [ ] **T-11.7 Sblocco a tutti dopo 7 giorni**
  1. Come `operatore.cc` apri un annuncio **pubblicato da meno di 7 giorni** e riservato a un progetto (es. T-11.3).
  2. **Atteso:** nessun comando di sblocco; la riga "Riservato al progetto …: si potrà rendere visibile a tutti dal …".
  3. Apri l'annuncio **#2** (riservato a GOL, pubblicato da più di 7 giorni) → **Rendi visibile a tutti** → **Sì,
     rendi visibile a tutti**.
  4. **Atteso:** "Annuncio #2 visibile ai Tutor di tutti i progetti."; stato con "Visibile a tutti dal …"; progetto
     "Tutti i progetti"; un Tutor di un altro progetto ora lo vede come "Aperto a tutti i progetti".

- [ ] **T-11.8 Archiviazione**
  1. Su un annuncio → **Archivia** → **Annulla**. **Atteso:** il punto in cui sei torna sul pulsante "Archivia".
  2. → **Archivia** → **Sì, archivia**.
  3. **Atteso:** "Annuncio #N archiviato."; stato «Archiviato»; dati in sola lettura, nessun comando; il Tutor non lo
     vede più.

## 12. Candidature dalla bacheca e corsia veloce (Sprint 7)

- [ ] **T-12.1 Candidatura di un beneficiario (US-306)**
  1. Come `tutor1` → **Bacheca** → su un annuncio pubblicato → **Candida un beneficiario**.
  2. **Atteso:** pagina "Candidatura all'annuncio #N"; tra i beneficiari solo i tuoi **senza** una segnalazione aperta
     (gli altri compaiono disattivati con il numero della segnalazione aperta); se l'annuncio è riservato a un
     progetto, il progetto è fisso, altrimenti si sceglie ed è obbligatorio.
  3. Scegli il beneficiario → **Invia la candidatura**.
  4. **Atteso:** "Candidatura inviata: segnalazione n. N" con il link alla segnalazione; in **Bacheca** l'annuncio
     mostra «Candidatura in valutazione» e non si può più candidare.
  5. Come `tutor2` (se l'annuncio è visibile anche a lui): **Atteso:** «Candidatura in valutazione», nessun pulsante.

- [ ] **T-12.2 Corsia veloce approvata (US-403)**
  1. Come `operatore.cc` → **Segnalazioni** → **Da prendere in carico**. **Atteso:** la segnalazione della candidatura
     è in cima (speciale, con la corsia veloce).
  2. Apri la segnalazione. **Atteso:** sezione "Corsia veloce: candidatura all'annuncio #N" con mansione e azienda
     dell'annuncio; prima della presa in carico si vede ma non si decide.
  3. **Prendi in carico** → **Approva la corsia veloce** → leggi la domanda → **Sì, approva e proponi**.
  4. **Atteso:** "Corsia veloce approvata: proposta inviata a … per «…»"; la segnalazione è in «Proposta
     all'azienda»; l'annuncio è tra gli **Archiviati**; `tutor1` ha la notifica della proposta.

- [ ] **T-12.3 Corsia veloce rifiutata (US-404)**
  1. Ripeti T-12.1 con un altro annuncio e un altro beneficiario.
  2. Come `operatore.cc`, presa in carico la segnalazione → **Rifiuta la corsia veloce** → conferma senza motivo.
  3. **Atteso:** il motivo è obbligatorio.
  4. Scrivi il motivo → **Sì, rifiuta**.
  5. **Atteso:** "Corsia veloce rifiutata: la segnalazione continua in lavorazione normale e l'annuncio #N torna
     candidabile."; ora c'è la ricerca delle mansioni compatibili; `tutor1` ha la notifica e legge il motivo nella
     cronologia; in **Bacheca** l'annuncio è di nuovo candidabile.

- [ ] **T-12.4 Annuncio archiviato con una candidatura in attesa (decisione del 28/10)**
  1. Ripeti T-12.1; poi come `operatore.cc` apri l'annuncio → **Archivia**.
  2. **Atteso:** la domanda avvisa che la segnalazione n. N perde la corsia veloce e continua in lavorazione normale.
  3. **Sì, archivia**. **Atteso:** la segnalazione resta in lavorazione senza corsia veloce; `tutor1` riceve la notifica.

## 13. Cataloghi (ADMIN, Sprint 7)

- [ ] **T-13.1 Zone e tipologie di mansione (US-106)**
  1. Come `admin` → menu **Cataloghi**. **Atteso:** tre sezioni: Zone, Tipologie di mansione, Tipi di tirocinio.
  2. **Aggiungi zona** con un nome nuovo. **Atteso:** compare tra le attive, con il codice generato.
  3. Aggiungi una zona con lo stesso nome (anche con maiuscole diverse). **Atteso:** errore sul nome, nulla è salvato.
  4. Cambia il nome di una voce, poi **Disattiva**. **Atteso:** "«…» disattivata: non si può più scegliere, resta sui
     dati che la usano."; nel modulo del beneficiario o della mansione la voce non è più tra le scelte.
  5. **Riattiva**. **Atteso:** "«…» riattivata." e torna selezionabile.

- [ ] **T-13.2 Tipi di tirocinio (US-108)**
  1. Sempre in **Cataloghi** → **Tipi di tirocinio**. **Atteso:** Curriculare, Extracurriculare, Di inclusione, Di
     inserimento socio-terapeutico.
  2. Aggiungi un tipo, poi disattivalo. **Atteso:** nella scheda del tirocinio (sezione 14) non si può più scegliere.

- [ ] **T-13.3 Solo per l'ADMIN**
  1. Come `operatore.cc` e come `tutor1`. **Atteso:** nel menu non c'è **Cataloghi**; all'indirizzo
     `/cataloghi` compare "Accesso non consentito".

- [ ] **T-13.4 Voci del report di restituzione (US-109, Sprint 8)**
  1. Come `admin` → **Cataloghi**. **Atteso:** due sezioni in più, **Formule di assunzione** (tempo determinato 3, 6
     e 12 mesi, tempo indeterminato, contratto a chiamata, prestazione occasionale) e **Motivi della mancata
     assunzione** (tre voci).
  2. Disattiva un motivo. **Atteso:** nel report di restituzione (T-17.1) non si può più scegliere.

- [ ] **T-13.5 Progetti (US-102, Sprint 9)**
  1. Come `admin` → **Cataloghi**. **Atteso:** in cima la sezione **Progetti**, con GOL e POLIS.
  2. Aggiungi il progetto «Prova collaudo». **Atteso:** compare attivo, con il codice `PROVA_COLLAUDO`. Aggiungerlo
     una seconda volta, anche in minuscolo, è rifiutato: «è già nel catalogo».
  3. Disattiva **POLIS**. **Atteso:** come `tutor2` (GOL e POLIS), nella nuova segnalazione POLIS non c'è più; le
     segnalazioni POLIS già fatte si aprono ancora e mostrano il progetto.
  4. Riattiva **POLIS**. **Atteso:** torna sceglibile nella nuova segnalazione.

## 14. Dati dell'azienda e scheda del tirocinio (Sprint 7)

- [ ] **T-14.1 Legale rappresentante dell'azienda (US-506)**
  1. Come `operatore.cc` apri un'azienda. **Atteso:** sezione **Legale rappresentante** con "Non ancora indicato." e
     il pulsante **Indica il legale rappresentante**.
  2. Inserisci nome, cognome e il codice fiscale `RSSMRA85T10A562T` → **Salva**. **Atteso:** "Codice fiscale non
     valido" (il carattere finale è sbagliato).
  3. Correggi in `rssmra85t10a562s` (minuscolo) → **Salva**. **Atteso:** "Legale rappresentante salvato."; codice in
     maiuscolo; i dati mancanti compaiono come "Non indicato".

- [ ] **T-14.2 Codici ATECO dell'azienda (US-506)**
  1. Sulla stessa azienda → **Aggiungi codice** → `5610`. **Atteso:** errore "Formato: 56, 56.10 oppure 56.10.11".
  2. `56.10.11` con una descrizione. **Atteso:** "Codice 56.10.11 aggiunto."; di nuovo lo stesso codice → errore.
  3. Aggiungi un secondo codice e **Togli**. **Atteso:** "Codice … tolto."

- [ ] **T-14.3 Chi vede la scheda del tirocinio (US-1001)**
  1. Come `tutor1`, su una tua segnalazione con l'**appuntamento fissato** (T-5.x). **Atteso:** sezione **Scheda del
     tirocinio** con "la scheda la vedi solo tu" e **Apri la scheda del tirocinio**.
  2. Come `operatore.cc` e come `tutor2` sulla stessa segnalazione. **Atteso:** nessun collegamento alla scheda;
     all'indirizzo della scheda il Call Center ha "Accesso non consentito".
  3. Su una segnalazione **prima** dell'appuntamento fissato. **Atteso:** nessun collegamento.

- [ ] **T-14.4 Dati dell'azienda dalla scheda**
  1. Apri la scheda (T-14.3). **Atteso:** titolo "Scheda del tirocinio · segnalazione n. N", beneficiario e azienda; in
     alto **Dati dell'azienda per i documenti**.
  2. Se l'azienda non ha il legale rappresentante: **Inserisci il legale rappresentante** → salva. **Atteso:**
     "Legale rappresentante inserito nella scheda dell'azienda."; poi il pulsante non c'è più (correggerlo spetta al
     Call Center).
  3. **Aggiungi un codice ATECO** (es. `56.10`). **Atteso:** compare nell'elenco e tra le scelte del modulo; come
     `operatore.cc` è anche nella scheda dell'azienda.

- [ ] **T-14.5 Compilazione e controlli della scheda (US-1001)**
  1. Compila tipo di tirocinio, date (dal 01/12/2026 al 31/05/2027), sede, scegli il codice ATECO; aggiungi un tutor
     aziendale.
  2. Aggiungi due periodi di sospensione che si sovrappongono (es. 22/12–06/01 e 06/01–10/01) → **Salva la scheda**.
     **Atteso:** riepilogo "Periodo 2: dal: Si sovrappone al periodo 1".
  3. Correggi il secondo periodo (dal 07/01). Codice fiscale `RSSMRA85T10A562S` con nascita 11/12/1985 → salva.
     **Atteso:** "Il codice fiscale non corrisponde alla data di nascita".
  4. Nascita 10/12/1985 → salva. **Atteso:** "Scheda del tirocinio salvata."; ricaricando, i dati ci sono.
  5. Nella segnalazione → cronologia. **Atteso:** "Scheda del tirocinio aggiornata", senza dati (anche per il Call
     Center).
  6. Come `operatore.cc`, nell'azienda → **Togli** il codice ATECO scelto nella scheda. **Atteso:** "Il codice … è
     scelto nella scheda del tirocinio della segnalazione n. N", il codice resta.

- [ ] **T-14.6 Griglia oraria e monte ore (US-1002)**
  1. Nella scheda → **Griglia oraria e monte ore**. **Atteso:** fasce precompilate 09:00–13:00 e 14:00–18:00, casella
     "Escludi dal monte ore le festività nazionali" attiva.
  2. Spunta la prima fascia da lunedì a venerdì e, per il mercoledì, anche la seconda con **6** ore. **Atteso:**
     "Totale settimanale: 22 ore", aggiornato mentre spunti.
  3. Scrivi **5** nelle ore del lunedì → salva. **Atteso:** "Ore di lunedì: Al massimo 4 ore nelle fasce scelte".
  4. Svuota le ore del lunedì → salva. **Atteso:** "Monte ore complessivo: … ore", che tiene conto di date,
     sospensioni e festività (con 01/12/2026–31/05/2027 e le sospensioni di T-14.5: **502 ore**).
  5. Togli la spunta alle festività → salva. **Atteso:** il monte ore cresce.
  6. Cambia la prima fascia in 08:30–12:30 → **Atteso:** le intestazioni e il totale settimanale si aggiornano.

- [ ] **T-14.7 Appuntamento fallito dopo la scheda**
  1. Sulla segnalazione con la scheda compilata → **L'appuntamento non è andato a buon fine** (T-5.x). **Atteso:**
     il testo avvisa che della scheda restano solo i dati del beneficiario.
  2. Quando la segnalazione avrà un nuovo appuntamento fissato, apri la scheda. **Atteso:** codice fiscale, nascita e
     residenza ci sono; tipo, date, sede, ATECO, tutor, sospensioni, griglia e note sono vuoti.

## 15. Pagina di accesso (Sprint 7)

- [ ] **T-15.1 Aspetto del sito**
  1. Esci e apri https://portalenexus.it.
  2. **Atteso:** pagina "Accedi a NEXUS" con la barra verde e il filetto oro, il logo, "NEXUS · Area Lavoro e Call
     Center Sociale", il titolo con il filetto oro, il pulsante "Accedi" verde e il piè di pagina del sito.
  3. Con il telefono o il computer in **modalità scura**. **Atteso:** la pagina passa ai colori scuri del sito.
  4. Sul telefono. **Atteso:** nessuno scorrimento laterale, campi e pulsante comodi da toccare.

- [ ] **T-15.2 Solo italiano ed errore di accesso**
  1. **Atteso:** nessun selettore della lingua; anche con il browser in inglese la pagina resta in italiano.
  2. Entra con un utente esistente e una password sbagliata. **Atteso:** "Nome utente o password non validi." in
     rosso, con il campo evidenziato.
  3. Entra con le credenziali giuste. **Atteso:** si apre la Home di NEXUS.

## 16. Avvio del tirocinio e documenti (Sprint 8)

- [ ] **T-16.1 Scheda incompleta (US-1003)**
  1. Come `tutor1`, apri la scheda di una segnalazione con l'appuntamento fissato e la scheda non ancora compilata.
     **Atteso:** sezione **Avvio del tirocinio** con l'elenco di ciò che manca; ogni voce porta al campo.
  2. Se il legale rappresentante dell'azienda non ha codice fiscale, nascita e residenza. **Atteso:** la voce dice di
     chiedere al Call Center di completarli.

- [ ] **T-16.2 Avvio con la scheda completa (US-1003)**
  1. Completa la scheda (T-14.5, T-14.6) e salva. **Atteso:** "La scheda è completa: puoi avviare il tirocinio."
  2. **Avvia il tirocinio** → conferma. **Atteso:** la segnalazione è «In tirocinio»; la scheda resta modificabile.
  3. Come `operatore.cc`. **Atteso:** notifica dell'avvio; nell'azienda, **Storico e pregressi** con "Tirocinio
     avviato".

- [ ] **T-16.3 Convenzione e Progetto Formativo (US-1004, US-1005)**
  1. Nella scheda → **Documenti** → **Scarica la Convenzione (.docx)**. **Atteso:** si scarica
     `Convenzione-segnalazione-N.docx`, compilato con i dati della scheda (per ora un modello provvisorio).
  2. **Scarica il Progetto Formativo (.docx)**. **Atteso:** `Progetto-formativo-segnalazione-N.docx` con orari, monte
     ore e tutor aziendali.
  3. Con un dato mancante nella scheda. **Atteso:** l'avviso che i dati mancanti restano da completare a mano; nel
     documento al loro posto c'è una riga `____________`.
  4. Nella segnalazione → cronologia. **Atteso:** "Documento scaricato" con il nome del documento.

## 17. Report di restituzione e chiusura (Sprint 8)

- [ ] **T-17.1 Compilazione e invio (US-1101)**
  1. Come `tutor1`, su una segnalazione «In tirocinio» → **Compila il report di restituzione**. **Atteso:** pagina
     "Report di restituzione della segnalazione n. N" con le due soddisfazioni da 1 a 5, assunto sì/no, mansione ancora
     disponibile sì/no, obiettivi e difficoltà (facoltativi).
  2. **Invia il report** senza scegliere nulla. **Atteso:** riepilogo degli errori con i campi obbligatori.
  3. Rispondi **Sì** all'assunzione. **Atteso:** compare **Formula di assunzione**; con **No** compare invece **Motivo
     della mancata assunzione**.
  4. Compila e **Invia il report**. **Atteso:** "Confermi l'invio del report?" e i campi bloccati; **Torna al modulo**
     li riapre.
  5. **Sì, invia il report**. **Atteso:** "Report inviato: il percorso della segnalazione n. N è concluso." e il
     report in sola lettura.

- [ ] **T-17.2 Dopo l'invio (US-1101)**
  1. Nella segnalazione. **Atteso:** stato «Report di restituzione», riquadro del report, nessun collegamento per
     compilarlo di nuovo; in cronologia "Report di restituzione inviato" con l'esito.
  2. Come `tutor2` (stesso progetto) e come `operatore.cc`. **Atteso:** vedono il report; l'operatore ha la notifica.
  3. Come `tutor1`, crea una nuova segnalazione per lo stesso beneficiario. **Atteso:** è possibile, perché il
     percorso precedente è concluso.

- [ ] **T-17.3 Mansione e storico dell'azienda (US-1102)**
  1. In un report rispondi **Sì** a "La mansione è ancora disponibile?". **Atteso:** come `operatore.cc` la mansione
     è di nuovo **libera** e torna tra le compatibili; nell'azienda, **Storico e pregressi** con "Tirocinio concluso"
     e "Con assunzione: «…»; mansione di nuovo libera" (o "Senza assunzione: «…»").
  2. In un altro report rispondi **No**. **Atteso:** la mansione resta bloccata; lo storico dice "mansione non più
     disponibile".
  3. **Atteso:** il riepilogo dello storico conta assunzioni ed esiti negativi.

## 18. Email delle notifiche (Sprint 8)

Servono la password SMTP nel `.env` del server e un indirizzo vero per un utente demo (dal profilo, sezione 19).

- [x] **T-18.1 Email ricevuta (US-1202)**
  1. Dai il tuo indirizzo a `operatore.cc` dal suo profilo (T-19.1). Come `tutor1` fai un passaggio che lo notifica
     (per esempio accetta una proposta). **Atteso:** entro pochi secondi arriva un'email da `noreply@portalenexus.it` con oggetto
     «PORTALE NEXUS - Segnalazione n. N», lo stesso testo della notifica in app e il collegamento alla segnalazione.
  2. **Atteso:** nell'email non ci sono nomi né altri dati del beneficiario.
  3. Il collegamento apre la segnalazione (dopo l'accesso).

- [ ] **T-18.2 Email spente senza password**
  1. Prima di aggiungere la password. **Atteso:** l'app funziona normalmente, nessuna email; il log del backend
     segnala "Email delle notifiche spente: manca la password SMTP".

## 19. Il mio profilo e cambio dell'indirizzo email (Sprint 9)

Servono la password SMTP e il client tecnico di Keycloak sul server (README, «Demo online»). Usa un tuo indirizzo
vero; per l'avviso al vecchio indirizzo, parti da un utente che ha già un indirizzo vero.

- [x] **T-19.1 Cambio dell'indirizzo (US-110)**
  1. Accedi come `operatore.cc` e attiva il tuo nome nell'intestazione (su mobile: nel menu). **Atteso:** si apre
     «Il mio profilo» con nome, ruolo e indirizzo email attuale.
  2. Scrivi il tuo indirizzo in «Nuovo indirizzo email» e premi «Invia il codice». **Atteso:** arriva un'email da
     `noreply@portalenexus.it` con un codice di 6 cifre e l'ora di scadenza (30 minuti); la pagina chiede il codice.
  3. Scrivi il codice e premi «Conferma il nuovo indirizzo». **Atteso:** «Indirizzo email aggiornato» e il profilo
     mostra il nuovo indirizzo.
  4. Esci e accedi scrivendo il **nuovo indirizzo** al posto del nome utente. **Atteso:** l'accesso funziona (anche
     Keycloak è aggiornato).

- [ ] **T-19.2 Codice sbagliato, nuovo codice, annullamento**
  1. Chiedi un altro cambio e scrivi un codice sbagliato. **Atteso:** «Codice non valido: 4 tentativi rimasti»,
     l'indirizzo non cambia.
  2. Premi subito «Invia un nuovo codice». **Atteso:** messaggio di attesa (puoi chiederne un altro tra N secondi).
     Dopo un minuto riprova. **Atteso:** arriva un nuovo codice; il primo non vale più.
  3. Premi «Annulla il cambio». **Atteso:** «Cambio annullato», l'indirizzo resta quello di prima.

- [x] **T-19.3 Avviso al vecchio indirizzo**
  1. Cambia di nuovo l'indirizzo di un utente che ne ha già uno vero. **Atteso:** al vecchio indirizzo arriva
     «PORTALE NEXUS - Il tuo indirizzo email è stato cambiato», senza il nuovo indirizzo.

- [x] **T-19.4 Indirizzo già usato**
  1. Come `tutor1`, prova a passare all'indirizzo che hai dato a `operatore.cc`. **Atteso:** errore sul campo
     «Indirizzo già usato da un altro utente», nessun codice inviato.

## 20. Utenti (ADMIN, Sprint 9)

Serve la posta di Keycloak sul server (README, «Demo online», `keycloak-email.sh`). Usa un tuo indirizzo vero per il
nuovo utente, per esempio un alias.

- [x] **T-20.1 Nuovo utente (US-103)**
  1. Come `admin`: nel menu c'è **Utenti**. **Atteso:** l'elenco con gli utenti demo, ruolo e stato.
  2. **Nuovo utente**: scrivi nome e cognome. **Atteso:** lo username si propone da solo come `nome.cognome`.
  3. Completa email e ruolo **Tutor** e crea. **Atteso:** torni all'elenco con «Utente … creato» e il nuovo utente
     compare in elenco.
  4. **Atteso:** all'indirizzo arriva da Keycloak l'email per impostare la password. Apri il collegamento, imposta la
     password. **Atteso:** entri in NEXUS come Tutor.
  5. Nella console di Keycloak (tunnel) l'utente c'è con il ruolo TUTOR (facoltativo).

- [ ] **T-20.2 Dati già usati e campi obbligatori**
  1. Crea un utente con username `tutor1`. **Atteso:** errore sul campo «Username già usato da un altro utente».
  2. Crea un utente con l'email di un utente esistente. **Atteso:** errore sul campo Email.
  3. Invia il modulo vuoto. **Atteso:** riepilogo degli errori con i campi obbligatori.

- [ ] **T-20.3 Nuovo invito**
  1. Nell'elenco, **Invia di nuovo l'invito** sull'utente creato. **Atteso:** «Invito inviato a …» e una nuova
     email; il collegamento precedente non serve più.

- [ ] **T-20.4 Solo l'ADMIN**
  1. Come `operatore.cc` apri `/utenti`. **Atteso:** «Pagina non consentita»; la voce Utenti non è nel menu.

- [ ] **T-20.5 Progetti di un Tutor (US-104)**
  1. Come `admin`, in **Utenti**: la colonna **Progetti** mostra i progetti dei Tutor (`tutor2`: GOL, POLIS) e
     «Tutti» per Call Center e ADMIN; il pulsante **Progetti** c'è solo sui Tutor.
  2. **Progetti** sul Tutor creato in T-20.1 (o su `tutor1`): seleziona POLIS e salva. **Atteso:** torni all'elenco con
     «Progetti di … aggiornati» e POLIS nella colonna.
  3. Entra con quel Tutor. **Atteso:** in **Nuova segnalazione** il progetto POLIS è tra quelli scelti; nella vista
     dei ticket del progetto compaiono quelli di POLIS, in sola lettura.
  4. Togli POLIS e salva; il Tutor ricarica la pagina. **Atteso:** i ticket di POLIS degli altri Tutor non si vedono
     più; i suoi restano.
  5. Disattiva un progetto nei **Cataloghi** e apri i progetti di un Tutor che non lo ha. **Atteso:** il progetto
     disattivato non compare; su un Tutor che lo ha, compare come «disattivato» e si può solo togliere.

- [ ] **T-20.6 Spostamento delle segnalazioni (US-111)**
  1. Come `admin`, in **Utenti**, pulsante **Segnalazioni** su `tutor1`. **Atteso:** le sue segnalazioni aperte per
     progetto, tutte già selezionate; le chiuse non ci sono.
  2. Su GOL scegli `tutor2` come Tutor che le riceve, togli una segnalazione dalla selezione e sposta. **Atteso:**
     «1 segnalazione spostata a …» e la segnalazione sparisce dall'elenco.
  3. Entra come `tutor2`. **Atteso:** la notifica «Ti è stata affidata la segnalazione n. …», anche per email; la
     segnalazione è tra le sue, con il beneficiario tra i suoi; nella cronologia «Passata a un altro Tutor: … → …».
  4. Su un progetto senza altri Tutor assegnati. **Atteso:** il messaggio che invita ad assegnare il progetto a un
     collega, senza pulsante.

- [ ] **T-20.7 Disattivazione, riattivazione e cambio di ruolo (US-105)**
  1. Come `admin`, **Accesso e ruolo** su un Tutor con segnalazioni aperte (es. `tutor1`). **Atteso:** l'avviso «Ha
     N segnalazioni aperte…» con il collegamento **Sposta le segnalazioni**, senza pulsanti.
  2. **Accesso e ruolo** su `operatore.cc` con ticket in carico. **Atteso:** l'avviso «Ha N ticket in carico…».
     **Disattiva l'utente**. **Atteso:** torni all'elenco con «… è disattivato»; in un'altra finestra `operatore.cc`
     non riesce più a entrare.
  3. Come `admin`, vista **Da prendere in carico**. **Atteso:** i ticket di `operatore.cc` sono in coda, anche quelli
     più avanti (es. ad appuntamento fissato); prendine uno: resta nella sua fase. In cronologia «Tornata in coda».
  4. Riattiva `operatore.cc`. **Atteso:** entra di nuovo con la sua password.
  5. Cambia il ruolo del Tutor creato in T-20.1 in Call Center. **Atteso:** «Il ruolo … ora è Call Center»; al suo
     prossimo accesso vede la Home del Call Center. Poi riportalo a Tutor.
  6. **Accesso e ruolo** sul tuo utente `admin`. **Atteso:** il messaggio che lo cambia un altro ADMIN, senza pulsanti.

- [ ] **T-20.8 Coordinatore di progetto (US-112)**
  1. Come `admin`, **Progetti** di `tutor1`: accanto a GOL spunta **Coordinatore** e salva (lascia POLIS senza).
     **Atteso:** nell'elenco degli utenti «GOL (Coordinatore), POLIS»; la casella c'è solo per i progetti assegnati.
  2. Entra come `tutor1` (o ricarica la pagina). **Atteso:** nel menu la voce **Coordinamento**; la pagina mostra
     tutte le segnalazioni aperte di GOL, anche di `tutor2`, con il Tutor di ognuna, e nessuna già selezionata.
  3. Scegli una segnalazione di `tutor2` e come Tutor che la riceve te stesso; sposta. **Atteso:** «1 segnalazione
     spostata a …»; nella pagina la segnalazione ora è tua; `tutor2` vede in cronologia «Passata a un altro Tutor».
  4. Entra come `tutor2`. **Atteso:** nel menu non c'è **Coordinamento**.
  5. Come `admin`, togli la spunta **Coordinatore** a `tutor1`. **Atteso:** al prossimo accesso di `tutor1` la voce
     **Coordinamento** non c'è più.

## 21. Accesso e sicurezza (Sprint 10)

Serve la posta di Keycloak sul server (`keycloak-email.sh`) e, una volta, `keycloak-password-reset.sh` (README,
«Demo online»).

- [x] **T-21.1 Password dimenticata (US-1308)**
  1. Esci da NEXUS. Nella pagina di accesso **Atteso:** sotto il modulo c'è **Password dimenticata?**, con l'aspetto di
     NEXUS.
  2. Aprilo, scrivi l'email di un tuo utente (es. quello creato in T-20.1) e invia. **Atteso:** il messaggio che
     arriverà un'email; nella casella un'email di NEXUS (`noreply@portalenexus.it`) con il collegamento, che scade in
     30 minuti.
  3. Apri il collegamento e imposta una nuova password (almeno 12 caratteri). **Atteso:** entri in NEXUS; la vecchia
     password non funziona più.
  4. Nella casella **Atteso:** un'altra email di NEXUS: «<tuo username>, le tue credenziali sono state cambiate il
     <data> alle <ora italiana> da questo indirizzo IP: …», con l'invito a contattare l'amministratore. Una password
     sbagliata all'accesso invece non manda email.
  5. **Atteso:** tutte le email ricevute (collegamento, avviso, e anche le notifiche di NEXUS) hanno l'oggetto che inizia
     con «PORTALE NEXUS - » (es. «PORTALE NEXUS - Reimposta la password») e finiscono con la firma «Portale Nexus».
  6. Chiedi un altro collegamento e aprilo dopo più di 30 minuti. **Atteso:** Keycloak dice che è scaduto e propone di
     chiederne uno nuovo.

- [x] **T-21.2 Registro degli accessi (US-113)**

  Serve, una volta dopo il deploy, `keycloak-access-log.sh` (README). Gli accessi di prima non ci sono.
  1. Esci ed entra come un tuo utente; poi esci e prova con la password sbagliata. Entra di nuovo.
  2. In **Il mio profilo**, sezione **I miei accessi**. **Atteso:** dal più recente, «Riuscito» e «Non riuscito:
     password sbagliata», con data, ora italiana e il tuo indirizzo IP.
  3. Come `admin`, in **Utenti**, pulsante **Accessi** su quell'utente. **Atteso:** le stesse righe.
  4. Esci e prova ad accedere con uno username che non esiste (es. `nessuno.cosi`). Come `admin`, **Utenti** →
     **Accessi non riusciti**. **Atteso:** la riga con «Utente inesistente», `nessuno.cosi` nella colonna «Scritto nel
     modulo», data, ora e IP; anche la password sbagliata del punto 1, con il nome dell'utente.
  5. Come Tutor, apri l'indirizzo `/utenti/accessi-falliti`. **Atteso:** la pagina non è disponibile per il tuo ruolo.

- [x] **T-21.3 Anonimizzazione a scadenza (US-1303)**

  Nei dati demo c'è «Demo Otto Beneficiaria» di `tutor1`, fermo da 3 anni: la notte dopo il deploy (alle 2:30) il
  controllo lo segna «Da anonimizzare».
  1. Come `tutor1`, **Beneficiari**. **Atteso:** la riga di «Demo Otto» evidenziata con «Da anonimizzare»; la casella
     **Solo da anonimizzare (1)** lascia solo lei.
  2. **Tieni i dati** e conferma senza motivo. **Atteso:** «Scrivi il motivo…». Chiudi con **Annulla**.
  3. Come `admin`, menu **Beneficiari**. **Atteso:** tutti i beneficiari con il Tutor; «Demo Otto» evidenziata, «Dal
     …» con la data del segno.
  4. **Anonimizza** → **Anonimizza definitivamente**. **Atteso:** «… è stato anonimizzato»; la riga ora è «O B»
     (iniziali) con «Anonimizzato». Come `tutor1`: nella scheda non c'è più **Modifica** e in **Nuova segnalazione**
     non compare tra i beneficiari.
  5. (Facoltativo, se c'è un altro beneficiario da anonimizzare) **Tieni i dati** con un motivo. **Atteso:** «Dati
     tenuti» e, per l'ADMIN, «Motivo: …».

- [x] **T-21.4 Controllo della demo e revisione di sicurezza (US-1307)**
  1. Su GitHub, **Settings → Notifications → System → Actions**: le notifiche dei workflow falliti arrivano per email
     (impostazione dell'account).
  2. **Actions → Controllo della demo → Run workflow** su `main`. **Atteso:** run verde con «Demo online» e i giorni di
     validità dei certificati. Nelle ore 8–20 compare da solo ogni 10 minuti; fuori orario i run sono verdi e dicono
     «Fuori dalla fascia 8–20».
  3. Leggi `docs/security/asvs-l1.md`. Per L2: **Settings → Security → Code security → Dependabot alerts → Enable**
     (solo avvisi, nessuna PR automatica).

- [x] **T-21.5 Menu dell'ADMIN (08/11)**
  1. Come `admin`, su un computer. **Atteso:** in alto Segnalazioni, Beneficiari, Aziende, Bacheca e **Amministrazione
     ▾**; niente si sovrappone a campanella, nome ed Esci.
  2. Apri **Amministrazione**. **Atteso:** Utenti, Cataloghi e Calendario; Esc o un clic fuori lo chiudono. Su una
     pagina di Utenti, Cataloghi o Calendario la voce Amministrazione è evidenziata.
  3. Restringi la finestra sotto i 1024 px (o da tablet). **Atteso:** il menu passa al pulsante ☰, con il gruppo
     «Amministrazione» già aperto.

- [ ] **T-21.6 Dipendenze aggiornate (EN-6)**
  1. Su GitHub, **Security → Dependabot**. **Atteso:** nessun avviso aperto (`deepmerge` archiviato).
  2. Il lunedì dopo il merge, **Pull requests**. **Atteso:** le PR di Dependabot («chore(deps): …»), le versioni minori
     raggruppate per ecosistema; ognuna con la CI verde prima del merge.
  3. Dopo ogni merge di Dependabot: deploy e controllo della demo, come per le altre PR.

- [x] **T-21.7 Password comuni rifiutate (EN-7)**

  Dopo il deploy e `keycloak-password-policy.sh` (README).
  1. Esci; nella pagina di accesso **Password dimenticata?** per un tuo utente e apri il collegamento dell'email.
  2. Come nuova password scrivi `Password1234` (due volte). **Atteso:** «Password troppo comune: è tra quelle più
     usate e facili da indovinare. Scegline un'altra.» e la pagina resta aperta.
  3. Scegli una password tua, lunga e non banale. **Atteso:** accettata, entri in NEXUS e arriva l'email di sicurezza.

- [x] **T-21.8 Curriculum del beneficiario (US-205)**
  1. Come `tutor1`, apri un tuo beneficiario. **Atteso:** la sezione **Curriculum** con «Nessun CV caricato.» e
     **Carica il CV** (solo PDF, al massimo 5 MB).
  2. Prova con un file che non è un PDF (es. una foto). **Atteso:** «Il CV deve essere un file PDF.»
  3. Carica un PDF. **Atteso:** «CV caricato.», nome e dimensione del file; **Scarica il CV** scarica lo stesso file.
  4. Come `operatore.cc` (o `admin`) apri lo stesso beneficiario. **Atteso:** il CV si scarica, ma non si carica né si
     toglie.
  5. Come `tutor1`, **Sostituisci** con un altro PDF, poi **Togli il CV**. **Atteso:** «CV sostituito.», poi «CV
     tolto.» e di nuovo «Nessun CV caricato.»

## 22. Cataloghi per i documenti reali (Sprint 12)

- [ ] **T-22.1 Comuni e province (US-114)**
  1. Come `admin`, **Amministrazione → Cataloghi**, sezione **Comuni e province**. **Atteso:** nella demo l'elenco è
     già caricato: circa 7900 comuni e 107 province validi oggi, ultimo caricamento valido dal 01/01/2026.
  2. In **Cerca un comune** scrivi `sesto f`. **Atteso:** «Sesto Fiorentino (FI) — Firenze, codice ISTAT 048043, valido
     dal 01/01/2026».
  3. Scarica dal sito dell'ISTAT il file «Elenco dei comuni italiani» (CSV) con il collegamento della sezione. Sceglilo,
     lascia la data di oggi e premi **Controlla il file**. **Atteso:** l'anteprima con i comuni del file e le
     differenze (se l'ISTAT non ha pubblicato variazioni dal 09/11: «Nessuna differenza»).
  4. Prova con un altro file CSV (es. un elenco qualsiasi). **Atteso:** il motivo del rifiuto, per esempio «Il file non
     è l'elenco dei comuni dell'ISTAT: manca la colonna …»; l'elenco non cambia.
  5. Se l'anteprima mostra differenze, premi **Conferma il caricamento**. **Atteso:** «Elenco caricato, valido dal …» e
     lo stato aggiornato; i comuni cambiati compaiono nella ricerca con la versione vecchia («valido dal … al …») e
     quella nuova.

- [ ] **T-22.2 Sedi di competenza, tipi per progetto, enti di presa in carico (US-115)**
  1. Come `admin`, **Amministrazione → Cataloghi**. **Atteso:** le sezioni «Tipi di tirocinio per progetto» (GOL: «Di
     inclusione, Extracurriculare»; gli altri progetti: «Tutti i tipi»), «Sedi di competenza» (Firenze, Borgo San
     Lorenzo, Sesto Fiorentino, con GOL) ed «Enti di presa in carico» (i quattro enti con la tipologia).
  2. Nelle sedi, **Progetti** di «Firenze»: aggiungi POLIS e salva. **Atteso:** «Progetti di «Firenze» salvati.» e
     POLIS nella colonna Progetti. Aggiungi una sede nuova, poi disattivala.
  3. Negli enti, **Dati** di un ente: compila il referente con una mail sbagliata e salva. **Atteso:** l'errore sul
     campo. Correggi e salva. **Atteso:** «Dati di «…» salvati.»
  4. Come `tutor1`, apri la scheda del tirocinio di una segnalazione di GOL (ad esempio quella in tirocinio). **Atteso:**
     nel tipo di tirocinio solo «Di inclusione» ed «Extracurriculare», più il tipo già scelto se era un altro.

- [ ] **T-22.3 Contatti, ente di presa in carico e case manager (US-206)**
  1. Come `tutor1`, **Modifica** di un tuo beneficiario. **Atteso:** la sezione «Contatti» (mail, telefono) e, nelle
     informazioni riservate, «Presa in carico» con l'ente e i quattro dati del case manager, tutti facoltativi.
  2. Scrivi una mail sbagliata nel case manager e salva. **Atteso:** l'errore sul campo. Correggi, scegli «Comune di
     Firenze», compila nome, mail e telefono del case manager e salva. **Atteso:** la scheda li mostra.
  3. Apri la scheda del tirocinio di una segnalazione di GOL di un beneficiario **senza** ente. **Atteso:** «Di
     inclusione (non disponibile)» non si può scegliere e sotto c'è l'elenco dei dati mancanti. Facoltativo, con lo
     screen reader: sul campo «Tipo di tirocinio» si sente anche l'elenco dei dati mancanti.
  4. Con il beneficiario del punto 2, scegli «Di inclusione» nella scheda del tirocinio e salva. **Atteso:** salvata.
     Torna a modificare il beneficiario. **Atteso:** l'ente non si cambia («L'ente non si può cambiare mentre il
     beneficiario ha un tirocinio aperto.») e nome, mail e telefono del case manager sono obbligatori.

- [ ] **T-22.4 Sede di competenza della segnalazione (US-308)**
  1. Come `tutor1`, **Nuova segnalazione** con il progetto GOL. **Atteso:** il campo «Sede di competenza» (Firenze,
     Borgo San Lorenzo, Sesto Fiorentino), obbligatorio: senza sede la segnalazione non parte.
  2. Scegli una sede e invia. **Atteso:** nel dettaglio della segnalazione «Sede di competenza» con la sede scelta e
     nessun comando per cambiarla.
  3. Come `admin` apri la stessa segnalazione. **Atteso:** «Correggi la sede di competenza»; scegli un'altra sede e
     salva. **Atteso:** «Sede di competenza corretta: …» e nella cronologia «Sede di competenza corretta» con la sede
     prima e dopo.
  4. Facoltativo: rendi `tutor2` Coordinatore di GOL (Utenti → progetti) e prova anche come lui.
  5. In **Cataloghi → Sedi di competenza** togli POLIS da «Firenze» (l'avevi aggiunto in T-22.2), poi come `admin`
     apri una segnalazione di POLIS. **Atteso:** «Sede di competenza: Non indicata» e nessun comando per correggerla.

## 23. Indirizzi e documenti reali (Sprint 13)

- [ ] **T-23.1 Indirizzi con il comune (US-1007)**
  1. Come `operatore.cc` apri **Aziende** e modifica «Demo Logistica S.r.l.». **Atteso:** «Indirizzo della sede
     legale» con «Via dell'Esempio 1» (senza «Città Demo») e «Comune della sede legale» con «Sesto Fiorentino (FI)».
  2. Nel comune scrivi `pra`. **Atteso:** dalla seconda lettera compare l'elenco dei comuni e in fondo «Solo i primi
     20 comuni: scrivi altre lettere per trovare quello giusto.». Continua fino a `prato`: **Atteso:** «Prato (PO)» in
     cima. Con la **freccia giù** evidenzia un comune, con **Invio** sceglilo; prova anche **Esc** (chiude l'elenco,
     la seconda volta svuota la casella) e la scelta con il mouse. Facoltativo, con lo screen reader: si sente quanti
     comuni ci sono e quale è evidenziato.
  3. Scrivi un nome senza sceglierlo dall'elenco e salva. **Atteso:** «Comune della sede legale: Scegli il comune
     dall'elenco» e nulla è salvato. Scegli un comune e salva. **Atteso:** salvato; nella cronologia dell'azienda il
     comune prima e dopo.
  4. Come `tutor1` apri la scheda del tirocinio di una segnalazione con l'appuntamento fissato. **Atteso:** la sede ha
     «Indirizzo della sede del tirocinio» e «Comune della sede del tirocinio»; la residenza ha «Comune di residenza»,
     «Via di residenza» e «Numero civico». Una residenza scritta prima come testo libero è nella via, con comune e
     numero civico vuoti, e la residenza compare tra i dati che mancano per l'avvio.
  5. Compila comune, via e numero civico della residenza e il comune della sede, salva e scarica la Convenzione.
     **Atteso:** «residente in Via … 12, Comune (PR)» e la sede del tirocinio «Via …, Comune (PR)».
