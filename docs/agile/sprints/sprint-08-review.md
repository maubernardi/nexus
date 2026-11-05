# Sprint 8 — Review, retrospettiva e planning

**Sprint:** 30 ottobre – 1 novembre 2026 (iniziato il 29/10 dopo il planning, chiuso il 04/11 per la pausa del cambio di
PC) · **Review:** 05/11/2026 · **Partecipanti:** Mauro Bernardi (Product Owner), team di sviluppo (Claude Code) ·
**Demo:** https://portalenexus.it, versione `0fbb22a` (tutto lo Sprint 8 e il cambio dell'indirizzo email) ·
**Collaudo:** [docs/test/collaudo.md](../../test/collaudo.md), T-13.4 e sezioni 16–19.

> Documento unico e asincrono: review (§1–§6), retrospettiva (§7), decisioni da prendere (§8) e proposta di planning
> dello Sprint 9 (§9). La PR resta in **bozza** finché non abbiamo finito di discuterne (A12): le decisioni le aggiungo
> qui, poi la segno come pronta per il merge.

---

## 1. Sintesi

| | |
|---|---|
| **Obiettivo dello sprint** | Il percorso arriva in fondo: il Tutor avvia il tirocinio, scarica Convenzione e Progetto Formativo compilati e chiude con il report di restituzione; la mansione torna disponibile se l'azienda lo dice. Le notifiche importanti arrivano anche per email. |
| **Esito** | ✅ **Raggiunto.** Tutte le storie Must di R3 sono fatte e pubblicate; le email delle notifiche arrivano davvero (T-18.1, 05/11). |
| **Impegno / completato** | 23 punti / **23 punti** (7 storie su 7) |
| **Extra** | Nessuno: US-205 e US-102 non iniziate, restano nel backlog |
| **Fuori piano** | US-110 *Cambio del proprio indirizzo email con codice* (8), chiesta da te il 05/11 durante la prova delle email (§8.3) |
| **Velocity** | Sprint 1: 21 · 2: 25 · 3: 21 · 4: 24 · 5: 21 · 6: 20 · 7: 31 · 8: **23** · media 23,3 |
| **PR unite** | Sprint: 9 (#87–#95): 7 di funzionalità, 1 sui test (A13), 1 di documentazione. Fuori piano: 3 (#96–#98): funzionalità, correzione, collaudo |
| **Migrazioni del database** | 2: V25 (cataloghi del report), V26 (report di restituzione). Fuori piano: V27 (richieste di cambio email) |
| **Deploy della demo** | 05/11, 00:08: `f83e95a` (Sprint 8 completo) · 05/11, 18:03: `acd5ff6` **fallito, demo giù** (§4) · 05/11, 19:22: `0fbb22a` (correzione) |

## 2. Cosa è stato consegnato

| ID | Storia | Punti | PR | In una frase |
|---|---|---|---|---|
| US-1003 | Avvio del tirocinio | 1 | [#88](https://github.com/maubernardi/nexus/pull/88) | Con la scheda completa il Tutor avvia il tirocinio; se manca qualcosa la pagina dice cosa e porta al campo. |
| US-1004 | Convenzione in Word | 5 | [#89](https://github.com/maubernardi/nexus/pull/89) | Il Tutor scarica la Convenzione compilata dalla scheda, con il modello segnaposto del tipo di tirocinio. |
| US-1005 | Progetto Formativo in Word | 3 | [#90](https://github.com/maubernardi/nexus/pull/90) | Come la Convenzione, con la griglia oraria e il monte ore. |
| US-109 | Cataloghi del report di restituzione | 2 | [#91](https://github.com/maubernardi/nexus/pull/91) | Formule di assunzione e motivi della mancata assunzione nella pagina "Cataloghi". |
| US-1101 | Report di restituzione | 5 | [#92](https://github.com/maubernardi/nexus/pull/92) | Gradimenti, esito e mansione ancora disponibile: con l'invio il percorso si chiude e il report non si modifica più. |
| US-1102 | Esito sulla mansione e audit trail | 2 | [#93](https://github.com/maubernardi/nexus/pull/93) | La mansione torna libera se l'azienda lo dice; lo storico dell'azienda registra l'esito del tirocinio. |
| US-1202 | Email transazionali | 5 | [#94](https://github.com/maubernardi/nexus/pull/94) | Ogni notifica in app arriva anche via email, da `noreply@portalenexus.it`, senza dati personali. |
| US-110 | Cambio del proprio indirizzo email (fuori piano) | 8 | [#96](https://github.com/maubernardi/nexus/pull/96), [#97](https://github.com/maubernardi/nexus/pull/97) | «Il mio profilo»: nuovo indirizzo confermato con un codice di 6 cifre, aggiornato in NEXUS e in Keycloak; avviso al vecchio indirizzo. |

### Il percorso dopo lo Sprint 8

```
Segnalazione ──► Coda ──► Abbinamento ──► Proposta ──► Accettata ──► Appuntamento ──► Scheda del tirocinio
                                                                                          │
                                                       Avvio (scheda completa) ◄──────────┘
                                                              │
                                     Convenzione e Progetto Formativo in Word
                                                              │
                                     Report di restituzione ──► Percorso chiuso
                                     (esito, mansione di nuovo libera o no, storico dell'azienda)

Ogni passaggio notificato in app e via email alla controparte.
```

## 3. Demo guidata

Le prove passo per passo sono nel **collaudo**, sulla versione `0fbb22a`:
- **T-13.4:** voci del report di restituzione (ADMIN);
- **sezione 16:** avvio del tirocinio, Convenzione e Progetto Formativo;
- **sezione 17:** report di restituzione, mansione e storico dell'azienda;
- **sezione 18:** email delle notifiche (✅ T-18.1);
- **sezione 19:** il mio profilo e cambio dell'indirizzo email (✅ T-19.1, T-19.3, T-19.4).

Per le sezioni 16–17 serve una segnalazione con la **scheda del tirocinio** compilata (sezione 14).

## 4. Qualità

### Test automatici

| | Fine Sprint 7 | Fine Sprint 8 | Con US-110 |
|---|---|---|---|
| Backend – unit | 69 | 100 | 106 |
| Backend – integrazione (PostgreSQL reale) | 280 | 305 | 326 |
| Frontend – componenti (tutti con controllo axe) | 156 | 170 | 179 |

Da segnalare:
- **I documenti Word si verificano aprendo il file generato** e controllando che nessun segnaposto resti scoperto.
- **Il report non si modifica nemmeno dal database**: un trigger rifiuta modifiche e cancellazioni, con un test.
- **US-110 si prova contro un Keycloak vero** (stessa versione e impostazioni di hostname della produzione), e da
  #97 un test avvia l'applicazione con il **profilo di produzione** (§7, A17).

### Accessibilità (WCAG 2.2 AA)

Nuove pagine e sezioni (avvio, documenti, report di restituzione, il mio profilo) con il controllo axe nei test:
**0 violazioni**. Il campo del codice usa `autocomplete="one-time-code"`: sugli smartphone il codice si può inserire
dal suggerimento della tastiera.

### Difetti

| Difetto | Dove | Come è emerso | Correzione |
|---|---|---|---|
| Testo delle email: solo l'ultima parte veniva compilata con il numero della segnalazione | US-1202 | Test, prima della PR | Prima della PR (#94) |
| **Il backend non partiva in produzione**: un componente usato solo con Keycloak chiedeva a Spring un oggetto che in Spring Boot 4 non esiste. I test girano con gli utenti di prova e non lo vedevano | US-110 | Deploy del 05/11 | [#97](https://github.com/maubernardi/nexus/pull/97) e un test di avvio con il profilo di produzione. **Demo irraggiungibile ~1 ora e 20 (18:03–19:22)** |
| **Il ripristino automatico della versione precedente non è ripartito** durante il deploy fallito | Deploy | Deploy del 05/11 | **Aperto**: causa non nota, non ho accesso ai log del server. Probabile la migrazione V27 già applicata. A16 |

## 5. Decisioni prese durante lo sprint

| # | Decisione | Presa da | Dove |
|---|---|---|---|
| D1 | Scheda completa per l'avvio: tutto tranne sospensioni e note | PO (§8.2 review Sprint 7) | US-1003 |
| D2 | Modelli Word segnaposto, uno per tipo di tirocinio; la modifica dei modelli più avanti | PO (§8.3 review Sprint 7) | US-1004, US-1005 |
| D3 | Report: invio in qualunque momento del tirocinio, obiettivi e difficoltà facoltativi, nessuna bozza (un invio con conferma) | Team, **da confermare** (§8.1) | US-1101 |
| D4 | Mansione non più disponibile: resta bloccata dalla segnalazione conclusa, senza ritiro automatico; decide il Call Center | Team, **da confermare** (§8.1) | US-1102 |
| D5 | Email: stesso testo delle notifiche in app, solo il numero della segnalazione; password SMTP solo nel `.env` del server | PO e team | US-1202 |
| D6 | Cambio email: in NEXUS **e** in Keycloak, avviso al vecchio indirizzo, codice di 6 cifre valido **30 minuti**, 5 tentativi, nuovo codice dopo 60 secondi | PO (05/11) | US-110 |
| D7 | Accesso tecnico `nexus-backend-admin` del backend verso Keycloak, con i soli permessi sugli utenti; creato sul server con uno script una tantum | Team, eseguito dal PO | US-110 |

## 6. Stato del prodotto rispetto alla roadmap

**R1** — sviluppo completo; resta **US-1301** (PWA su smartphone, a cura del PO: prova T-10.1).

**R2** — ✅ completa.

**R3** — *tirocinio, documenti Word, chiusura, email*: **tutte le storie Must fatte e pubblicate** (40 punti su 45).
Resta **US-205** *Curriculum del beneficiario* (5, Could) (§8.4).

**R4** — *produzione*: fatte US-1304 (tema di accesso) e, fuori piano, US-110: **11 punti su 58**. Restano:

| Gruppo | Storie | Punti |
|---|---|---|
| Utenti e progetti | US-102 progetti (3), US-103 creazione utente (8), US-104 utenti e progetti (3), US-105 disattivazione e ruolo (3) | 17 |
| Accesso | US-1308 recupero password (2) | 2 |
| Dati | US-1302 backup (5), US-1303 anonimizzazione a scadenza (5) | 10 |
| Produzione | US-1305 revisione UX/UI (8), US-1306 passaggio in produzione (5), US-1307 monitoraggio e sicurezza (5) | 18 |

**Product backlog:** 70 elementi attivi, 250 punti; completati 58 elementi (197 punti) negli Sprint 1–8, US-110
compresa.

## 7. Retrospettiva

**Cosa ha funzionato**
- **Impegno completato per intero**, email comprese, nonostante la pausa per il cambio di PC: il backup dei file di
  Claude e le note di ripartenza hanno fatto riprendere il lavoro senza perdite.
- **A13 fatta per prima**: i test lenti del modulo beneficiario non hanno più dato problemi.
- **A14 applicata**: per il report di restituzione ho scelto le soluzioni che non limitano il Tutor e le ho segnate da
  confermare, invece di aggiungere blocchi.
- **Le email provate fino in fondo**: notifica, codice, avviso al vecchio indirizzo e accesso con la nuova email, in
  pochi minuti dopo il deploy.
- **Decisioni rapide su US-110**: tre domande, risposte subito, nessun rifacimento.

**Cosa non ha funzionato**
- **Ho messo giù la demo per circa un'ora e 20.** Il backend di US-110 non partiva con il profilo di produzione, che
  nessun test caricava: la CI era verde ma non provava quello che va sul server.
- **Il ripristino automatico non ha funzionato**, e senza i log non so perché. Il paracadute del deploy va verificato,
  non dato per scontato.
- **Non posso leggere lo stato del server**: `sudo` chiede la password e l'accesso a Docker non è previsto. In caso di
  problemi dipendo da te anche solo per leggere un log.
- **Il collaudo ha poche caselle spuntate** (4 su circa 90): non so se le prove degli sprint precedenti le hai fatte
  senza segnarle o se sono ancora da fare (§8.6).
- **US-110 è partita a sprint chiuso, senza planning.** Era urgente per la prova delle email e l'hai chiesta tu, ma
  va contata in modo chiaro (§8.3).

**Azioni**

| # | Azione | Chi |
|---|---|---|
| A16 | Capire perché il ripristino automatico non è ripartito e correggerlo; poi provarlo di proposito con una release che non parte. Serve un modo per leggere i log del server (§8.5) | Team, con il PO |
| A17 | Ogni componente usato solo in produzione è coperto dal test di avvio con il profilo `keycloak` (fatto in #97) | Team |
| A15 | (confermata) Passi manuali sul server come lista completa; lo script di US-110 ha funzionato al primo colpo | Team |
| A12 | (confermata) La PR della review si apre in bozza; questa lo è | Team |

## 8. Da decidere

### 8.1 Scelte del team su US-1101 e US-1102
Le avevo segnate da confermare:
- **report di restituzione:** si invia in qualunque momento del tirocinio, obiettivi e difficoltà sono facoltativi, non
  c'è una bozza (un solo invio, con conferma);
- **mansione non più disponibile:** resta legata alla segnalazione conclusa, senza ritiro automatico; il Call Center
  decide se ritirarla o modificarla.

*Proposta:* confermarle così come sono.

### 8.2 Chiusura dello Sprint 8
*Proposta:* Sprint 8 chiuso con velocity **23**.

### 8.3 Come contare US-110
US-110 (8 punti) è stata chiesta e fatta il 05/11, dopo la chiusura dello Sprint 8 e prima del planning dello Sprint 9.
*Proposta:* come US-1304 nello Sprint 7, **Done fuori piano, senza contarla nella velocity** di nessuno sprint: così
la velocity resta una base onesta per il planning.

### 8.4 R3 chiusa e US-205
Tutte le Must di R3 sono pubblicate sulla demo. *Proposta:*
- **R3 completa** nella roadmap;
- **US-205** *Curriculum del beneficiario* (Could) resta nel backlog come extra, senza legarla a una release.

### 8.5 Leggere i log del server (A16)
Per indagare il ripristino, e in generale per non dipendere da te per un log, propongo un **comando di sola lettura**
nello script di deploy: la chiave di deploy, oggi capace solo di `deploy`, potrebbe anche chiedere `logs <servizio>` e
`status`, senza poter cambiare nulla. La GitHub Action li espone in un workflow lanciato a mano.

*Alternativa:* nessun accesso in più; in caso di problemi mi incolli tu i log.

### 8.6 Il collaudo
Ci sono 4 caselle spuntate su circa 90. *Proposta:* nella review di ogni sprint mi dici quali sezioni hai provato e io
le spunto (come oggi). Per le prove degli sprint passati che hai già fatto senza segnarle, basta un elenco delle
sezioni.

### 8.7 Istruzioni per la password dei nuovi utenti (US-103 e US-1308)
Quando l'ADMIN crea un utente (US-103), l'utente deve ricevere un'email per impostare la password. Il recupero della
password dimenticata (US-1308) è lo stesso meccanismo.
*Proposta:* lo fa **Keycloak**, con le sue email "imposta la password" e "password dimenticata", in italiano e con
il tema di NEXUS. Serve configurare in Keycloak lo stesso SMTP di IONOS: la password la inserisci tu nella console
(o con uno script, mai nel repository). Così US-1308 diventa poco più di una configurazione.

### 8.8 Username dei nuovi utenti (US-103)
*Proposta:* lo scrive l'ADMIN, con il suggerimento `nome.cognome`; l'utente può accedere anche con l'email.

## 9. Proposta per lo Sprint 9 (3 giorni, ~20 punti)

**Obiettivo proposto:** *l'ADMIN gestisce progetti e utenti da NEXUS, senza la console di Keycloak: crea gli utenti,
li assegna ai progetti, li disattiva o cambia il ruolo; chi dimentica la password la reimposta da solo.*

| Ordine | ID | Storia | Punti | Release |
|---|---|---|---|---|
| 1 | A16 | Ripristino del deploy (azione, senza punti) | — | — |
| 2 | US-102 | Gestione progetti | 3 | R4 |
| 3 | US-103 | Creazione utente (§8.7, §8.8) | 8 | R4 |
| 4 | US-104 | Assegnazione utenti ai progetti | 3 | R4 |
| 5 | US-105 | Disattivazione e cambio ruolo | 3 | R4 |
| 6 | US-1308 | Recupero password (§8.7) | 2 | R4 |
| | | **Totale** | **19** | |

**Extra**, nell'ordine: US-1302 *Backup del database* (5), US-205 *Curriculum del beneficiario* (5).

US-103 e US-105 riusano l'accesso tecnico a Keycloak di US-110, che ha già i permessi necessari. Dipende dalle
decisioni §8.5–§8.8. Nella PR che apre lo sprint archivio anche la change di US-110 (`add-email-change`). Per il PO:
collaudo (T-13.4, sezioni 16–17, T-19.2), US-1301, e l'SMTP in Keycloak se accetti §8.7.

## 10. Azioni a carico del PO

- [ ] Decidere i punti §8.1–§8.8 e confermare il planning (§9).
- [ ] Collaudo sulla versione `0fbb22a`: T-13.4, sezioni 16–17, T-19.2.
- [ ] US-1301: PWA su Android e iOS (collaudo T-10.1).
- [x] Password SMTP nel `.env` del server e prova di un'email reale (T-18.1, 05/11).
- [x] Script del client Keycloak sul server (US-110, 05/11).
