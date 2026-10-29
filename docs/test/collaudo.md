# NEXUS — Prove di collaudo

Elenco delle prove manuali da eseguire sulla demo, passo per passo. Ogni prova ha una casella: segnala `[x]` quando
l'esito è quello atteso. Se qualcosa non va, lascia la casella vuota e scrivi una nota sotto la prova
(`> Nota: …`): la raccolgo nella review dello sprint.

Il documento si aggiorna a ogni storia consegnata: le prove nuove sono segnate con lo sprint in cui sono arrivate.

| | |
|---|---|
| **Ambiente** | https://portalenexus.it |
| **Versione della demo** | `db3fdaa` (29/10/2026): Sprint 1–7 completi e pagina di accesso con il tema di NEXUS |
| **Ultimo aggiornamento** | 29/10/2026 (sezioni 12–15 dello Sprint 7; T-1.2 con i menu attuali) |

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
