# NEXUS — Prove di collaudo

Elenco delle prove manuali, passo per passo. Ogni prova ha una casella: segnala `[x]` quando
l'esito è quello atteso. Se qualcosa non va, lascia la casella vuota e scrivi una nota sotto la prova
(`> Nota: …`): la raccolgo nella review dello sprint.

Il documento si aggiorna a ogni storia consegnata: le prove nuove sono segnate con lo sprint in cui sono arrivate.

| | |
|---|---|
| **Ambiente** | dal 24/11: le storie nuove sul **PC del PO**, prima del merge (`infra/collaudo/collaudo.sh`, EN-11); dopo il deploy, un controllo veloce in produzione su https://portalenexus.it, senza dati finti. Fino al 24/11: la demo su https://portalenexus.it |
| **Versione in produzione** | `119cb88` (26/11/2026), su **PostgreSQL 19**, con i dati veri dal passaggio del 24/11 (US-1306): Sprint 1–17 e, dello Sprint 18, affidabilità e interesse al progetto delle aziende (US-514, V52), tema a scelta con l'intestazione fissa delle schede (US-1312), zona dell'azienda e aziende per il Tutor (US-511, V53), valutazione dell'azienda alla fine del tirocinio (US-1104, V54), azienda «nave scuola» e ragione sociale in evidenza negli elenchi (US-515, V55). Prima, dal 26/11 sera, `5095004`; dal 26/11 pomeriggio, `3a64364`; dal 26/11 mattina, `1603fcc`; dal 24/11, `8139e84` |
| **Ultimo aggiornamento** | 26/11/2026 (T-39.1 … T-39.7: azienda «nave scuola», bollino ben visibile e ragione sociale in evidenza negli elenchi, riuscite sul collaudo prima del merge; deploy `119cb88`; T-38.1 … T-38.4: valutazione dell'azienda alla fine del tirocinio, riuscite sul collaudo prima del merge; deploy `5095004`; T-37.1 … T-37.4: zona dell'azienda, aziende per il Tutor e «Azzera i filtri», riuscite sul collaudo prima del merge; deploy `3a64364`; T-35.6 riuscita in produzione dopo il deploy `1603fcc`; T-36.1 … T-36.3: tema a scelta e intestazione fissa delle schede, riuscite sul collaudo del PC prima del merge; T-35.1 … T-35.5: affidabilità e interesse al progetto delle aziende, riuscite sul collaudo del PC prima del merge; T-35.6 in produzione dopo il deploy); 25/11/2026 (T-27.3 riuscita: backup della notte dopo il passaggio, US-1306 chiusa); 24/11/2026 (T-34.1 … T-34.4: correzione dei dati di un utente, riuscite sul collaudo del PC prima del merge; T-34.5 riuscita in produzione dopo il deploy `8139e84`; T-33.1 … T-33.3 riuscite il 24/11); 23/11/2026 (collaudo prima del passaggio finito dal PO il 23/11: T-24, T-25, T-28, T-29 e T-30.1 … T-30.3 riusciti; restano T-30.4, il backup della notte sulla 19, e dopo il passaggio T-26.3 e T-27; T-30.1 … T-30.4: aggiornamento a PostgreSQL 19, fatto il 23/11 sulla demo `47fe414`; T-30.3 corretta: le aziende si provano come `operatore.cc`; T-29.1 … T-29.3: testi dei messaggi, sulla demo `e8223da`; T-28.1 … T-28.3: avvisi di fine tirocinio; T-27.1 … T-27.3: dopo il passaggio in produzione; T-26.1 e T-26.2: backup e ripristino fatti insieme il 21/11, da spuntare; T-26.3: il controllo delle 9 del 22/11; T-25.1 … T-25.3 e T-25.5 da provare sulla demo; T-25.4 in locale, sul PC del PO; T-24.1 … T-24.6, T-23.1 … T-23.4 e T-22.1 … T-22.4 da provare) |

> **Dopo il passaggio in produzione** (24/11) il sito ha i dati veri e gli utenti demo (`tutor1`, `operatore.cc`, …)
> non ci sono più: le prove fino alla sezione 32 restano come storico. Le storie nuove si collaudano sul PC del PO, dal
> branch della PR, prima del merge (decisione del PO del 24/11, EN-11): le prove dicono «sul collaudo», cioè su
> http://localhost:8090. Gli indirizzi https://portalenexus.it nelle prove vecchie valgono, nel collaudo, come
> http://localhost:8090.

## Come prepararsi

- **Collaudo sul PC** (dal 24/11): lo avvio io con `infra/collaudo/collaudo.sh avvia <PR>` e ti scrivo che è pronto;
  puoi avviarlo anche tu con lo stesso comando (README, «Collaudo sul PC»). Riparte sempre dai dati finti.

  | Cosa | Dove |
  |---|---|
  | NEXUS | http://localhost:8090 |
  | Email inviate, in una casella finta: nessuna esce dal PC | http://localhost:8025 |
  | Password di tutti gli utenti qui sotto | `collaudo-nexus-2026` |

- **Utenti finti** (sulla demo, fino al 24/11, la password era quella della configurazione del server):

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

- [ ] **T-23.2 Fine effettiva del tirocinio e ruolo del tutor aziendale (US-1009)**
  1. Come `tutor1` apri la scheda del tirocinio di una segnalazione con l'appuntamento fissato e aggiungi un tutor
     aziendale. **Atteso:** tra nome e recapito c'è «Funzione o ruolo del tutor aziendale 1», facoltativo, con
     l'esempio «Responsabile di produzione». Scrivi un ruolo e salva. **Atteso:** salvato; riaprendo la scheda il ruolo
     c'è.
  2. Scarica la Convenzione. **Atteso:** «Tutor del soggetto ospitante: Nome, ruolo (recapito).»; senza il ruolo
     «Nome (recapito)».
  3. Avvia il tirocinio e apri **Compila il report di restituzione**. **Atteso:** il primo campo è «Fine effettiva del
     tirocinio», obbligatorio, già compilato con la fine prevista della scheda.
  4. Metti una data prima dell'inizio del tirocinio e invia. **Atteso:** «Fine effettiva del tirocinio: La fine
     effettiva non può precedere l'inizio del tirocinio (gg/mm/aaaa)» nel riepilogo degli errori. Svuota il campo e
     invia. **Atteso:** «Campo obbligatorio».
  5. Metti una data prima della fine prevista, compila il resto e invia. **Atteso:** il report inviato mostra per prima
     la «Fine effettiva del tirocinio» con quella data; la scheda del tirocinio ha ancora la fine prevista di prima.

- [ ] **T-23.3 Convenzione e progetti formativi (US-1008)**
  1. Come `tutor1` apri la scheda del tirocinio di una segnalazione con l'appuntamento fissato, con le date del
     tirocinio (per esempio dal 01/12/2026 al 31/05/2027) e una griglia oraria. **Atteso:** dopo le sospensioni ci sono
     le sezioni «Convenzione» e «Progetti formativi»; sotto la convenzione «Durata: dal 01/12/2026 al 31/05/2027, come
     il tirocinio. La convenzione è per n. 1 tirocinio.».
  2. Scrivi numero, data e data di stipula della convenzione. Premi **Aggiungi progetto formativo**. **Atteso:**
     «Progetto formativo 1» dal 01/12/2026 al 31/05/2027, con la mansione della segnalazione. Cambia la fine al
     28/02/2027, scrivi un dettaglio della mansione e salva. **Atteso:** salvato; sotto il progetto «Durata: 3 mesi.
     Monte ore: … ore.».
  3. Aggiungi un secondo progetto. **Atteso:** «Progetto formativo 2» dal 01/03/2027 al 31/05/2027 con la mansione del
     primo. Metti come inizio il 28/02/2027 e salva. **Atteso:** «Progetto formativo 2: dal: Deve iniziare dopo la fine
     del progetto formativo 1 (28/02/2027)» nel riepilogo degli errori. Rimetti il 01/03/2027, cambia la mansione e
     salva. **Atteso:** salvato, con i due progetti.
  4. Anticipa la fine prevista del tirocinio al 30/04/2027 e salva. **Atteso:** «Progetto formativo 2: al: Il progetto
     finisce dopo la fine prevista del tirocinio (30/04/2027)». Rimetti la data di prima.
  5. Apri la segnalazione. **Atteso:** nella cronologia «Scheda del tirocinio aggiornata» per ogni salvataggio. (L'audit
     con convenzione e progetti, e il dettaglio della mansione solo come «cambiato», è verificato dai test.)
  6. Su un'altra segnalazione con l'appuntamento fissato e la scheda senza convenzione né progetti formativi.
     **Atteso:** in «Avvio del tirocinio» tra i dati mancanti ci sono «La convenzione (numero, data e data di stipula)» e
     «Almeno un progetto formativo»; i collegamenti portano alle due sezioni (decisione del PO dell'11/11).

- [ ] **T-23.4 Modelli dei documenti caricati dall'ADMIN (US-1010)**
  Prepara due file Word di prova: una copia della convenzione scaricata da una scheda con `{NOME_TIROCINANTE}` e una
  riga di testo riconoscibile (per esempio «MODELLO DI PROVA»), e un'altra copia con anche `{CUP}`.
  1. Come `admin` apri **Modelli dei documenti** nel menu «Amministrazione». **Atteso:** la pagina con i segnaposto
     che si possono usare (elenco a scomparsa), il modulo «Carica un modello» e l'elenco «Modelli caricati», vuoto.
  2. Carica il file con `{CUP}` come convenzione del progetto e del tipo di tirocinio di una segnalazione di prova.
     **Atteso:** «File Word (.docx): Il modello usa segnaposto che NEXUS non conosce: {CUP}» e nulla è caricato. Prova
     anche un PDF: **Atteso:** «Il file deve essere un documento Word .docx…».
  3. Carica il file giusto con il nome «Convenzione di prova» e «Tutti gli enti». **Atteso:** «Modello «Convenzione di
     prova» caricato.» e il modello nell'elenco come «Attivo», con progetto, tipo e «Tutti gli enti»; il tipo si sceglie
     solo tra quelli ammessi dal progetto.
  4. Come `tutor1` scarica la convenzione della segnalazione. **Atteso:** il documento ha «MODELLO DI PROVA» e il nome
     del tirocinante.
  5. Come `admin` carica un secondo modello con le stesse condizioni («Convenzione di prova bis»). Come `tutor1`
     **Atteso:** sopra il pulsante della convenzione compare «Modello della convenzione»; senza scegliere, «Scegli
     prima il modello…»; scelto un modello, il download parte con quello.
  6. Come `admin` disattiva i due modelli. Come `tutor1` **Atteso:** la tendina sparisce e la convenzione torna al
     modello generico. Prova anche «Sostituisci il file» e «Scarica il file» su un modello.

## 24. Proroghe, motivi di chiusura e documenti reali (Sprint 14)

- [x] **T-24.1 Motivo di chiusura del tirocinio (US-1103)**
  1. Come `admin` apri **Cataloghi**. **Atteso:** la sezione «Motivi di chiusura del tirocinio» con «Chiusura
     regolare», «Interruzione anticipata per volontà dell'azienda» e «… del destinatario». Prova a disattivare
     «Chiusura regolare». **Atteso:** l'operazione è rifiutata con la spiegazione.
  2. Come `tutor1` apri il report di restituzione di un tirocinio avviato. Metti come fine effettiva una data
     **prima** della fine prevista. **Atteso:** compaiono «Motivo dell'interruzione anticipata», con le interruzioni del
     catalogo, e «Descrizione dell'interruzione», entrambi obbligatori; senza compilarli l'invio è rifiutato con il
     riepilogo degli errori.
  3. Rimetti la fine prevista. **Atteso:** i due campi spariscono. Torna a una data anticipata, scegli il motivo,
     scrivi la descrizione e invia. **Atteso:** il report inviato mostra il «Motivo di chiusura» e la «Descrizione
     dell'interruzione»; nella cronologia della segnalazione la chiusura comincia con il motivo.
  4. Su un altro tirocinio invia il report con la fine effettiva uguale alla prevista. **Atteso:** «Motivo di
     chiusura: Chiusura regolare», senza averlo scelto.

- [x] **T-24.2 Proroga del tirocinio (US-1012)**
  1. Come `tutor1` apri la scheda di una segnalazione con l'appuntamento fissato (tirocinio non ancora avviato).
     **Atteso:** dopo «Progetti formativi» non c'è la sezione «Proroghe».
  2. Apri la scheda di un tirocinio avviato. **Atteso:** la sezione «Proroghe» con «Nessuna proroga.» e il pulsante
     «Aggiungi proroga».
  3. Premi «Aggiungi proroga». **Atteso:** compare «Proroga 1» con la data di inizio già proposta al giorno dopo la
     fine prevista. Spostala di qualche giorno più avanti, compila numero, data della convenzione, data di stipula e
     data di fine; con «Aggiungi progetto formativo alla proroga 1» aggiungi un progetto. **Atteso:** il progetto parte
     con le date della proroga e la mansione della segnalazione; nella sezione «Convenzione» compare «Con le proroghe
     il tirocinio finisce il …».
  4. Prova a salvare con la proroga che inizia il giorno di fine della convenzione, oppure con il progetto della
     proroga che inizia prima della proroga. **Atteso:** il salvataggio è rifiutato e il riepilogo degli errori
     indica il campo, per esempio «Proroga 1: dal: Deve iniziare dopo la fine della convenzione (…)».
  5. Correggi e salva. **Atteso:** sotto il progetto della proroga compaiono durata e monte ore; il monte ore del
     tirocinio non conta i giorni tra la fine della convenzione e l'inizio della proroga. Nella cronologia della
     segnalazione c'è «Scheda del tirocinio aggiornata». (L'audit con la proroga e i suoi progetti è verificato dai
     test.)
  6. Sposta la «Data di fine prevista» dopo l'inizio della proroga e salva. **Atteso:** l'errore è sulla data di fine
     prevista: «Deve finire prima dell'inizio della proroga 1 (…)».
  7. Apri il report di restituzione dello stesso tirocinio. **Atteso:** la fine effettiva è precompilata con la fine
     della proroga; con la fine della convenzione iniziale compare il motivo dell'interruzione anticipata.
  8. Dopo l'invio del report riapri la scheda. **Atteso:** la proroga si legge ma non si modifica più.

- [x] **T-24.3 Intestazione e avviso dei documenti per progetto (US-1011)**
  1. Come `admin` apri **Cataloghi**. **Atteso:** la sezione «Dati dei documenti dei progetti», con ogni progetto,
     «Nessuna intestazione» e «Testo predefinito».
  2. Premi «Modifica» su GOL. **Atteso:** il modulo si apre con il focus sul titolo; il «Riferimento all'avviso» ha
     già il testo predefinito (Avviso pubblico n. 10). Scrivi l'intestazione «Firenze in Rete 2 – Codice 9014358 – CUP
     B81D24000440006» e salva. **Atteso:** l'esito «Dati dei documenti di «GOL» salvati.» e la riga aggiornata.
  3. Riapri GOL, svuota il riferimento all'avviso e salva. **Atteso:** la colonna «Avviso» torna a «Testo
     predefinito».
  4. Apri **Modelli dei documenti** e il riquadro «Segnaposto che i modelli possono usare». **Atteso:** i segnaposto
     sono divisi per gruppo (Segnalazione e progetto, Soggetto promotore, Ente di presa in carico, Soggetto ospitante,
     Tirocinante, Tirocinio e orario, Convenzione, Progetto formativo, Chiusura), ognuno con la sua descrizione.

- [x] **T-24.4 Documenti per convenzione e progetto, relazione finale (US-1011)**

  Prima della prova: i dati del soggetto promotore di GOL in «Cataloghi» (T-24.6). Senza modelli caricati i documenti
  usano il modello generico di NEXUS.
  1. Come `tutor1` apri la scheda di una segnalazione con la convenzione e due progetti formativi salvati.
     **Atteso:** la sezione «Documenti» ha tre gruppi: «Convenzione» con «Scarica la Convenzione (.docx)»,
     «Progetti formativi» con un pulsante per ogni progetto, «Relazione finale» con «Si scarica a tirocinio chiuso…»
     e nessun pulsante.
  2. Scarica la Convenzione. **Atteso:** il file `Convenzione-segnalazione-<n>.docx` riporta l'intestazione di GOL
     (T-24.3), numero e date della convenzione, il riferimento all'avviso e i dati del promotore di GOL (T-24.6).
  3. Scarica il Progetto Formativo 2. **Atteso:** il file `Progetto-formativo-2-segnalazione-<n>.docx` riporta il
     progetto 2 con le sue date, mesi, ore e mansione, e il numero della convenzione.
  4. Sul tirocinio con la proroga (T-24.2) scarica «Scarica la Convenzione della proroga 1» e «Proroga 1: scarica il
     Progetto Formativo 1». **Atteso:** numero e date sono quelli della proroga; nella cronologia della segnalazione
     «Documento scaricato» dice «Progetto Formativo 1, proroga 1».
  5. Dopo l'invio del report di restituzione riapri la scheda. **Atteso:** nel gruppo «Relazione finale» c'è
     «Scarica la Relazione finale (.docx)»; il documento riporta la fine effettiva, i mesi del tirocinio, il motivo di
     chiusura (con la descrizione, se l'interruzione è anticipata), la sede di competenza e la data di chiusura.
  6. Usa la tastiera: **Atteso:** ogni pulsante si raggiunge con Tab e l'esito del download si legge sotto i gruppi
     («Documento scaricato: …»), mentre il focus resta sul pulsante.

- [x] **T-24.5 Modelli reali di GOL (EN-8)**

  Prima della prova: i dati del promotore di GOL (T-24.6) e i 6 modelli preparati in `documenti_base/modelli-nexus/`
  (fuori dal repository).
  1. Come `admin` apri **Modelli dei documenti** e il riquadro dei segnaposto. **Atteso:** nel gruppo «Ente di presa
     in carico» ci sono le sette caselle della tipologia («Casella della tipologia «Comune»…», fino ad «Altro
     soggetto»).
  2. Carica i 6 modelli, ognuno con il suo documento, il progetto GOL, il tipo (Extracurriculare o Di inclusione) e
     nessun ente:
     - `GOL-Extracurriculare-Convenzione.docx`, `GOL-Extracurriculare-Progetto-formativo.docx`,
       `GOL-Extracurriculare-Relazione-finale.docx`;
     - `GOL-Inclusione-Convenzione.docx` (Allegato A), `GOL-Inclusione-Progetto-formativo.docx` (Allegato B),
       `GOL-Inclusione-Relazione-finale.docx` (Allegato C).

     **Atteso:** tutti accettati, senza segnaposto sconosciuti.
  3. Come `tutor1` scarica Convenzione e Progetto Formativo di un tirocinio Extracurriculare di GOL. **Atteso:** il
     modello della Regione con loghi, articoli e formattazione originali (grassetti, elenchi, piè di pagina con
     l'intestazione del progetto) e i dati compilati: promotore, ospitante, tirocinante, convenzione, progetto
     formativo, giorni a caselle, fasce per giorno su una riga, polizze INAIL e RC. Il bollo dice solo «non è
     soggetta».
  4. Scarica i documenti di un tirocinio Di inclusione. **Atteso:** nell'Allegato B il sesso e la tipologia dell'ente
     a caselle (☒ su «Comune» per il Comune di Firenze, su «Società della Salute» per una SdS), referente dell'ente e
     case manager, e in fondo la firma «Il Tirocinante» con il nome; nell'Allegato A l'ente con il suo rappresentante.
  5. A tirocinio chiuso scarica le due relazioni finali. **Atteso:** nell'Extracurriculare il motivo di chiusura (e la
     descrizione dell'interruzione) in testa alla «Valutazione complessiva», i mesi e la fine effettiva; nell'Allegato
     C la durata fino alla fine effettiva, con luogo (sede di competenza) e data di chiusura.
  6. Apri i documenti in Word. **Atteso:** si aprono senza avvisi di contenuto illeggibile.

- [x] **T-24.6 Dati del promotore e variabili per progetto (US-1013)**

  Da fare prima di T-24.4 e T-24.5: i dati del promotore servono ai documenti.
  1. Come `admin` apri **Cataloghi** → «Dati dei documenti dei progetti». **Atteso:** le colonne «Promotore» («Non
     indicato») e «Variabili» («Nessuna»).
  2. Premi «Modifica» su GOL. **Atteso:** oltre a intestazione e avviso, i gruppi «Soggetto promotore», «Legale
     rappresentante del promotore», «Assicurazioni» e «Variabili del progetto».
  3. Inserisci i dati del promotore dai modelli della cooperativa (denominazione, sede, codice fiscale, telefono, mail,
     PEC, legale rappresentante con ruolo, luogo e data di nascita, posizione INAIL, compagnia, numero e scadenza della
     polizza RC) e salva. **Atteso:** l'esito «Dati dei documenti di «GOL» salvati.» e la denominazione nella colonna
     «Promotore».
  4. Riapri GOL e scrivi una PEC sbagliata (`pec-sbagliata`). **Atteso:** il riepilogo degli errori con il link al
     campo PEC; correggila e salva.
  5. Premi «Aggiungi una variabile»: nome `prova`, valore «Valore di prova», e salva. **Atteso:** il nome diventa
     `PROVA` e la colonna «Variabili» lo riporta. Riapri: il nome non si modifica più. Prova anche il nome `PROMOTORE`.
     **Atteso:** l'errore «PROMOTORE è già un segnaposto di NEXUS».
  6. In **Modelli dei documenti** il riquadro dei segnaposto ha il gruppo «Variabili del progetto «GOL…»» con `PROVA`.
     Carica per GOL un .docx che contiene `{PROVA}`. **Atteso:** accettato; lo stesso file per un altro progetto è
     rifiutato perché `{PROVA}` è sconosciuto.
  7. Torna in Cataloghi e togli la variabile `PROVA`. **Atteso:** il salvataggio è rifiutato: la variabile è usata dal
     modello appena caricato.
  8. In Modelli dei documenti disattiva il modello di prova, poi togli `PROVA` dai Cataloghi. **Atteso:** ora si
     salva. Riattiva il modello. **Atteso:** rifiutato, perché `{PROVA}` non è più un segnaposto noto; il modello
     resta disattivato.

## 25. Database attuale e aziende (Sprint 15)

- [x] **T-25.1 Aziende da completare (US-508)**

  Le aziende del database attuale arriveranno con l'importazione (US-1309). Nella demo ce ne sono due dimostrative:
  «Demo Bar del Centro», senza partita IVA, e «Azienda senza nome n. 90001», senza nome né partita IVA.
  1. Come `operatore.cc` apri **Aziende**. **Atteso:** per le due aziende, nella colonna «Partita IVA» c'è «Da
     completare» al posto degli zeri; accanto ad «Azienda senza nome n. 90001» c'è anche «Da completare».
  2. Spunta «Solo le aziende da completare». **Atteso:** restano solo le due aziende e l'indirizzo della pagina
     contiene `daCompletare=1`; con il tasto Indietro torna l'elenco completo.
  3. Apri «Azienda senza nome n. 90001». **Atteso:** sotto il titolo «P.IVA da completare» e l'avviso «Da completare:
     mancano la ragione sociale e la partita IVA…»; nel modulo i due campi sono vuoti, con la spiegazione, e il
     telefono è quello salvato.
  4. Premi «Salva modifiche» senza compilarli. **Atteso:** il riepilogo degli errori con i due campi. Scrivi
     «Azienda senza nome n. 5» e «00000000000». **Atteso:** i messaggi «Inserisci la ragione sociale vera…» e
     «Inserisci la partita IVA vera…».
  5. Inserisci un nome e una partita IVA veri (11 cifre, non usata da altre aziende) e salva. **Atteso:** «Modifiche
     salvate.», il titolo diventa il nome nuovo, sotto c'è la partita IVA e l'avviso sparisce; l'azienda non compare
     più tra quelle da completare.
  6. Aggiungi a «Demo Bar del Centro» una mansione con la tipologia e la zona di una segnalazione da abbinare. Apri
     quella segnalazione e, tra le mansioni compatibili, premi «Dettagli» sulla mansione del bar. **Atteso:** «Partita
     IVA: Da completare».

- [x] **T-25.2 Categoria merceologica e attività (US-509)**
  1. Come `admin` apri **Cataloghi**. **Atteso:** dopo «Tipologie di mansione» la sezione «Categorie merceologiche»
     con 53 voci attive, tra cui «Albergo», «Ristorante / Trattoria / Bistrot» e «Pulizie e facchinaggio».
  2. Aggiungi «Agenzia immobiliare». **Atteso:** la voce compare attiva nell'elenco.
  3. Come `operatore.cc` apri **Aziende**. **Atteso:** la colonna «Categoria» dopo la partita IVA: le aziende
     dimostrative hanno una categoria (per esempio «Demo Ristorazione S.n.c.» → «Ristorante / Trattoria / Bistrot»).
  4. Nel menu «Categoria merceologica» scegli «Bar / Pub». **Atteso:** l'elenco si aggiorna subito, mostra solo «Demo
     Bar del Centro» e l'indirizzo contiene `categoria=`; torna a «Tutte le categorie».
  5. Apri «Demo Logistica S.r.l.». **Atteso:** nel modulo «Categoria merceologica» («Trasporto merci / Traslochi /
     Imballaggi») e «Attività» («Logistica e magazzino»). Scegli «Agenzia immobiliare», scrivi un'altra attività e
     salva. **Atteso:** «Modifiche salvate.» e la categoria nuova nell'elenco.
  6. Come `admin` disattiva «Agenzia immobiliare» nei Cataloghi. Come `operatore.cc` riapri «Demo Logistica S.r.l.».
     **Atteso:** la categoria resta «Agenzia immobiliare» e il salvataggio riesce; in un'altra azienda il menu non la
     propone più, né la propone il filtro dell'elenco.
  7. In una segnalazione da abbinare, premi «Dettagli» su una mansione di un'azienda dimostrativa. **Atteso:** tra i
     dati dell'azienda «Categoria merceologica» e «Attività».

- [x] **T-25.3 Referenti, recapiti e sede operativa (US-510)**

  Sulla demo il referente che le aziende avevano prima diventa il primo referente, senza ruolo. Le due aziende
  dimostrative senza referente ne ricevono uno con il ruolo.
  1. Come `admin` apri **Cataloghi**. **Atteso:** dopo «Categorie merceologiche» c'è la sezione «Ruoli dei referenti»
     con 12 voci attive, tra cui «Titolare», «Responsabile del personale» e «Legale rappresentante».
  2. Come `operatore.cc` apri **Aziende**. **Atteso:**
     - la colonna «Referente» mostra il primo referente: per esempio «Referente Bar» per «Demo Bar del Centro»;
     - per «Azienda senza nome n. 90001» mostra «Centralino», cioè il ruolo, perché il nome manca.
  3. Apri «Demo Logistica S.r.l.». **Atteso:**
     - il modulo è diviso in «Sede legale», «Sede operativa», «Recapiti dell'azienda» e «Referenti»;
     - la sede operativa è «Via del Magazzino 10», «Prato (PO)»;
     - tra i recapiti ci sono anche «Cellulare», «Fax» e «Seconda email»;
     - «Referente 1» è il referente di prima.
  4. Scegli per il referente 1 il ruolo «Titolare». Premi «Aggiungi referente». **Atteso:** compare «Referente 2» e il
     cursore è nel suo nome.
  5. Lascia vuoto il nome, scrivi solo il telefono del referente 2 e salva. **Atteso:** il riepilogo «Referente 2:
     nome: Inserisci il nome o scegli il ruolo»; il collegamento porta al campo.
  6. Scegli per il referente 2 il ruolo «Responsabile del personale», scrivi il cellulare e il fax dell'azienda e
     salva. **Atteso:**
     - «Modifiche salvate.»;
     - nell'elenco delle aziende «Referente Logistica e un altro» (o il nome del referente 1 seguito da «e un altro»).
  7. Riapri l'azienda e premi «Togli il referente 2». **Atteso:** il gruppo sparisce e il cursore è su «Aggiungi
     referente». Salva.
  8. In una segnalazione da abbinare, premi «Dettagli» su una mansione di «Demo Logistica S.r.l.». **Atteso:**
     - «Sede operativa: Via del Magazzino 10, Prato (PO)»;
     - cellulare e fax;
     - i referenti, ognuno con il ruolo e i recapiti.

- [x] **T-25.4 Importazione dal database attuale, in prova (US-1309)**

  Si fa sul PC, non sulla demo: il file Access ha dati personali e sulla demo restano i dati dimostrativi.
  L'importazione vera si fa con il passaggio in produzione (US-1306).
  1. Segui la procedura dell'[analisi](../analisi/database-attuale.md), §9.1. **Atteso:** il comando finisce da solo
     in pochi secondi; nel log c'è la riga «Importazione dal database attuale: aziende 13799 lette, 13799 nuove…».
  2. Apri `documenti_base/nexus.mdb-resoconto.txt`. **Atteso:**
     - i conteggi come nella tabella del §9.3;
     - le sezioni «Da rivedere» con i numeri di Access e i collegamenti `/aziende/…`;
     - nessun nome di azienda o di persona, recapito o nota.
  3. Tra le «Chiamate con possibili nomi di beneficiari» controlla in Access quelle che hanno davvero nome e cognome,
     insieme alle 7 dell'analisi (§7, 4b).
  4. Ripeti il comando. **Atteso:** «0 nuove» per aziende e chiamate.
  5. Facoltativo, con il frontend in locale (`pnpm dev`) e l'utente `operatore.cc`: apri un'azienda del resoconto.
     **Atteso:**
     - la partita IVA è da completare;
     - ci sono i referenti con il ruolo;
     - nel diario c'è la nota con la data di Access e l'autore «Database attuale».

- [x] **T-25.5 Una mansione per più segnalazioni, una per tipo di tirocinio (US-605)**

  Nella demo la segnalazione n. 4 (GOL, Extracurriculare) ha la proposta della mansione «Magazziniere reparto
  spedizioni». La n. 3 (POLIS, in lavorazione, in carico a `operatore.cc`) è Curriculare.
  1. Come `tutor1` apri **Nuova segnalazione**. **Atteso:**
     - c'è il menu «Tipo di tirocinio», obbligatorio, spento finché non scegli il progetto;
     - scelto GOL, propone solo i tipi ammessi dal progetto.
     Scegli «Di inclusione» per un beneficiario senza case manager e invia. **Atteso:** l'errore sul tipo dice che
     cosa manca nella scheda del beneficiario.
  2. Come `operatore.cc` apri la segnalazione n. 3. **Atteso:** nei dati c'è «Tipo di tirocinio: Curriculare».
  3. Tra le mansioni compatibili scegli «Tutte le zone» e «Tutte le tipologie». **Atteso:**
     - «Magazziniere reparto spedizioni» compare con l'avviso «Già assegnata alla segnalazione n. 4
       (Extracurriculare)»;
     - l'avviso c'è anche in «Dettagli»;
     - c'è anche nella conferma della proposta.
  4. Conferma la proposta. **Atteso:**
     - la proposta riesce;
     - nella scheda di «Demo Logistica S.r.l.», tra le mansioni, lo stato è «Assegnata – segnalazioni n. 3
       (Curriculare), n. 4 (Extracurriculare)».
  5. Come `tutor2`, sulla segnalazione n. 3 premi «Cambia il tipo», scegli «Extracurriculare» e salva. **Atteso:**
     l'errore dice che sulla mansione c'è già la segnalazione n. 4 (Extracurriculare). Premi «Annulla»: il focus
     torna su «Cambia il tipo».
  6. Come `tutor2` rifiuta la proposta della n. 3. **Atteso:** la mansione resta assegnata alla n. 4, e lo stato
     nelle mansioni dell'azienda torna «Assegnata – segnalazione n. 4 (Extracurriculare)».
  7. Se trovi una segnalazione senza tipo, aprila come `operatore.cc`. **Atteso:**
     - «Tipo di tirocinio: Non indicato»;
     - sopra le mansioni l'avviso che il tipo lo indica il Tutor;
     - i pulsanti «Proponi» spenti.

## 26. Backup del database (Sprint 16)

Si fanno dopo il deploy di US-1302 e la preparazione del README, «Backup del database», punti 1–5: contenitore su IONOS,
chiavi age, variabili nel `.env` e `nexus-deploy` aggiornato. Sulla demo ci sono solo dati dimostrativi.

- [x] **T-26.1 Backup subito e stato (US-1302)**
  1. Su GitHub apri Actions → «Backup del database» → *Run workflow*, scegli «backup adesso». **Atteso:**
     - il run è verde;
     - nel log ci sono i passi (salvataggio di nexus e keycloak, cifratura, caricamento, verifica) e la riga «OK: …»
       con il nome del file e la dimensione;
     - nessun nome, email o altro dato della demo.
  2. Rilancia il workflow con «stato». **Atteso:**
     - «Configurazione: completa»;
     - «Ultimo backup riuscito» con l'ora del punto 1;
     - «Blocco: GOVERNANCE fino al …», 30 giorni dopo;
     - nessun avviso, e in fondo «OK».
  3. Nella console di IONOS apri il contenitore. **Atteso:** c'è il file `nexus/AAAA/MM/nexus-….tar.age`.
  4. Apri le proprietà del file. **Atteso:** il blocco è *Governance*, fino a 30 giorni dopo il backup. Non serve
     provare a cancellarlo: con *Governance* tu puoi farlo, scavalcando il blocco, e il backup andrebbe perso.

- [x] **T-26.2 Ripristino di prova sul PC (US-1302)**
  1. Scarica dalla console di IONOS il file del punto T-26.1.
  2. Sul PC, in WSL, nella cartella del progetto: `backup/ripristino-di-prova.sh <file scaricato>
     ~/nexus-backup-chiave.txt`. **Atteso:**
     - «Impronte corrette.» e l'indice con la data e la versione;
     - le righe per tabella, simili a quelle della demo (per esempio `company` e `ticket` non a zero);
     - «Ultima migrazione: V49» (o la più recente);
     - gli utenti di Keycloak del realm `nexus`;
     - in fondo «Fatto: il ripristino riesce».
  3. Ripeti con una chiave diversa (`age-keygen -o /tmp/altra.txt`, poi `/tmp/altra.txt` al posto della tua).
     **Atteso:** «non riesco a decifrare l'archivio», e nessun database ripristinato.

- [ ] **T-26.3 Controllo del mattino (US-1302)**
  1. Il giorno dopo il deploy, verso le 9:30, apri Actions → «Backup del database». **Atteso:** c'è un run delle
     9:07, verde, con l'esito del backup delle 3:30. GitHub può ritardarlo: il 22/11 è partito alle 17:06.
  2. Nello stesso giorno c'è un solo run pianificato che ha controllato davvero; l'altro, se c'è, dice «Cron
     dell'altra stagione».
  3. Facoltativo: se un giorno il run fallisce, arriva l'email di GitHub, come per «Controllo del sito».

## 27. Passaggio in produzione (Sprint 16)

Si fanno subito dopo lo script del passaggio (README, «Passaggio in produzione»), sul sito vero, con i dati veri: nelle
note scrivi solo numeri e codici, mai nomi o recapiti.

- [x] **T-27.1 Primo accesso dell'ADMIN (US-1306)**
  1. Entro qualche minuto dal passo 7 dello script controlla la casella di `NEXUS_ADMIN_EMAIL`. **Atteso:** l'email
     di Keycloak per impostare la password. Se non arriva, usa «Password dimenticata?» nella pagina di accesso.
  2. Imposta la password e accedi. **Atteso:** sei dentro come ADMIN.
  3. Apri «Utenti». **Atteso:** ci sei solo tu, con il ruolo ADMIN.
  4. Prova ad accedere come `tutor1` con la password della demo. **Atteso:** l'accesso è rifiutato: l'utente non
     esiste più.
  > Nota (24/11): riuscita dal PO, subito dopo il passaggio.

- [x] **T-27.2 Dati della produzione (US-1306)**
  1. Apri la bacheca e l'elenco delle segnalazioni. **Atteso:** sono vuoti.
  2. Apri «Aziende». **Atteso:**
     - 13.799 aziende, quelle del database attuale; nessuna azienda «Demo»;
     - con «Solo le aziende da completare» restano quelle senza partita IVA o senza nome.
  > Nota (24/11): le aziende ci sono, ma l'elenco le carica tutte insieme ed è troppo lento. Il PO chiede le pagine:
  > US-513, prove T-32.
  3. Apri un'azienda con una nota di Access. **Atteso:** nel diario la nota ha la data di Access e l'autore «Database
     attuale».
  4. Apri i Cataloghi. **Atteso:** progetti, tipologie di mansione, sedi, enti, comuni e i 6 modelli dei documenti
     sono quelli della demo. Correggi o disattiva le voci che la pulizia ha elencato come dimostrative.
  > Nota (24/11): riuscita dal PO, con la nota di Access nel diario e i Cataloghi controllati. La velocità dell'elenco
  > delle aziende si prova con T-32, dopo il deploy di US-513.

- [x] **T-27.3 Backup della produzione (US-1302, US-1306)**
  1. Il giorno dopo il passaggio, alle 9, controlla Actions → «Backup del database». **Atteso:** verde, con il backup
     della notte.
  > Nota (25/11): riuscita, con il controllo «stato» lanciato a mano alle 9:25 (run 45576089761): backup delle 3:30,
  > 4,3 MB, cifrato, bloccato su IONOS fino al 25/12; release in esecuzione `8139e84`.

- [x] **T-27.4 Console di Keycloak solo dal tunnel (US-1306, controllo ASVS del 24/11)**
  1. Prima del deploy della seconda PR di US-1306, sul server: `sudo bash keycloak-admin-tunnel.sh` (README, «Console
     di Keycloak»). **Atteso:** «impostato: il realm master risponde come http://localhost:8081».
  2. Dopo il deploy, dal PC: `ssh -L 8081:127.0.0.1:8081 <server>`, poi apri `http://localhost:8081/admin` e accedi
     come `admin`. **Atteso:** la console si apre; in «Users» del realm nexus vedi gli utenti.
  3. Senza tunnel apri `https://auth.portalenexus.it/realms/master/account`. **Atteso:** «Accesso non consentito».
  4. Esci e rientra in NEXUS. **Atteso:** l'accesso funziona come prima.
  > Nota (24/11): riuscita dal PO, con lo script eseguito prima del deploy di `388e8c2`.

## 28. Avvisi di fine tirocinio (Sprint 16)

Si fanno sulla demo dopo il deploy di US-1203, come `tutor1`, con un tirocinio avviato. Se non ce n'è uno: porta una
segnalazione fino all'appuntamento, compila la scheda con inizio un mese fa e avvia il tirocinio (T-16.2).

- [x] **T-28.1 Blocco delle nuove segnalazioni (US-1203)**
  1. Nella scheda del tirocinio metti come fine prevista **ieri** e salva.
  2. Apri «Nuova segnalazione». **Atteso:**
     - al posto del modulo c'è «Prima chiudi i tirocini finiti»;
     - c'è il collegamento «Segnalazione n. …, fine prevista il …» con la data di ieri, e porta alla segnalazione.
  3. Dalla bacheca prova a candidare un beneficiario a un annuncio. **Atteso:** lo stesso avviso, niente modulo.
  4. Accedi come `tutor2` e apri «Nuova segnalazione». **Atteso:** il modulo c'è: il blocco vale solo per `tutor1`.
  5. Torna `tutor1` e metti come fine prevista **oggi**. **Atteso:** «Nuova segnalazione» mostra di nuovo il modulo.

- [x] **T-28.2 Proroga e chiusura (US-1203)**
  1. Rimetti la fine prevista a ieri. **Atteso:** «Nuova segnalazione» è di nuovo bloccata.
  2. Registra una proroga di un mese. **Atteso:** il modulo torna.
  3. Facoltativo: chiudi il tirocinio con il report di restituzione. **Atteso:** anche così il blocco non c'è.

- [x] **T-28.3 Avvisi del mattino (US-1203)**
  1. Su un tirocinio in corso metti come fine prevista il giorno fra **7 giorni** e salva.
  2. La mattina dopo, dalle 8, controlla le notifiche di `tutor1` e la sua casella. **Atteso:**
     - la notifica «Il tirocinio della segnalazione n. … sta per finire: la convenzione è in chiusura…»;
     - la stessa email, con oggetto «PORTALE NEXUS - Segnalazione n. …».

     Se `tutor1` ha un indirizzo finto, dagli prima un indirizzo vero dal suo profilo (README, US-110).
  3. Nella cronologia della segnalazione. **Atteso:** «Avviso di fine tirocinio al Tutor».
  4. Ricontrolla dopo le 8 del giorno dopo. **Atteso:** nessun altro avviso, perché il prossimo è a 3 giorni dalla
     fine.

## 29. Testi dei messaggi (Sprint 16)

Si fanno sulla demo dopo il deploy di US-1204, come `admin`. Alla fine ripristina i testi cambiati: la configurazione
passa in produzione così com'è.

- [x] **T-29.1 Pagina e testi distinti (US-1204)**
  1. Nel menu di amministrazione apri «Testi dei messaggi». **Atteso:** due gruppi, «Notifiche» ed «Email del cambio
     di indirizzo»; ogni messaggio dice a chi va e quando parte.
  2. Apri «Proposta di tirocinio». **Atteso:**
     - tre campi: testo nell'app, oggetto (parte distintiva), testo dell'email;
     - l'anteprima dell'oggetto è «PORTALE NEXUS - Segnalazione n. 12 - Nuova proposta di tirocinio»;
     - l'anteprima dell'email finisce con collegamento, avvertenza e «Portale Nexus».
  3. Cambia il testo nell'app in «Prova: proposta per la n. {numero}» e salva. **Atteso:** «Testo salvato.» e, nel
     sommario, «Testi cambiati».
  4. Come `operatore.cc` fai una proposta su una segnalazione di `tutor1`; come `tutor1` apri le notifiche. **Atteso:**
     la notifica dice «Prova: proposta per la n. …»; l'email, se `tutor1` ha un indirizzo vero, ha il testo di sempre e
     l'oggetto con «Nuova proposta di tirocinio».

- [x] **T-29.2 Controlli (US-1204)**
  1. Nel testo dell'email scrivi «Ciao {nome}» e salva. **Atteso:** il campo dice che si può usare solo {numero}.
  2. Scrivi «Proposta {numero» e salva. **Atteso:** il campo parla delle parentesi graffe.
  3. Svuota l'oggetto e salva. **Atteso:** «Scrivi il testo».
  4. Accedi come `tutor1` e apri `/testi`. **Atteso:** la pagina non c'è per il Tutor.

- [x] **T-29.3 Ripristino (US-1204)**
  1. Come `admin`, sul testo nell'app cambiato al T-29.1, premi «Ripristina il testo predefinito». **Atteso:**
     «Ripristinato il testo predefinito.», il campo torna al testo di prima e «Testi cambiati» sparisce.
  2. Apri «Codice per il nuovo indirizzo email». **Atteso:** due campi, oggetto e testo, con i segnaposto {codice} e
     {ora}; l'anteprima dell'oggetto non ha il numero della segnalazione.


## 30. Aggiornamento a PostgreSQL 19 (Sprint 17)

Si fanno con l'aggiornamento della demo (README, «Aggiornare PostgreSQL»). Nelle note solo numeri e codici.

- [x] **T-30.1 Prova sul backup vero, sul PC (EN-10)**
  1. Scarica dalla console di IONOS l'ultimo backup della demo.
  2. In WSL, nella cartella del progetto aggiornata a `main`: `backup/ripristino-di-prova.sh --tieni <file scaricato>
     ~/nexus-backup-chiave.txt`. **Atteso:**
     - «PostgreSQL temporaneo (postgres:19.0-alpine, …)»;
     - le righe per tabella, «Ultima migrazione: V51» e gli utenti di Keycloak, come nel ripristino del 23/11;
     - in fondo «Fatto: il ripristino riesce».
  3. Lascia il database acceso: lo uso per avviare backend e Keycloak sulla 19, poi lo spengo io.

- [x] **T-30.2 Aggiornamento sul server (EN-10)**
  1. `sudo bash aggiorna-postgresql.sh prepara`, con «SI». **Atteso:**
     - i passi da 1 a 7 senza errori;
     - al passo 7 «Stessi numeri:», con tabelle, righe, trigger, «ultima migrazione: V51» e utenti di Keycloak;
     - in fondo «Fatto: la PostgreSQL 19 è pronta».
  2. Deploy di `main` dalle Actions. **Atteso:** il run è verde e il sito risponde.
  3. `sudo bash aggiorna-postgresql.sh verifica`. **Atteso:** «Stessi numeri di prima dell'aggiornamento», il backup
     riuscito e «Blocco: GOVERNANCE fino al …».
  4. Si fa una volta sola. **Atteso:** rilanciato dopo, `prepara` si ferma al passo 1 con «PostgreSQL è già alla 19:
     niente da aggiornare», senza fermare il sito né toccare i dati.

- [x] **T-30.3 La demo sulla 19 (EN-10)**
  1. Accedi come `tutor1`. **Atteso:** le segnalazioni di prima, con lo stesso stato.
  2. Esci e rientra come `operatore.cc`: «Aziende» è del Call Center e dell'ADMIN, il Tutor non ce l'ha. Apri
     un'azienda e aggiungi una nota al diario. **Atteso:** la nota si salva e compare.
  3. Esci e rientra come `admin`, poi apri «Utenti». **Atteso:** gli utenti di prima.

- [x] **T-30.4 Backup della notte sulla 19 (EN-10)**
  1. Il giorno dopo l'aggiornamento, verso le 9:30, apri Actions → «Backup del database». **Atteso:** il run delle
     9:07 è verde, con il backup delle 3:30 (può partire in ritardo, come il 22/11).
  > Nota (24/11): backup delle 3:30 riuscito sulla 19 (1 MB, blocco fino al 24/12). Il controllo pianificato non era
  > ancora partito alle 9:40: lo stesso controllo, lanciato a mano alle 9:08 («stato»), è verde.

## 31. Sessione attiva finché NEXUS è aperto (Sprint 17)

Si fanno dopo il deploy di US-1310, con un utente qualsiasi.

- [x] **T-31.1 Pagina aperta in secondo piano (US-1310)**
  1. Accedi a NEXUS, poi passa a un'altra scheda o a un altro programma, oppure blocca lo schermo (Windows+L).
  2. Dopo almeno 45 minuti torna a NEXUS e apri una pagina qualsiasi. **Atteso:** sei ancora dentro, senza password.

- [x] **T-31.2 PC in sospensione (US-1310)**
  1. Con NEXUS aperto, metti il PC in sospensione (o chiudi il coperchio) per più di 40 minuti.
  2. Riaccendilo. **Atteso:** entro un minuto NEXUS mostra la pagina di accesso, senza i dati di prima sullo schermo.

- [x] **T-31.3 Avviso prima delle 10 ore (US-1310)**
  1. Facoltativo, a fine giornata: resta collegato quasi 10 ore. **Atteso:** 5 minuti prima sotto l'intestazione compare
     «La sessione scade alle …», con «Accedi di nuovo adesso» e «Ho capito».
  2. Premi «Accedi di nuovo adesso». **Atteso:** la pagina di accesso chiede la password; dopo l'accesso sei di nuovo
     dentro.
  > Nota (24/11): T-31.1 … T-31.3 riuscite dal PO in produzione.

## 32. Elenco delle aziende a pagine (Sprint 17)

Si fanno dopo il deploy di US-513, come ADMIN, in produzione: si consulta soltanto, senza modificare niente.

- [x] **T-32.1 Prima pagina (US-513)**
  1. Apri «Aziende». **Atteso:**
     - l'elenco compare in un paio di secondi;
     - sopra la tabella «13.799 aziende: dalla 1 alla 25», e 25 righe in ordine di ragione sociale;
     - sotto la tabella «Pagina 1 di 552», con «Prima» e «Precedente» disattivati e il menu «Aziende per pagina» su 25.

- [x] **T-32.2 Sfogliare (US-513)**
  1. Premi «Successiva». **Atteso:** «dalla 26 alla 50», «Pagina 2 di 552»; la pagina torna all'inizio della tabella.
  2. Premi «Ultima». **Atteso:** «Pagina 552 di 552», con «Successiva» e «Ultima» disattivati.
  3. Premi Indietro del browser. **Atteso:** torni alla pagina 2.

- [x] **T-32.3 Aziende per pagina (US-513)**
  1. Nel menu «Aziende per pagina» scegli 100. **Atteso:** 100 righe, «dalla 1 alla 100», «Pagina 1 di 138».
  2. Prova anche 10 e 50. **Atteso:** le righe e le pagine cambiano di conseguenza.

- [x] **T-32.4 Filtri e pagine (US-513)**
  1. Con 50 aziende per pagina vai alla pagina 3, poi spunta «Solo le aziende da completare». **Atteso:** sei alla
     pagina 1 delle aziende da completare, sempre con 50 per pagina.
  2. Cerca una ragione sociale che conosci. **Atteso:** la trovi; con pochi risultati i pulsanti delle pagine non ci
     sono.
  > Nota (24/11): T-32.1 … T-32.4 riuscite dal PO in produzione, dopo il deploy di `db4a4c5`.

## 33. Collaudo sul PC (Sprint 17)

Si fanno sul tuo PC, con il collaudo avviato da me (`infra/collaudo/collaudo.sh avvia <PR>`): dati e utenti finti, la
password è `collaudo-nexus-2026`. Nessun dato vero, nessuna email esce dal PC.

- [x] **T-33.1 Accesso al collaudo (EN-11)**
  1. Apri http://localhost:8090. **Atteso:** la pagina «Accedi a NEXUS», con il tema, su http://localhost:8091.
  2. Accedi come `operatore.cc`. **Atteso:** sei dentro; in «Segnalazioni» e in «Aziende» ci sono i dati finti del
     seed (per esempio «Demo Bar del Centro»), nessun dato vero.
  3. Esci ed entra come `tutor1`. **Atteso:** il menu del Tutor, con «Beneficiari».

- [x] **T-33.2 Email nella casella finta (EN-11)**
  1. Esci; nella pagina di accesso premi «Password dimenticata?» e scrivi `tutor2`.
  2. Apri http://localhost:8025. **Atteso:** c'è l'email «PORTALE NEXUS - Reimposta la password» per
     `tutor2@nexus.local`; nessuna email è arrivata davvero.

- [x] **T-33.3 Tunnel e produzione intatti (EN-11)**
  1. Con il collaudo avviato apri il tunnel verso la produzione (`ssh -L 8081:127.0.0.1:8081 <server>`). **Atteso:** si
     apre senza errori di porta occupata; la console della produzione funziona come prima.
  2. Apri https://portalenexus.it. **Atteso:** la produzione, con i dati veri, non è cambiata.
  > Nota (24/11): T-33.1 … T-33.3 riuscite dal PO sul suo PC, con il collaudo della PR #201 (`ff59f76`).

## 34. Correzione dei dati di un utente (Sprint 17)

Sul collaudo del PC, prima del merge (http://localhost:8090, password `collaudo-nexus-2026`; email su
http://localhost:8025). Gli utenti qui sotto sono finti: in produzione, dopo il deploy, c'è solo T-34.5.

- [x] **T-34.1 Username di un utente mai entrato (US-116)**
  1. Entra come `admin`; in «Utenti» → «Nuovo utente» crea un Tutor: nome `Mario`, cognome `Rosi`, username
     `mario.rosi`, email `mario.rosi@nexus.local`. **Atteso:** l'utente è creato e l'invito è nella casella finta.
  2. Nella riga di Mario Rosi premi «Dati». **Atteso:** la pagina «Dati di Mario Rosi» dice che non è ancora entrato;
     tutti i campi, username compreso, si possono cambiare.
  3. Correggi cognome `Rossi`, username `mario.rossi` ed email `mario.rossi@nexus.local`, poi «Salva i dati».
     **Atteso:** torni a «Utenti» con «Dati di Mario Rossi salvati. Abbiamo inviato a mario.rossi@nexus.local un nuovo
     invito…»; nell'elenco c'è `mario.rossi`.
  4. Nella casella finta apri l'invito arrivato a `mario.rosi@nexus.local` e premi il collegamento. **Atteso:** Keycloak
     risponde «Indirizzo email non valido»: il vecchio invito non vale più.
  5. Apri l'invito arrivato a `mario.rossi@nexus.local`, imposta la password `collaudo-nexus-2026` ed entra come
     `mario.rossi`. **Atteso:** sei dentro, con il menu del Tutor.

- [x] **T-34.2 Username bloccato dopo il primo accesso (US-116)**
  1. Esci ed entra come `admin`; in «Utenti» premi «Dati» di Mario Rossi. **Atteso:** lo username è in sola lettura,
     con il motivo «È già entrato in NEXUS: lo username non si cambia più…»; gli altri campi si possono cambiare.
  2. Scrivi un telefono, per esempio `055 123456`, e salva. **Atteso:** «Dati di Mario Rossi salvati.»
  3. Prova lo stesso con «Dati» di `tutor1`. **Atteso:** anche qui lo username è in sola lettura.
  4. Restringi la finestra alla larghezza di uno smartphone (o F12 → vista dispositivo). **Atteso:** i campi stanno uno
     sotto l'altro, leggibili, senza scorrere di lato; in «Utenti» la tabella scorre di lato dentro il suo riquadro.

- [x] **T-34.3 Email di un utente già entrato (US-116)**
  1. In «Dati» di Mario Rossi scrivi l'email di un altro utente, `tutor2@nexus.local`, e salva. **Atteso:** errore sul
     campo, «Indirizzo già usato da un altro utente»; niente cambia.
  2. Scrivi `mario.rossi2@nexus.local` e salva. **Atteso:** «Dati di Mario Rossi salvati.»; nella casella finta il
     vecchio indirizzo `mario.rossi@nexus.local` riceve «PORTALE NEXUS - Il tuo indirizzo email è stato cambiato».

- [x] **T-34.4 Dati dell'account solo da NEXUS (US-116)**
  1. Esci ed entra come `mario.rossi`; poi apri http://localhost:8091/realms/nexus/account. **Atteso:** la pagina
     «il mio account» di Keycloak mostra username, email, nome e cognome, ma non li fa cambiare.
  > Nota (24/11): T-34.1 … T-34.4 riuscite dal PO sul suo PC, con il collaudo della PR #204 (`dc015d5`).

- [x] **T-34.5 In produzione, dopo il deploy (US-116)**
  1. Sul server, una volta: lo script `keycloak-user-profile.sh` (README, «Dati degli utenti solo da NEXUS»; ti scrivo
     io il comando). **Atteso:** finisce con «Fatto: l'ADMIN corregge i dati degli utenti dalla pagina «Dati» di
     NEXUS…».
  2. Su https://portalenexus.it, in «Utenti» premi «Dati» dell'utente creato con lo username sbagliato, correggilo e
     salva. **Atteso:** «Dati di … salvati.»; nell'elenco c'è lo username giusto. L'invito già inviato resta valido se
     l'email non cambia.
  > Nota (24/11, sera): deploy `8139e84`; lo script ha trovato 4 utenti, nessuno senza email, nome o cognome, e ha
  > applicato le regole; il PO ha corretto l'utente in produzione e l'ha riattivato.

## 35. Affidabilità e interesse al progetto delle aziende (Sprint 18)

Sul collaudo del PC, prima del merge (http://localhost:8090, password `collaudo-nexus-2026`), come `operatore.cc`. Nei
dati finti tre aziende hanno già una valutazione, come le aziende convertite dal database attuale: «Demo Logistica
S.r.l.» con 3 stelle e 2 pollici, «Demo Ristorazione S.n.c.» con 2 stelle, «Demo Bar del Centro» con un pollice. In
produzione, dopo il deploy, c'è solo T-35.6.

- [x] **T-35.1 Stelle e pollici nell'elenco (US-514)**
  1. Apri «Aziende». **Atteso:** due colonne nuove, «Affidabilità» e «Interesse al progetto»:
     - «Demo Logistica S.r.l.»: tre stelle piene e una vuota, due pollici pieni;
     - «Demo Ristorazione S.n.c.»: due stelle piene e due vuote, un trattino per l'interesse;
     - «Demo Bar del Centro»: un trattino per l'affidabilità, un pollice pieno e uno vuoto;
     - le altre aziende: due trattini.

- [x] **T-35.2 Filtri (US-514)**
  1. Nel menu «Affidabilità» scegli «Almeno 2 stelle». **Atteso:** solo «Demo Logistica S.r.l.» e «Demo Ristorazione
     S.n.c.».
  2. Scegli «Almeno 3 stelle». **Atteso:** solo «Demo Logistica S.r.l.».
  3. Rimetti «Qualunque»; nel menu «Interesse al progetto» scegli «Almeno un pollice». **Atteso:** «Demo Logistica
     S.r.l.» e «Demo Bar del Centro». Con «Due pollici», solo «Demo Logistica S.r.l.».
  4. Premi Indietro del browser. **Atteso:** torni al filtro di prima.

- [x] **T-35.3 Stelle e pollici nella scheda (US-514)**
  1. Apri «Demo Servizi Pulizia Soc. Coop.». **Atteso:** nella sezione «Valutazione del Call Center» quattro stelle
     vuote, bianche con il contorno, e due pollici vuoti.
  2. Clicca la terza stella e il primo pollice, poi «Salva modifiche». **Atteso:** le prime tre stelle e il primo
     pollice si riempiono; dopo il salvataggio «Modifiche salvate.», e nell'elenco l'azienda ha tre stelle e un
     pollice.
  3. Riapri la scheda, premi «Togli» accanto ai pollici e salva. **Atteso:** i pollici tornano vuoti; nell'elenco
     l'interesse è un trattino.
  4. Passa al tema scuro con il pulsante 🌙 nella barra in alto (US-1312; prima di quella storia, con il tema scuro del
     PC). **Atteso:** stelle e pollici, pieni e vuoti, si vedono bene anche lì.

- [x] **T-35.4 Da tastiera (US-514)**
  1. Nella scheda di un'azienda arriva alle stelle con il tasto Tab. **Atteso:** un riquadro evidenzia la stella su cui
     sei.
  2. Premi le frecce a destra e a sinistra. **Atteso:** la scelta si sposta di una stella alla volta e le stelle si
     riempiono fino a lì.

- [x] **T-35.5 Nome senza i simboli del database attuale (US-514)**
  1. Nella scheda di un'azienda scrivi come ragione sociale `$$ Demo Servizi` e salva. **Atteso:** il modulo non salva
     e dice «La ragione sociale non può iniziare con $, £, & o ^: per affidabilità e interesse al progetto usa stelle e
     pollici».
  2. Prova anche con `& Demo Servizi`. **Atteso:** lo stesso messaggio. Una «&» dentro il nome, come `Rossi & C.`, va
     bene.
  > Nota (26/11): T-35.1 … T-35.5 riuscite dal PO sul suo PC, con il collaudo della PR #208 (`24e41e0`). Richieste dal
  > collaudo: sotto «Valutazione del Call Center» solo «Facoltativa.» (fatto nella stessa PR); «Torna all'elenco» in
  > cima e in fondo alle schede e il pulsante 🌙/☀️ nella barra in alto, perché F12 non cambia il tema (US-1312). Il
  > tema scuro di T-35.3 si riprova con il pulsante.

- [x] **T-35.6 In produzione, dopo il deploy (US-514)**
  Il deploy converte i nomi delle aziende importate: i simboli all'inizio diventano stelle e pollici. Si consulta
  soltanto, come ADMIN, senza modificare niente.
  1. In «Aziende», menu «Affidabilità». **Atteso**, se nel frattempo nessuno le ha cambiate:
     - «Almeno 1 stella»: 271 aziende;
     - «Almeno 2 stelle»: 128;
     - «Almeno 3 stelle»: 21.
  2. Rimetti «Qualunque»; nel menu «Interesse al progetto» scegli «Almeno un pollice», poi «Due pollici». **Atteso:** 90
     aziende, poi 1.
  3. Rimetti i menu su «Qualunque» e cerca `$`, poi `£`. **Atteso:** nessuna azienda trovata: nessun nome contiene più
     i simboli.
  4. Apri un'azienda che conosci e che nel database attuale aveva i simboli. **Atteso:** il nome è pulito, con le
     stelle e i pollici di prima.
  > Nota (26/11): T-35.6 riuscita dal PO in produzione, dopo il deploy `1603fcc`: i conteggi tornano.

## 36. Tema a scelta e intestazione fissa delle schede (Sprint 18)

Sul collaudo del PC, prima del merge (http://localhost:8090, password `collaudo-nexus-2026`), come `operatore.cc`, con il
PC nel tema chiaro.

- [x] **T-36.1 Pulsante del tema (US-1312)**
  1. Guarda la barra in alto. **Atteso:** accanto all'utente c'è una luna 🌙.
  2. Premi la luna. **Atteso:** tutta l'interfaccia passa ai colori scuri, e al posto della luna c'è un sole ☀️.
  3. Apri «Aziende» e una scheda. **Atteso:** anche le altre pagine sono scure. Rifai T-35.3, passo 4: stelle e
     pollici si vedono bene.
  4. Premi il sole. **Atteso:** torna il tema chiaro, con la luna.

- [x] **T-36.2 Scelta ricordata (US-1312)**
  1. Passa al tema scuro, chiudi il browser e riapri http://localhost:8090. **Atteso:** NEXUS si apre già scuro,
     senza mostrare prima la pagina chiara.
  2. Rimetti il tema chiaro con il sole.

- [x] **T-36.3 Intestazione fissa delle schede (US-1312)**
  1. Apri la scheda di un'azienda. **Atteso:** sopra il titolo c'è «← Torna all'elenco delle aziende».
  2. Scorri la scheda fino in fondo. **Atteso:** il ritorno e il titolo restano fermi sotto la barra in alto; in fondo
     non c'è un secondo ritorno. Il ritorno riporta all'elenco.
  3. Con il tasto Tab scorri i campi del modulo. **Atteso:** il campo con il riquadro del focus non finisce mai sotto
     l'intestazione fissa.
  4. Apri una segnalazione e il beneficiario di una segnalazione. **Atteso:** anche lì l'intestazione è fissa
     («Torna alle segnalazioni», «Torna alla segnalazione n. …»).
  5. Come `admin`, apri «Utenti», poi «Nuovo utente» e la pagina «Dati» di un utente. **Atteso:** «Torna agli
     utenti» solo in cima, nell'intestazione fissa; in fondo c'è solo il pulsante del modulo.
  6. Restringi la finestra alla larghezza di uno smartphone. **Atteso:**
     - nella barra in alto logo, menu, campanella, luna ed «Esci» stanno su una riga senza sovrapporsi; sotto i 375 px
       resta solo il logo, senza la scritta;
     - l'intestazione della scheda resta fissa.
  7. Riduci molto l'altezza della finestra (o ingrandisci il testo a 200% e più). **Atteso:** con poco spazio in
     altezza l'intestazione della scheda scorre con la pagina, per non coprirla.
  > Nota (26/11): T-36.1 … T-36.3 riuscite dal PO sul suo PC, con il collaudo della PR #209 (`dbf6dae`), insieme al
  > passo 4 di T-35.3 con il pulsante del tema. Nella modalità mobile di Chrome, con la finestra ridotta, l'intestazione
  > scorreva: è voluto (finestra alta meno di circa 512 px), e il PO ha deciso di tenere questa soglia (scelta A).

## 37. Zona dell'azienda e aziende per il Tutor (Sprint 18)

Sul collaudo del PC, prima del merge (http://localhost:8090, password `collaudo-nexus-2026`). Nei dati finti tre aziende
hanno già una zona: «Demo Logistica S.r.l.» Firenze Rifredi, «Demo Ristorazione S.n.c.» Firenze San Lorenzo, «Demo
Servizi Pulizia Soc. Coop.» Firenze Galluzzo.

- [x] **T-37.1 Zona nella scheda e nell'elenco del Call Center (US-511)**
  1. Entra come `operatore.cc` e apri «Aziende». **Atteso:** una colonna «Zona», con le tre zone dei dati finti e un
     trattino per le altre aziende.
  2. Nel menu «Zona» scegli «Firenze Rifredi». **Atteso:** solo «Demo Logistica S.r.l.».
  3. Rimetti «Tutte le zone», apri «Demo Commercio al Dettaglio S.p.A.», scegli la zona «Firenze Isolotto» e salva.
     **Atteso:** «Modifiche salvate.»; nell'elenco l'azienda ha la zona Firenze Isolotto.

- [x] **T-37.2 Aziende per il Tutor (US-511)**
  1. Esci ed entra come `tutor1`. **Atteso:** nel menu c'è «Aziende».
  2. Apri «Aziende». **Atteso:** l'elenco delle aziende attive, con ragione sociale, categoria, zona, indirizzo con il
     comune, stelle e pollici. Non ci sono partita IVA, telefoni, email, referenti né il pulsante «Nuova azienda», e i
     nomi non si aprono.
  3. Cerca «demo», poi scegli la categoria «Bar / Pub». **Atteso:** solo «Demo Bar del Centro».
  4. Rimetti «Tutte le categorie» e scegli la zona «Firenze Rifredi». **Atteso:** solo «Demo Logistica S.r.l.», con 3
     stelle e 2 pollici.
  5. Rimetti «Tutte le zone» e cerca la partita IVA `99000000001`. **Atteso:** «Nessuna azienda corrisponde alla
     ricerca.»: il Tutor non cerca per partita IVA.

- [x] **T-37.3 Aziende disattivate e sede operativa (US-511)**
  1. Come `operatore.cc` disattiva «Demo Commercio al Dettaglio S.p.A.» (in fondo alla scheda, «Disattiva azienda»).
  2. Come `tutor1` riapri «Aziende». **Atteso:** «Demo Commercio al Dettaglio S.p.A.» non c'è più.
  3. Guarda «Demo Logistica S.r.l.», che ha una sede operativa. **Atteso:** l'indirizzo è quello della sede operativa,
     «Via del Magazzino 10, Prato (PO)», non quello della sede legale. Le altre aziende mostrano la sede legale.
  > Nota (26/11): T-37.1 … T-37.3 riuscite dal PO sul suo PC, con il collaudo della PR #212 (`a6cbc78`). Richiesta dal
  > collaudo: un pulsante per azzerare i filtri, che sono parecchi (T-37.4).

- [x] **T-37.4 «Azzera i filtri» (US-511)**
  1. Come `operatore.cc`, in «Aziende» cerca «demo», scegli «Almeno 2 stelle», spunta «Mostra anche le aziende
     disattivate» e scegli 50 aziende per pagina. **Atteso:** sotto i filtri compare «Azzera i filtri».
  2. Premi «Azzera i filtri». **Atteso:** ricerca e filtri tornano vuoti, l'elenco riparte dalla prima pagina con tutte
     le aziende attive, sempre 50 per pagina; il cursore è nel campo di ricerca; il pulsante sparisce.
  3. Senza filtri. **Atteso:** «Azzera i filtri» non c'è.
  4. Come `tutor1`, in «Aziende» scegli una zona e una categoria, poi «Azzera i filtri». **Atteso:** lo stesso.
  > Nota (26/11): T-37.4 riuscita dal PO sul suo PC, con il collaudo della PR #212 (`5da6e36`); deploy `3a64364`.

## 38. Valutazione dell'azienda alla fine del tirocinio (Sprint 18)

Sul collaudo del PC, prima del merge (http://localhost:8090, password `collaudo-nexus-2026`). Nei dati finti:
- «Demo Logistica S.r.l.» ha già due tirocini valutati: di `tutor1` (4, 5, 4, con una nota) e di `tutor2` (5, 5, 5);
- `tutor1` ha una segnalazione in tirocinio presso «Demo Servizi Pulizia Soc. Coop.» (beneficiaria «Demo Otto»), con il
  report di restituzione da compilare.

- [x] **T-38.1 Valutazioni nella scheda del Call Center (US-1104)**
  1. Entra come `operatore.cc` e apri «Demo Logistica S.r.l.». **Atteso:** la sezione «Valutazioni dei tirocini» con la
     faccina «Ottima · 4,7» e «Media di 2 tirocini valutati».
  2. Guarda l'elenco. **Atteso:** prima la valutazione più recente, «Ottima · 5,0», tirocinio finito il 31/10/2026, di
     Tutor Due, senza nota; poi «Buona · 4,3», finito il 30/09/2026, di Tutor Uno, con la nota. Nessun nome di
     beneficiario.
  3. Apri un'altra azienda. **Atteso:** «Nessun tirocinio valutato.»

- [x] **T-38.2 Valutazione nel report di restituzione (US-1104)**
  1. Esci ed entra come `tutor1`; apri la segnalazione di «Demo Otto», in tirocinio, e il report di restituzione.
     **Atteso:** la sezione «Valutazione dell'azienda» con tre domande, ognuna con cinque faccine e una parola, da
     «Pessima» a «Ottima».
  2. Scegli «Buona», «Ottima», «Buona». **Atteso:** compare «Media: Buona · 4,3»; sotto c'è il campo «Nota sulla
     valutazione», facoltativo.
  3. Scegli «Ottima», «Ottima», «Buona». **Atteso:** «Media: Ottima · 4,7»; la nota resta (dal 26/11, richiesta del PO
     dopo il collaudo).
  4. Torna a «Buona», «Ottima», «Buona», scrivi una nota (senza dati personali), compila il resto e invia. La «Fine
     effettiva del tirocinio» parte vuota, perché questo tirocinio dimostrativo non ha la scheda: scrivi una data.
     **Atteso:** il report inviato mostra le tre faccine, la media e la nota.
  5. Prova a inviare un report senza una delle tre faccine (puoi farlo prima del passo 4). **Atteso:** il riepilogo
     degli errori indica la domanda senza risposta.

- [x] **T-38.3 La valutazione nuova nella scheda dell'azienda (US-1104)**
  1. Come `operatore.cc` apri «Demo Servizi Pulizia Soc. Coop.». **Atteso:** «Valutazioni dei tirocini» con la tua
     valutazione, il tuo nome da Tutor (Tutor Uno) e la nota.

- [x] **T-38.4 Le valutazioni per il Tutor (US-1104)**
  1. Come `tutor2` apri «Aziende». **Atteso:** una colonna «Valutazioni dei tirocini» con la faccina e il numero di
     tirocini, per «Demo Logistica S.r.l.» e «Demo Servizi Pulizia Soc. Coop.»; un trattino per le altre.
  2. Premi il nome «Demo Logistica S.r.l.». **Atteso:** la scheda in sola lettura, con categoria, zona, indirizzo,
     stelle, pollici e le valutazioni con i nomi dei Tutor; niente partita IVA, recapiti o pulsanti per modificare.
  3. Restringi la finestra alla larghezza di uno smartphone. **Atteso:** faccine e testi leggibili, senza scorrere di
     lato.
  > Nota (26/11): T-38.1 … T-38.4 riuscite dal PO sul suo PC, con il collaudo della PR #214 (`e35941e`). Richieste dal
  > collaudo: un testo neutro sotto «Valutazioni dei tirocini» e la nota sempre presente nel report, anche con
  > «Ottima» (fatte nella stessa PR). Unita con la #214 (`5095004`) e in produzione con il deploy del 26/11 sera.

## 39. Azienda «nave scuola» (Sprint 18)

Sul collaudo del PC, prima del merge (http://localhost:8090, password `collaudo-nexus-2026`; le email in Mailpit,
http://localhost:8025). Nei dati finti «Demo Ristorazione S.n.c.» è già una nave scuola, e `tutor1` ha una segnalazione
in tirocinio presso «Demo Servizi Pulizia Soc. Coop.» (beneficiaria «Demo Otto»), seguita da `operatore.cc`, con il
report di restituzione da compilare. La nave scuola è un bollino oro con la nave verde, dopo il nome dell'azienda:
passandoci sopra con il mouse compare «Nave scuola».

- [x] **T-39.1 Nave scuola nella scheda e nell'elenco delle aziende (US-515)**
  1. Entra come `operatore.cc` e apri «Aziende». **Atteso:** la nave accanto a «Demo Ristorazione S.n.c.», e a nessun
     altro nome.
  2. Spunta «Solo le navi scuola». **Atteso:** si vede solo «Demo Ristorazione S.n.c.»; compare «Azzera i filtri».
  3. Azzera i filtri, apri «Demo Logistica S.r.l.» e nella «Valutazione del Call Center» spunta «Nave scuola», con il
     suo aiuto sotto; salva. **Atteso:** la nave accanto al nome nel titolo della scheda e nell'elenco.
  4. Togli la spunta e salva. **Atteso:** la nave non c'è più.

- [x] **T-39.2 La nave dove si sceglie l'azienda (US-515)**
  1. Come `operatore.cc` apri la bacheca e scegli «Pubblicati». **Atteso:** l'annuncio «Tirocinio cameriere di sala»
     ha la nave accanto a «Demo Ristorazione S.n.c.», anche nel suo dettaglio.
  2. Apri una segnalazione in lavorazione e cerca le mansioni compatibili in tutte le zone e tipologie. **Atteso:** le
     mansioni di «Demo Ristorazione S.n.c.» hanno la nave accanto al nome dell'azienda.

- [x] **T-39.3 Proposta nel report di restituzione (US-515)**
  1. Esci ed entra come `tutor1`; apri la segnalazione di «Demo Otto» e il report di restituzione. **Atteso:** nella
     «Valutazione dell'azienda», dopo la nota, la domanda «L'azienda è adatta come nave scuola?», senza «obbligatorio»,
     con l'aiuto che spiega che cos'è una nave scuola e che decide il Call Center.
  2. Compila il report rispondendo «Sì» e invialo. La «Fine effettiva del tirocinio» parte vuota: scrivi una data.
     **Atteso:** il report inviato mostra «Proposta come nave scuola: Sì».

- [x] **T-39.4 L'avviso all'operatore e il diario dell'azienda (US-515)**
  1. Esci ed entra come `operatore.cc`; apri le notifiche. **Atteso:** «Nel report di restituzione della segnalazione
     n. … il Tutor propone l'azienda del tirocinio come nave scuola: decidi tu dalla scheda dell'azienda.», con il
     collegamento «Apri la scheda dell'azienda della segnalazione n. …».
  2. Premi il collegamento. **Atteso:** la scheda di «Demo Servizi Pulizia Soc. Coop.», ancora senza la nave: decidi
     tu. Nelle note c'è «Proposta come nave scuola dal Tutor, nel report di restituzione della segnalazione n. …», di
     Tutor Uno, con la data di oggi.
  3. Apri Mailpit. **Atteso:** l'email a `operatore.cc` con l'oggetto «PORTALE NEXUS - Segnalazione n. … - Proposta di
     nave scuola», il collegamento alla scheda dell'azienda (non alla segnalazione) e la firma «Portale Nexus».
  4. Come `admin`, apri «Testi dei messaggi». **Atteso:** c'è «Proposta di nave scuola», con il testo nell'app,
     l'oggetto e il testo dell'email, che si possono cambiare come gli altri.

- [x] **T-39.5 La nave per il Tutor (US-515)**
  1. Come `tutor2` apri «Aziende». **Atteso:** la nave accanto a «Demo Ristorazione S.n.c.»; apri la sua scheda: la
     nave anche nel titolo.
  2. Apri la bacheca dei Tutor. **Atteso:** nessuna nave e nessun nome di azienda: la bacheca resta anonima.
  3. Prova il tema scuro con il pulsante 🌙 in alto e restringi la finestra alla larghezza di uno smartphone.
     **Atteso:** la nave si vede bene e i nomi non escono dalla pagina.
  > Nota (26/11): T-39.1 … T-39.5 riuscite dal PO sul suo PC, con il collaudo della PR #216 (`874e48c`). Richiesta dal
  > collaudo: il simbolo era troppo poco evidente; ora è un bollino oro con la nave verde scura, più grande e staccato
  > dal nome, in tutti gli elenchi e nei titoli delle schede (fatto nella stessa PR, T-39.6).

- [x] **T-39.6 Il bollino della nave scuola ben visibile (US-515)**
  1. Come `operatore.cc`, guarda «Demo Ristorazione S.n.c.» in «Aziende», nella bacheca («Pubblicati») e nelle mansioni
     compatibili di una segnalazione in lavorazione. **Atteso:** dopo il nome, un po' staccato, un bollino rotondo oro
     con il bordo e la nave verde scuro, ben visibile; nel titolo della scheda dell'azienda è più grande.
  2. Come `tutor2`, in «Aziende» e nella scheda. **Atteso:** lo stesso bollino.
  3. Passa al tema scuro con il pulsante 🌙. **Atteso:** il bollino oro si vede bene anche sul fondo scuro.

- [x] **T-39.7 La ragione sociale in evidenza negli elenchi (US-515)**
  1. Come `operatore.cc`, in «Aziende» crea un'azienda finta con un nome lungo, per esempio «Prova Cooperativa Sociale
     Servizi Integrati» (43 caratteri), con la spunta «Nave scuola». **Atteso:** nell'elenco non ci sono più le colonne
     «Telefono» ed «Email»; la colonna «Ragione sociale» è la più larga e il nome sta su una riga, con il bollino.
  2. Crea un'altra azienda finta con un nome di 50 caratteri, il massimo del database attuale. **Atteso:** solo questo
     va a capo.
  3. Come `tutor2`, apri «Aziende». **Atteso:** anche qui la colonna «Ragione sociale» è la più larga e i nomi stanno su
     una riga.
  4. Restringi la finestra alla larghezza di uno smartphone. **Atteso:** come prima, la tabella scorre di lato e i nomi
     restano su una riga.
  > Nota (26/11): T-39.6 e T-39.7 riuscite dal PO sul suo PC, con il collaudo della PR #216 (`8c610e8`). Unita con la
  > #216 (`119cb88`) e in produzione con il deploy del 26/11 sera.

## 40. Richiesta di contatto di un'azienda dal Tutor (Sprint 19)

Sul collaudo del PC, prima del merge (http://localhost:8090, password `collaudo-nexus-2026`). La richiesta di contatto
si fa solo per un'azienda che non è ancora in NEXUS (PO, 27/11). Nei dati finti `tutor1` ha già due richieste: una
aperta, per «Officina Meccanica Demo del Ponte», e una in carico a `operatore.cc`, per «Bar Pasticceria Demo Stella».

- [ ] **T-40.1 Le richieste per il Call Center (US-507)**
  1. Entra come `operatore.cc`. **Atteso:** nel menu la voce «Richieste di contatto» con il numero 1.
  2. Aprila. **Atteso:** in cima l'officina, aperta, da prendere in carico; sotto il bar, già in carico. Per ognuna il
     Tutor (Tutor Uno), la zona, lo stato e l'operatore. Dal 27/11, richiesta del PO: le richieste da prendere in carico
     stanno in cima.
  3. Premi «Prendi in carico» sull'officina. **Atteso:** «Hai preso in carico la richiesta di contatto n. …»; la
     richiesta mostra il tuo nome e il numero nel menu sparisce.

- [x] **T-40.2 Nessuna richiesta per le aziende già in NEXUS (US-507)**
  1. Esci ed entra come `tutor2`; apri «Aziende» e la scheda di «Demo Ristorazione S.n.c.». **Atteso:** nessun pulsante
     per chiedere di contattarla: è già in anagrafica.
  2. Come `tutor1`, apri «Richieste» nel menu. **Atteso:** la pagina «Le mie richieste» con le sue due richieste, tutte
     e due in carico all'operatore.

- [ ] **T-40.3 Richiesta per un'azienda nuova, con i nomi simili (US-507)**
  1. Come `tutor2`, apri «Richieste», premi «Chiedi di contattare un'azienda nuova» e poi «Invia la richiesta» senza
     scrivere niente. **Atteso:** il riepilogo indica il nome e la zona; la categoria è facoltativa.
  2. Scrivi «Demo Ristorazioni», scegli una zona e invia. **Atteso:** prima dell'invio compare «Aziende con un nome
     simile», con «Demo Ristorazione S.n.c.», il suo bollino della nave scuola e il collegamento alla scheda. Il testo
     dice che, se è quella, è già in anagrafica e la richiesta non serve. Ci sono solo «No, è un'azienda nuova: invia
     la richiesta» e «Correggi il nome».
  3. Premi «Correggi il nome», scrivi un nome che non somiglia a nessuno, per esempio «Ferramenta Lunghi e Figli», e
     invia. **Atteso:** la richiesta parte subito e compare in «Le mie richieste», aperta.
  4. Come `operatore.cc`, apri «Richieste di contatto». **Atteso:** in cima la richiesta di `tutor2`, aperta, da
     prendere in carico, sopra le due già in carico; nel menu il numero 1.

- [x] **T-40.4 Menu, smartphone e tema scuro (US-507)**
  1. Come `admin`, apri «Amministrazione» nel menu. **Atteso:** in testa «Richieste di contatto», con il numero di
     quelle da prendere in carico; la riga del menu era piena.
  2. Con la finestra larga meno di 1280 pixel. **Atteso:** il menu è il pulsante ☰, con le voci, nome e ruolo dentro;
     prima, tra 1024 e 1279 pixel, le voci dell'ADMIN finivano sotto la campanella.
  3. Prova le due pagine delle richieste con la finestra stretta come uno smartphone e con il tema scuro (🌙).
     **Atteso:** tutto leggibile; la tabella scorre di lato e i nomi delle aziende restano su una riga.
  > Nota (27/11): dal collaudo della PR #219 (`d834151`) il PO ha trovato coerenti le prove. Ha chiesto che le richieste
  > da prendere in carico stiano in cima: per T-40.1 e T-40.3 resta da riprovare solo l'ordine dell'elenco (passi 2 e
  > 4). T-40.2 e T-40.4 riuscite.
