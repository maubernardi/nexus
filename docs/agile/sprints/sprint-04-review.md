# Sprint 4 — Review, retrospettiva e planning

**Sprint:** 25 – 27 ottobre 2026 (planning 24/10) · **Review:** 27/10/2026 · **Partecipanti:** Mauro Bernardi (Product
Owner), team di sviluppo (Claude Code) · **Demo:** https://portalenexus.it, versione `d08e6dc` (tutto lo Sprint 4).

> Documento unico e asincrono: review (§1–§6), retrospettiva (§7), decisioni da prendere (§8) e proposta di planning
> dello Sprint 5 (§9). Ne discutiamo un punto alla volta, quando il PO è disponibile.

---

## 1. Sintesi

| | |
|---|---|
| **Obiettivo dello sprint** | L'operatore sceglie la proposta conoscendo storico e note dell'azienda (R1 chiuso); il Tutor accetta la proposta e parte il timer di 7 giorni lavorativi. |
| **Esito** | ✅ **Raggiunto.** Lo sviluppo di **R1 è completo** (resta US-1301, a cura del PO); il ciclo prosegue in R2 fino all'accettazione con timer. In più la nuova tipologia dal Tutor (US-302), aggiunta dal PO. |
| **Impegno / completato** | 24 punti / **24 punti** (7 storie su 7; 19 al planning + US-302 aggiunta il 25/10) |
| **Prima dello sprint** | Decisioni della review dello Sprint 3 (§8.1, §8.3) in [#40](https://github.com/maubernardi/nexus/pull/40), senza punti |
| **Velocity** | Sprint 1: 21 · Sprint 2: 25 · Sprint 3: 21 · Sprint 4: **24** · media 23 |
| **PR unite** | 12 (#40–#51): 7 di funzionalità, 1 di correzione, 4 di documentazione |
| **Migrazioni del database** | V15 (note dell'azienda), V16 (calendario unico), V17 (stato "In attesa di approvazione" e avviso al Tutor) |
| **Deploy della demo** | 25/10 (due), 26/10, 27/10 (due): la demo è sempre stata allineata a `main` (azione A1) |

## 2. Cosa è stato consegnato

| ID | Storia | Punti | PR | In una frase |
|---|---|---|---|---|
| US-505 | Diario delle note dell'azienda | 2 | [#42](https://github.com/maubernardi/nexus/pull/42) | Note datate con autore nella scheda azienda; non si modificano né si cancellano (anche il database lo impedisce); nell'audit senza testo. |
| US-503 | Storico e pregressi dell'azienda | 3 | [#44](https://github.com/maubernardi/nexus/pull/44) | Riepilogo sempre completo (proposte, accettate, appuntamenti falliti, tirocini, assunzioni, esiti negativi) ed eventi dalla più recente, esclusioni comprese. |
| US-604 | Dettagli di azienda e mansione durante l'abbinamento | 5 | [#45](https://github.com/maubernardi/nexus/pull/45) | "Dettagli" accanto a "Proponi": finestra accessibile con azienda, mansione, note e pregressi; da lì si propone con la stessa conferma. |
| US-107 | Calendario unico (patrono e chiusure) | 3 | [#47](https://github.com/maubernardi/nexus/pull/47) | Pagina "Calendario" dell'ADMIN: patrono (24 giugno) e chiusure, con audit; un solo calendario per tutti i progetti. |
| US-703 | Calendario dei giorni lavorativi | 3 | [#48](https://github.com/maubernardi/nexus/pull/48) | Festività calcolate (Pasqua compresa, senza il 4 ottobre), patrono e chiusure; "Prova del calcolo" per l'ADMIN. |
| US-701 | Accettazione della proposta | 3 | [#49](https://github.com/maubernardi/nexus/pull/49) | Il Tutor della segnalazione accetta con conferma: "Proposta accolta" e scadenza al 7° giorno lavorativo, visibile a tutti. |
| US-302 | Nuova tipologia di mansione dal Tutor | 5 | [#51](https://github.com/maubernardi/nexus/pull/51) | Il Tutor descrive una tipologia nuova; il Call Center la approva (entra nel catalogo) o sceglie quella giusta con motivo; avviso "Tipologia cambiata". |

### Il flusso dopo lo Sprint 4

```
Tutor                                            Call Center
  segnala (anche con una tipologia nuova) ──────► Da approvare ► approva / sceglie dal catalogo ──┐
                                                  Da prendere in carico ◄──────────────────────────┘
                                                    │ Mansioni compatibili ► Dettagli (azienda, note, pregressi)
  Proposta all'azienda ◄────────────────────────────┘ Proponi ► Conferma
  │ Accetta la proposta ► Conferma
  ▼
  Proposta accolta · scadenza: 7° giorno lavorativo (calendario unico)
```

## 3. Demo guidata (sul sito)

Utenti demo: `tutor1`, `tutor2` (progetto GOL), `operatore.cc`, `admin`.

1. **ADMIN** — *Calendario*: patrono 24 giugno; aggiungere una chiusura (es. un ponte) e usare *Prova del calcolo*
   con la data di oggi: promemoria e scadenza saltano fine settimana, festività e chiusura.
2. **Call Center** — *Segnalazioni* → vista **Da approvare**: la n. 2 ("Tecnico del suono") → nel dettaglio approvarla
   (anche correggendo il nome) oppure scegliere una tipologia del catalogo con un motivo.
3. **Tutor** (`tutor1`) — se la tipologia è stata cambiata, nella Home la segnalazione ha **"Tipologia cambiata"**;
   aprendola compare l'avviso e la cronologia spiega il motivo; tornando alla Home l'evidenza è sparita.
4. **Tutor** — *Nuova segnalazione* → "La mansione non è nell'elenco: la descrivo io" → invio: "In attesa di
   approvazione".
5. **Call Center** — *Aziende* → una scheda: sezioni **Note** (aggiungerne una) e **Storico e pregressi**.
6. **Call Center** — una segnalazione presa in carico → mansioni compatibili → **Dettagli**: azienda, mansione, note e
   pregressi; *Proponi* dalla finestra → conferma.
7. **Tutor** — la segnalazione proposta → **Accetta la proposta** → conferma: stato "Proposta accolta" con la scadenza;
   la vede anche il Call Center; nello storico dell'azienda conta tra le accettate.

## 4. Qualità

### Test automatici

| | Fine Sprint 3 | Fine Sprint 4 |
|---|---|---|
| Backend – unit | 27 | 52 |
| Backend – integrazione (PostgreSQL reale) | 149 | 185 |
| Frontend – componenti (tutti con controllo axe) | 73 | 93 |

CI verde su tutte le PR. I nuovi vincoli del database sono testati per nome (`ck_company_note_text`,
`ck_work_calendar_*`, `ck_calendar_closure_*`), così come la sola aggiunta di `company_note`.

### Accessibilità (WCAG 2.2 AA)

Ogni pagina nuova o cambiata è stata verificata in un browser reale (Chromium + axe) a 320 e 1280 px, in chiaro e
scuro: note e storico dell'azienda, finestra "Dettagli" (focus intrappolato, Esc, ritorno del focus, schermo intero su
mobile), Calendario, accettazione della proposta, decisione sulla nuova tipologia. **0 violazioni**, nessuno
scorrimento laterale.

### Difetti trovati e corretti durante lo sprint (prima del merge)

| Difetto | Dove | Come è emerso | Correzione |
|---|---|---|---|
| Caselle del riepilogo troppo strette ("Appuntamenti falliti" debordava) | Storico nella finestra "Dettagli" | Screenshot in browser | Colonne secondo la larghezza della sezione (container query) |
| Etichetta "Zona di residenza" per la zona della mansione | Finestra "Dettagli" | Revisione dello screenshot | Etichetta "Zona" |
| Il focus non tornava su "Dettagli" chiudendo la finestra | Finestra "Dettagli" | Test automatico | Ritorno del focus gestito esplicitamente |
| Pulsante "Approva e aggiungi al catalogo" oltre il bordo a 320 px | Dettaglio della segnalazione | Misura in browser | Il testo va a capo |
| Primo deploy fallito (SHA abbreviato) | Workflow di deploy | Log di GitHub Actions | SHA completo; nessun effetto sul server |

Nessun difetto noto sul codice unito.

### Note operative

- Le **prove di archiviazione** delle change prima di ogni PR (azione A2) hanno funzionato: nessuna archiviazione
  fallita in questo sprint.
- Un deploy è rimasto ~10 minuti in coda su GitHub Actions prima di partire: nessun impatto, solo attesa.

## 5. Decisioni prese durante lo sprint

| # | Decisione | Presa da | Dove |
|---|---|---|---|
| D1 | Calendario **unico** per tutti i progetti (non per progetto); il 4 ottobre non è festivo | PO (planning) | US-107, US-703 |
| D2 | Riepilogo dei pregressi con tutte le voci, a zero finché mancano i dati | PO (planning) | US-503 |
| D3 | "Nuova segnalazione" e "Nuovo beneficiario" come pulsanti in cima agli elenchi; menu del Tutor: Segnalazioni, Beneficiari | PO (review Sprint 3) | navigazione |
| D4 | US-302 aggiunta all'impegno (24 punti) | PO (25/10) | Sprint 4 |
| D5 | Accetta la proposta **solo il Tutor della segnalazione**: i tutor non sono intercambiabili come gli operatori | PO (27/10) | US-701 |
| D6 | Le note dell'azienda non si modificano né si cancellano; una correzione è una nuova nota | Team (dalla storia) | US-505 |
| D7 | "Esiti negativi" = tirocini conclusi senza assunzione | Team → PO | US-503 — ✅ confermata (§8.1) |
| D8 | Scadenza del timer alla fine del 7° giorno lavorativo, ora di Roma; il giorno dell'accettazione non conta | Team | US-701, US-703 |
| D9 | "Prova del calcolo" nella pagina Calendario, per verificare US-703 prima di US-701 | Team | US-703 |
| D10 | Una tipologia nuova con il nome di una già presente va scelta dall'elenco; il codice del catalogo si ricava dal nome | Team | US-302 |
| D11 | Stato `IN_ATTESA_APPROVAZIONE_ADMIN` rinominato `IN_ATTESA_APPROVAZIONE` (lo decide anche l'operatore) | PO (note) → Team | US-302 |

## 6. Stato del prodotto rispetto alla roadmap

**Rilascio R1** — *"il tutor registra e segnala un beneficiario; il Call Center lo prende in carico e lo abbina a una
mansione"*: **sviluppo completo**. Per chiuderlo resta **US-1301** (verifica della PWA su smartphone, a cura del PO).

**Rilascio R2** — *proposta, timer, appuntamenti, bacheca e fast-track*: fatti US-107, US-703, US-701, US-302
(14 punti). Restano 16 voci, 52 punti:

| Gruppo | Storie | Punti |
|---|---|---|
| Dopo l'accettazione: timer e appuntamento | US-702 rifiuto della proposta (3), US-704 promemoria (3), US-705 scadenza (3), US-801 appuntamento preso (2), US-802 appuntamento fallito (2) | 13 |
| Notifiche | US-1201 notifiche in app (5) | 5 |
| Bacheca e fast-track | US-803 rilancio (5), US-901 (3), US-902 (5), US-903 (2), US-904 (3), US-306 (3), US-403 (5), US-404 (3) | 29 |
| Altro | US-603 esclusione di un'azienda (2), US-106 cataloghi (3) | 5 |

**Product backlog:** 65 elementi attivi, 230 punti; completati 28 elementi (91 punti) negli Sprint 1–4.

## 7. Retrospettiva

**Cosa ha funzionato**
- Impegno sopra la media (24 punti) completato nei 3 giorni, con PR piccole e una per storia.
- Le azioni dello Sprint 3 sono diventate abitudine: deploy dopo ogni gruppo di merge (A1), prova di archiviazione delle
  change (A2), stati del backlog solo nelle PR di documentazione (A4).
- Le decisioni del PO sono arrivate in tempo e sono state registrate dove servono (backlog, sprint, design delle change).
- La verifica in browser reale ha trovato 4 difetti prima del merge (§4).

**Cosa non ha funzionato**
- Un punto resta aperto da due PR: la definizione di "esiti negativi" (§8.1).
- Le nuove note operative del PO (25/10) sono state messe da parte su sua richiesta: vanno riprese prima che il loro
  contenuto (stati come filtro a tasti, ordine per numero, viste del Call Center) entri in conflitto con il lavoro sulla
  Home.
- Il calcolo delle scadenze e le pagine nuove richiedono molti dati di prova: alcuni test d'integrazione ripetono la
  stessa preparazione.

**Azioni**

| # | Azione | Chi |
|---|---|---|
| A5 | Riprendere le note operative del 25/10 (quando il PO le avrà completate) all'inizio dello Sprint 5 | PO + team |
| A6 | Chiudere i punti "da confermare" entro la PR di documentazione successiva al merge | Team (li elenca) + PO |
| A7 | Estrarre una preparazione comune per i test che creano segnalazioni in un certo stato (proposta, accettata) | Team |

## 8. Da decidere

### 8.1 "Esiti negativi" nel riepilogo dello storico (D7)
Oggi conta i **tirocini conclusi senza assunzione**. In alternativa si possono contare anche proposte rifiutate e
appuntamenti falliti, che però hanno già voci proprie.
*Proposta:* tenere la definizione attuale.

> ✅ **Deciso (PO, 27/10): proposta accettata.** "Esiti negativi" = tirocini conclusi senza assunzione; proposte
> rifiutate e appuntamenti falliti restano nelle loro voci.

### 8.2 Segnalazioni "riaperte"
Con US-702 (rifiuto della proposta), US-705 (scadenza del timer) e US-802 (appuntamento fallito) la segnalazione torna
al Call Center in **RIAPERTO**. Va deciso dove la ritrova l'operatore.
*Proposta:* resta **in carico allo stesso operatore** (vista "In carico a me", in cima, con l'evidenza "Riaperta") e si
lavora come una segnalazione in lavorazione: nuova ricerca delle compatibili, con l'azienda esclusa se il motivo lo
richiede. Alternativa: torna in "Da prendere in carico" per chiunque.

> ✅ **Deciso (PO, 27/10): proposta accettata.** La segnalazione riaperta resta **in carico allo stesso operatore**, in
> cima a "In carico a me" con l'evidenza "Riaperta", e si lavora come una in lavorazione (un collega può sempre
> intervenire, US-405). **Nessuna esclusione automatica** dell'azienda dopo un appuntamento fallito: decide l'operatore
> (US-603). Resta l'esclusione automatica al rifiuto della proposta da parte del Tutor (Q3).
>
> Per decidere, l'operatore ha il **motivo dell'appuntamento fallito** (PO, 27/10): il Tutor indica una causa da un menu
> ("L'azienda non ha confermato il tirocinio", "Il beneficiario ha rinunciato", "Il beneficiario non si è presentato",
> "Altro") e una nota, obbligatoria con "Altro"; il motivo va in cronologia e nello storico dell'azienda. Voci del menu
> provvisorie, da confermare con il committente. Criterio aggiunto a US-802 (resta 2 punti).

### 8.3 Notifiche in app (US-1201)
Le notifiche servono a promemoria, scadenza, rifiuto e tipologia cambiata. Q9 chiede app **ed email**: l'email è una
storia a parte (US-1202, R3).
*Proposta:* nello Sprint 5 solo le notifiche **in app** (campanella con contatore nell'intestazione, elenco, "segna come
letta"); l'avviso provvisorio di US-302 diventa una notifica.

> ✅ **Deciso (PO, 27/10): proposta accettata.** Nello Sprint 5 solo notifiche **in app**: campanella con il numero di
> non lette (annunciato agli screen reader), elenco dalla più recente con link alla segnalazione, segna come letta (una o
> tutte). Destinatari: la controparte del passaggio (Tutor ↔ operatore che ha in carico la segnalazione; la scadenza del
> timer a entrambi). L'avviso "Tipologia cambiata" di US-302 diventa una notifica. Nessun dato personale oltre al numero
> della segnalazione. L'email resta in US-1202 (R3).

### 8.4 Chiusura dello Sprint 4
*Proposta:* chiudere lo Sprint 4 con questa review (velocity 24) e avviare lo Sprint 5 alla conferma del planning.

> ✅ **Deciso (PO, 27/10): proposta accettata.** Sprint 4 chiuso il 27/10 con velocity 24; lo Sprint 5 parte alla
> conferma del planning, per 3 giorni.

## 9. Proposta per lo Sprint 5 (3 giorni, ~20 punti)

**Obiettivo proposto:** *dopo l'accettazione il percorso prosegue: promemoria e scadenza del timer, rifiuto della
proposta, esito dell'appuntamento; Tutor e operatori ricevono le notifiche in app.*

| Ordine | ID | Storia | Punti | Release |
|---|---|---|---|---|
| 1 | US-1201 | Notifiche in app | 5 | R2 |
| 2 | US-801 | Appuntamento preso (il timer si ferma) | 2 | R2 |
| 3 | US-802 | Appuntamento fallito (RIAPERTO, mansione libera) | 2 | R2 |
| 4 | US-702 | Rifiuto della proposta con motivo | 3 | R2 |
| 5 | US-704 | Promemoria al 4° giorno lavorativo | 3 | R2 |
| 6 | US-705 | Scadenza del timer | 3 | R2 |
| 7 | US-603 | Esclusione di un'azienda | 2 | R2 |
| | | **Totale** | **20** | |

Ordine: prima le notifiche, che servono a tutte le altre; poi le azioni del Tutor (appuntamento, rifiuto) e infine il
job del timer (promemoria e scadenza: una novità tecnica, un'attività pianificata nel backend, idempotente).
Dipendono dalle decisioni §8.2 e §8.3. Per il PO: US-1301 e le note operative del 25/10 (A5).

*Alternativa:* iniziare la bacheca (US-901, US-902, US-803: 13 punti) e rinviare promemoria e scadenza; sconsigliata,
perché una proposta accettata oggi resterebbe senza scadenza effettiva.

## 10. Azioni a carico del PO

- [ ] Provare la demo guidata (§3) sulla versione `d08e6dc`.
- [ ] US-1301: installazione e funzionamento offline della PWA su Android e iOS.
- [ ] Decidere i punti §8.1–§8.4 e confermare il planning (§9).
- [ ] Completare le note operative del 25/10 (A5).
