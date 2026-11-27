# Sprint 19 — Review, retrospettiva e planning

**Sprint:** 27 – 29 novembre 2026, concluso il 27/11 · **Review:** 27/11/2026 · **Partecipanti:** Mauro Bernardi
(Product Owner), team di sviluppo (Claude Code) · **Produzione:** https://portalenexus.it, versione `16789ed`, ancora
pre-produzione · **Collaudo:** [docs/test/collaudo.md](../../test/collaudo.md): tutte le prove dello sprint riuscite
(T-40 e T-41, 8 prove).

> Documento unico e asincrono: review (§1–§6), retrospettiva (§7), decisioni da prendere (§8) e proposta di planning
> dello Sprint 20 (§9). In fondo, l'appendice con la ricerca A42. La PR resta in **bozza** finché non abbiamo finito di
> discuterne (A12). Le storie sono già chiuse e le change archiviate nelle PR di chiusura (#220, #223).

---

## 1. Sintesi

| | |
|---|---|
| **Obiettivo dello sprint** | Il Tutor chiede al Call Center di contattare un'azienda e segue la richiesta fino all'esito. |
| **Esito** | ✅ **Raggiunto.** Le due storie sono in produzione dal 27/11, lo stesso giorno del planning. Il collaudo sul tuo PC ha cambiato due cose prima del merge: la richiesta si fa solo per le aziende non in anagrafica, e le richieste da prendere in carico stanno in cima. |
| **Impegno / completato** | 13 punti al planning; **10** dopo che hai rimesso US-1311 nel backlog (27/11); completati **10** (2 storie su 2) |
| **Extra** | Menu a tendina sotto i 1280 pixel, che sistema anche uno sconfinamento che c'era già; ricerca A42, nell'appendice |
| **Velocity** | Sprint 1: 21 · 2: 25 · 3: 21 · 4: 24 · 5: 21 · 6: 20 · 7: 31 · 8: 23 · 9: 25 · 10: 22 · 11: 15 · 12: 21 · 13: 22 · 14: 29 · 15: 27 · 16: 18 · 17: 29 · 18: 23 · 19: **10** · media 22,5 |
| **PR unite** | 6: #218 (review dello Sprint 18), #219, #220 (US-507), #221, #223 (US-512), #222 (US-1311 nel backlog). Aperte: nessuna |
| **Migrazioni del database** | 2: **V56** (richieste di contatto, estensione `pg_trgm` per i nomi simili), **V57** (tentativi e chiusura). La prossima è **V58** |
| **Deploy** | 2, il 27/11: `6aa35c3` (16:42, US-507), `16789ed` (17:39, US-512) |
| **CI** | Verde in tutte le PR unite |

## 2. Cosa è stato consegnato

| ID | Storia | Punti | PR | In una frase |
|---|---|---|---|---|
| US-507 | Richiesta di contatto di un'azienda dal Tutor | 5 | [#219](https://github.com/maubernardi/nexus/pull/219), [#220](https://github.com/maubernardi/nexus/pull/220) | Il Tutor chiede al Call Center di contattare un'azienda che non è in anagrafica, dopo aver visto le aziende con un nome simile. Un operatore qualsiasi prende in carico la richiesta, e nel menu c'è il numero di quelle da prendere. |
| US-512 | Tentativi di contatto ed esito della richiesta | 5 | [#221](https://github.com/maubernardi/nexus/pull/221), [#223](https://github.com/maubernardi/nexus/pull/223) | Chi ha la richiesta in carico segna i tentativi e la chiude con l'esito e una nota, collegando se serve l'azienda dell'anagrafica. Il Tutor riceve l'avviso e vede tutto nella pagina della richiesta. |

## 3. Cosa vedi in produzione

Le due storie le hai collaudate sul PC con i dati finti. In produzione, per ora pre-produzione, non c'è stato un
controllo manuale: le richieste di contatto partono vuote.

- **T-40** richiesta di contatto, sul PC. ✅
- **T-41** tentativi ed esito, sul PC. ✅

Le prove sono 190, e ne sono spuntate 86.

## 4. Qualità

### Test automatici

| | Fine Sprint 18 | Fine Sprint 19 |
|---|---|---|
| Backend – unit | 254 | 258 |
| Backend – integrazione (PostgreSQL e Keycloak veri) | 596 | 620 |
| Frontend – componenti (tutti con controllo axe) | 370 | 390 |
| Prove da capo a fondo nella CI, a ogni PR | backup e ripristino, aggiornamento di PostgreSQL, ambiente di collaudo, profilo utente di Keycloak | uguali |

Da segnalare:
- **L'estensione `pg_trgm` supera backup e ripristino.** È la prima estensione di PostgreSQL in NEXUS. La prova di backup
  e ripristino della CI la ricrea, perché il backup copia l'intero database.
- **La pulizia della demo si è fermata da sola**, come previsto. Una tabella nuova che punta alle aziende fa fermare
  lo script invece di farla svuotare di nascosto. Ora lo script svuota anche richieste e tentativi, e servirà così per
  l'azzeramento (US-1313).
- **Ogni vincolo nuovo è nel database e ha il suo test**:
  - il nome dell'azienda non è vuoto;
  - lo stato è valido;
  - una richiesta in carico ha l'operatore;
  - una richiesta chiusa ha esito e nota;
  - l'esito del tentativo è uno dei tre;
  - l'avviso punta a una richiesta che esiste.

### Sicurezza e privacy

- **Il Tutor vede solo le sue richieste**: quelle degli altri risultano inesistenti, e un test lo controlla.
- **Le note sono per il Tutor**: l'aiuto chiede di non scriverci dati personali. Le note dei tentativi e della chiusura
  non entrano nell'audit.
- **Nomi simili**: il Tutor vede solo i dati essenziali delle aziende, come nella sua lista.
- **Dependabot**: nessun avviso aperto.

### Difetti

| Difetto | Dove | Come è emerso | Correzione |
|---|---|---|---|
| Tra 1024 e 1279 pixel le voci del menu finivano sotto la campanella; per l'ADMIN e i Tutor coordinatori già prima di questo sprint | Menu | Misure in Chrome (A47), prima della PR | Menu a tendina sotto i 1280 pixel; per l'ADMIN le richieste nel sottomenu; per il Tutor la voce «Richieste» (#219) |
| La richiesta si poteva fare anche per un'azienda già in anagrafica | US-507 | Tu, prima del collaudo | Tolta: solo aziende non in anagrafica (#219) |
| Nell'elenco del Call Center le richieste da prendere in carico non stavano in cima | US-507 | Tuo collaudo T-40 | In cima, poi quelle in carico (#219) |
| La pulizia della demo si fermava con la tabella nuova | Pulizia della demo | Test `DemoCleanupIT` | Tabelle aggiunte allo script (#219, #221) |
| Il collaudo riavviato subito dopo un push era partito dal commit precedente | Collaudo | Mio controllo dello SHA | Dopo un push si avvia con lo SHA del commit |

## 5. Decisioni prese durante lo sprint

| # | Decisione | Presa da | Dove |
|---|---|---|---|
| D1 | Sprint 18 chiuso con velocity 23; avvisi con UptimeRobot e Healthchecks.io; US-1313 nel backlog; Sprint 19 con US-507, US-512, US-1311 | PO (27/11) | Review dello Sprint 18 |
| D2 | Nell'azzeramento ripartono da 1 anche i contatori delle segnalazioni e degli annunci | PO (27/11) | US-1313 |
| D3 | La richiesta di contatto si fa solo per un'azienda non ancora in anagrafica | PO (27/11) | US-507 |
| D4 | Nell'elenco del Call Center le richieste da prendere in carico stanno in cima | PO (27/11) | US-507 |
| D5 | US-1311 fuori dallo sprint e nel backlog: la riprendi quando lo dici tu | PO (27/11) | Sprint 19 |
| D6 | Nomi simili con la somiglianza dei trigrammi (`pg_trgm`): al massimo 5, anche con errori di battitura | Team (27/11) | US-507 |
| D7 | Menu a tendina sotto i 1280 pixel; per l'ADMIN le richieste nel sottomenu «Amministrazione» | Team (27/11) | US-507 |
| D8 | Tentativi e chiusura solo da chi ha la richiesta in carico; nota di chiusura obbligatoria; conferma prima di chiudere | Team (27/11) | US-512 |

## 6. Stato del prodotto rispetto alla roadmap

**R1** — sviluppo completo; resta **US-1301** (PWA su smartphone, a cura tua: prova T-10.1).

**R2, R3** — ✅ complete.

**R4** — *produzione*: **237 punti su 253**. Restano:

| Storia | Punti | Quando |
|---|---|---|
| US-1311 Avvisi affidabili su sito e backup | 3 | Quando lo dici tu (D5) |
| US-1313 Azzeramento della pre-produzione | 5 | Quando fissi la data (§8.3) |
| US-1305 Revisione UX/UI | 8 (da rivedere) | Dopo le prime settimane d'uso reale |

**Senza release**: completo. US-511, US-507 e US-512, 15 punti, sono tutte fatte.

**Product backlog:** 110 elementi, 455 punti; completati 106 (438 punti). Restano 4 storie, 17 punti. Nessuna è
pronta per lo Sprint 20 senza una tua decisione (§8.2).

## 7. Retrospettiva

**Cosa ha funzionato**
- **Due storie collegate in un giorno**, con il collaudo prima del merge, due deploy e la CI sempre verde.
- **Il collaudo ha preso le cose giuste in tempo**: la richiesta per le aziende già presenti e l'ordine dell'elenco
  sono stati corretti prima del merge.
- **Le azioni della retrospettiva scorsa hanno servito**:
  - con A46 le PR di chiusura e le storie nuove non sono mai andate in conflitto;
  - con A47 ho trovato lo sconfinamento del menu misurando, prima che lo vedessi tu.

**Cosa non ha funzionato**
- **Ho costruito un percorso che non volevi**: la richiesta per un'azienda già in anagrafica. Era nei criteri scritti
  dopo l'analisi del 23/11, ma quattro giorni dopo non corrispondeva più a quello che avevi in mente. L'hai notato
  prima del collaudo, e la correzione è costata un giro in più.
- **Lo sconfinamento del menu c'era da sprint**: per l'ADMIN tra 1024 e 1279 pixel, e nessuno l'aveva visto, io
  compreso.
- **A42 era rimasta indietro per due sprint**: l'ho fatta solo per questa review.

**Azioni**

| # | Azione | Chi |
|---|---|---|
| A49 | Quando una storia viene da un'analisi di qualche giorno prima, all'inizio ti riassumo in tre righe i percorsi principali, e parto subito. Se qualcosa non torna, lo correggiamo prima del codice | Team |
| A50 | Le azioni senza punti (come A42) le metto nel burndown, così non scivolano | Team |
| A45 … A48 | (confermate) PR su `main`; controllo dei conflitti; testi lunghi e colpo d'occhio; dipendenze al planning | Team |

## 8. Da decidere

### 8.1 Chiusura dello Sprint 19
*Proposta:* Sprint 19 chiuso con velocity **10**: le due storie sono fatte, collaudate e in produzione. La velocity è
bassa perché hai tolto US-1311 e lo sprint è finito in un giorno, non per un rallentamento.

> ✅ **Deciso (PO, 27/11):** Sprint 19 chiuso con velocity 10.

### 8.2 Che cosa fare nello Sprint 20
Il backlog pronto è finito. Delle 4 storie che restano, nessuna parte senza una tua decisione:
- US-1311 la riprendi tu (D5);
- US-1313 aspetta la data dell'azzeramento;
- US-1305 viene dopo l'uso reale;
- US-1301 è una prova a cura tua.

| | Cosa | A favore | Contro |
|---|---|---|---|
| **A** | **Le correzioni della ricerca A42** (appendice): tre piccole storie per correggere i dati che oggi restano sbagliati, circa 8 punti | Dati sbagliati correggibili prima che arrivino gli utenti; il lavoro è chiaro | Nessuna funzione nuova |
| **B** | **Azzeramento della pre-produzione** (US-1313), se fissi la data, più A | Si arriva pronti agli utenti | Serve la data e la risposta sull'ADMIN (§8.3) |
| **C** | **Una sessione di raccolta**: mi racconti che cosa manca dal punto di vista di Tutor e Call Center, e ne escono storie nuove | Il backlog riparte da quello che serve davvero | Lo sprint parte dopo la raccolta |

*Proposta:* **A**, con **C** in parallelo, quando hai tempo, per riempire il backlog degli sprint successivi. Se fissi
la data dell'azzeramento, si aggiunge B.

> ✅ **Deciso (PO, 27/11):** **A**. Il PO ha molte cose nuove da aggiungere: la raccolta (**C**) si fa, e le storie
> nuove entrano nel backlog per gli sprint successivi. Lo Sprint 20 è quello del §9 ([sprint-20](sprint-20.md)).

### 8.3 Azzeramento della pre-produzione (US-1313)
Per renderla pronta mi servono due risposte:
- **la data**, anche indicativa;
- **l'ADMIN di oggi**: lo teniamo con il suo accesso, oppure lo ricreiamo con un invito nuovo? *Proposta:* tenerlo,
  così non devi rifare la password. L'audit della pre-produzione si azzera comunque.

> ✅ **Deciso (PO, 27/11):** siamo ancora nella fase di brainstorming: tutto resta com'è. US-1313 resta nel backlog,
> senza data, e la domanda sull'ADMIN resta aperta.

## 9. Proposta per lo Sprint 20

**Date:** tre giorni dalla conferma del planning, indicativamente 30/11 – 2/12. **Obiettivo proposto:** *i dati inseriti
per sbaglio si correggono dall'app, senza chiedere a me.*

| Ordine | ID | Storia | Punti |
|---|---|---|---|
| 1 | US-309 (nuova) | Correzione della mansione richiesta e del progetto di una segnalazione (appendice, 1) | 3 |
| 2 | US-804 (nuova) | Correzione dell'appuntamento registrato (appendice, 2) | 2 |
| 3 | US-606 (nuova) | Togliere l'esclusione di un'azienda per una segnalazione (appendice, 3) | 3 |
| | | **Totale** | **8** |

I numeri delle storie sono provvisori: li fisso quando le scrivo nel backlog, dopo il tuo ok. La correzione della
richiesta di contatto (appendice, 4) la propongo dopo, se serve.

## 10. Azioni a carico del PO

- [x] Decidere i punti §8.1 … §8.3 (27/11).
- [ ] Raccontarmi le cose nuove da aggiungere (§8.2, C).
- [ ] Unire questa PR dopo le decisioni.
- [ ] Gli account degli utenti, dopo l'azzeramento (§8.3).
- [ ] Il blocco automatico dello schermo dei PC del Call Center, dopo pochi minuti, con chi gestisce i PC (US-1310).
- [ ] I 30 giorni dei backup nel registro dei trattamenti, con il DPO (dallo Sprint 16).
- [ ] US-1301: la prova T-10.1 della PWA sullo smartphone.

---

## Appendice — A42: dati che si inseriscono ma non si correggono dall'app

Ho guardato tutte le operazioni dell'API che scrivono dati, e per ognuna se esiste un modo di correggerla. Restano
**quattro gruppi di dati** che, se inseriti per sbaglio, oggi non si correggono né da chi li ha scritti né da altri:

| # | Dato | Chi lo inserisce | Oggi | Proposta |
|---|---|---|---|---|
| 1 | **Progetto e mansione richiesta di una segnalazione** (tipologia del catalogo o testo libero) | Tutor, all'invio | Non cambiano più. Si correggono la sede di competenza (ADMIN o coordinatore) e il tipo di tirocinio | Il Tutor li corregge finché la segnalazione non è abbinata a una mansione; poi il Call Center. Con l'audit prima/dopo |
| 2 | **Nota dell'appuntamento fissato** (per esempio data e ora) | Tutor | Non cambia | Il Tutor la corregge finché il tirocinio non parte |
| 3 | **Esclusione di un'azienda per una segnalazione** | Call Center, Tutor (rifiuto) | Non si toglie: l'azienda non torna tra le compatibili | Il Call Center la toglie, con il motivo; resta nello storico |
| 4 | **Nome, zona e categoria di una richiesta di contatto** | Tutor | Non cambiano | Il Tutor li corregge finché la richiesta è aperta |

Con un **aggiramento** che basta:
- **dati di un modello dei documenti**: documento, progetto, tipo, ente e nome. L'ADMIN lo disattiva e lo carica di
  nuovo.

**Immodificabili per scelta**, e restano così:
- il report di restituzione;
- le note del diario delle aziende;
- i tentativi di contatto;
- i motivi di rifiuto e di appuntamento fallito;
- lo storico e l'audit.

Ogni correzione nuova entra nell'audit con prima e dopo (EN-2).
