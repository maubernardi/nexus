# Sprint 16 — Review, retrospettiva e planning

**Sprint:** 21 – 23 novembre 2026 · **Review:** 23/11/2026 · **Partecipanti:** Mauro Bernardi (Product Owner), team di
sviluppo (Claude Code) · **Demo:** https://portalenexus.it, versione `e8223da` · **Collaudo:**
[docs/test/collaudo.md](../../test/collaudo.md), T-22 … T-29 da fare.

> Documento unico e asincrono: review (§1–§6), retrospettiva (§7), decisioni da prendere (§8) e proposta di planning
> dello Sprint 17 (§9). La PR resta in **bozza** finché non abbiamo finito di discuterne (A12). La chiusura dello sprint
> (change archiviate, backlog, collaudo) è nella [PR #186](https://github.com/maubernardi/nexus/pull/186).

---

## 1. Sintesi

| | |
|---|---|
| **Obiettivo dello sprint** | NEXUS va in produzione sul server di oggi, con un backup giornaliero cifrato fuori dal server e provato con un ripristino. La produzione tiene la configurazione della demo e prende le aziende dal database attuale; parte senza segnalazioni, beneficiari e utenti, con la sola utenza dell'ADMIN. |
| **Esito** | 🟡 **In parte.** Il backup è in funzione dal 21/11, provato con l'archivio vero. Il passaggio in produzione è pronto e provato sul backup vero della demo, ma non è avvenuto: mancano la tua preparazione e la data (§8.2). |
| **Impegno / completato** | 13 punti al planning, **26** dopo le due storie che hai aggiunto il 22/11; completati **18** (3 storie su 4) |
| **Extra** | Diagnosi dell'accesso alla console di Keycloak (guasto di WSL, non del server); verifica dei testi delle email di Keycloak dalla console |
| **Velocity** | Sprint 1: 21 · 2: 25 · 3: 21 · 4: 24 · 5: 21 · 6: 20 · 7: 31 · 8: 23 · 9: 25 · 10: 22 · 11: 15 · 12: 21 · 13: 22 · 14: 29 · 15: 27 · 16: **18** · media 22,8 |
| **PR unite** | 8: #174 (backup), #176 (blocco *Governance*), #177 (preparazione del passaggio), #178 (controllo al minuto 7), #179 (avvisi di fine tirocinio), #180 (testi dei messaggi), #185 (volumi del ripristino), #175 (Dependabot). Prima dello sprint #173 (planning). Aperte: #181 (da unire) e #182–#184 (PostgreSQL 19, da non unire: §8.3) |
| **Migrazioni del database** | 2, solo additive: V50 (fasi degli avvisi di fine tirocinio), V51 (testi dei messaggi) |
| **Deploy della demo** | 4: `765dec9` (21/11, poi rifatto per leggere il `.env` del backup), `2432f49` (22/11), `e8223da` (23/11) |
| **CI** | Verde in tutte le PR dello sprint |

## 2. Cosa è stato consegnato

| ID | Storia | Punti | PR | In una frase |
|---|---|---|---|---|
| US-1302 | Backup del database | 5 | [#174](https://github.com/maubernardi/nexus/pull/174), [#176](https://github.com/maubernardi/nexus/pull/176), [#178](https://github.com/maubernardi/nexus/pull/178), [#185](https://github.com/maubernardi/nexus/pull/185) | Ogni notte alle 3:30 i database di NEXUS e Keycloak vanno su IONOS a Berlino, cifrati con la tua chiave, bloccati 30 giorni; alle 9:07 un controllo ti scrive se manca un backup; ripristino provato sul tuo PC. |
| US-1203 | Avvisi di fine tirocinio e blocco delle nuove segnalazioni | 5 | [#179](https://github.com/maubernardi/nexus/pull/179) | Il Tutor riceve notifica ed email 7 e 3 giorni prima della fine prevista e 3 e 7 giorni dopo; dal giorno dopo la fine non apre segnalazioni finché non chiude o proroga. |
| US-1204 | Testi delle notifiche e delle email modificabili dall'ADMIN | 8 | [#180](https://github.com/maubernardi/nexus/pull/180) | Pagina «Testi dei messaggi»: per ogni notifica testo nell'app, oggetto e testo dell'email, distinti, con anteprima e ripristino; oggetto con la parte distintiva del tipo. |
| US-1306 | Passaggio in produzione | 8 | [#177](https://github.com/maubernardi/nexus/pull/177) (preparazione) | **Non chiusa.** Pronti e provati sul backup vero: pulizia della demo, utenti di Keycloak, importazione, primo ADMIN, script del passaggio. Restano il passaggio e la seconda PR. |

## 3. Demo guidata

Sulla demo (`e8223da`), con le prove del collaudo:

- **T-26.1** backup subito e stato: fatto insieme il 21/11 (1 MB, blocco fino al 21/12). ✅ da spuntare
- **T-26.2** ripristino di prova sul tuo PC: riuscito il 21/11 e di nuovo il 23/11. ✅ da spuntare
- **T-26.3** controllo del mattino: il 22/11 GitHub l'ha fatto partire alle 17:06; ora è alle 9:07. ⏳
- **T-28** avvisi di fine tirocinio e blocco: con un tirocinio avviato e la fine prevista a ieri. ⏳
- **T-29** testi dei messaggi: modifica, controlli, ripristino. ⏳
- **T-27** prove della produzione: dopo il passaggio. ⏳

Da prima restano T-22 … T-25. I 6 modelli di GOL sono caricati: il ripristino ne conta 6.

## 4. Qualità

### Test automatici

| | Fine Sprint 15 | Fine Sprint 16 |
|---|---|---|
| Backend – unit | 190 | 246 |
| Backend – integrazione (PostgreSQL e Keycloak veri) | 528 | 551 |
| Frontend – componenti (tutti con controllo axe) | 292 | 299 |
| Prova del backup nella CI (backup, S3 di prova, ripristino) | — | a ogni PR |

Da segnalare:
- **Il backup si prova da capo a fondo a ogni PR**: database di prova, archivio cifrato, caricamento su un S3 di prova
  con il blocco, ripristino, confronto delle righe, archivio alterato e chiave sbagliata rifiutati. Al posto di MinIO,
  che non pubblica più le immagini, VersityGW.
- **La pulizia della demo è provata sullo schema vero** con i dati dimostrativi (`DemoCleanupIT`), poi sul backup vero
  della demo (23/11): tolte 39 voci dell'audit e 17 righe degli script dimostrativi, importate 13.799 aziende e 38.789
  chiamate, backend senza `seed` con l'ADMIN creato.
- **Gli avvisi di fine tirocinio** sono provati giorno per giorno, dagli 8 giorni prima ai 20 dopo, con le proroghe e i
  giorni saltati. Il controllo non tocca la versione della scheda.
- **I testi predefiniti** producono esattamente le email di prima: i test delle email sono passati senza cambiare i
  testi attesi, solo l'oggetto con la parte distintiva.

### Sicurezza e privacy

- **Backup**:
  - cifrati sul server con la tua chiave pubblica; la chiave privata è solo tua (password manager e copia offline);
  - le chiavi di IONOS stanno solo nel `.env`;
  - blocco *Governance* di 30 giorni: una richiesta normale non cancella un backup, nemmeno con le chiavi del server.
- **Il ripristino di prova lasciava sul disco il backup in chiaro**, in un volume di Docker. Trovato nella prova
  generale e corretto (#185); hai cancellato i volumi rimasti.
- **Nei log e nelle Actions** solo date, dimensioni e nomi dei file; lo script del passaggio stampa solo numeri e
  codici.
- **Il primo ADMIN** arriva con l'invito; il realm di produzione non ha utenti di prova, e la CI lo controlla.
- **Dependabot**: #181 da unire; #182–#184 (PostgreSQL 19) da non unire (§8.3).

### Difetti

| Difetto | Dove | Come è emerso | Correzione |
|---|---|---|---|
| Il ripristino di prova si fermava senza messaggio con Docker Desktop spento | US-1302 | Tuo ripristino del 21/11 | Controllo di Docker all'inizio (#176) |
| Il controllo del mattino è partito alle 17:06 invece che alle 9 | US-1302 | Primo giorno (22/11) | Cron al minuto 7, meno carico (#178) |
| Il ripristino di prova lasciava il backup in chiaro nel volume di Docker | US-1302 | Prova generale (23/11) | `docker rm -f -v` (#185) |
| Il controllo finale della pulizia scambiava la riga di Flyway della creazione dello schema per uno script dimostrativo | US-1306 | Test, prima della PR | Controllo sui soli script `R__` |
| Colonna `smallint` dove Hibernate si aspetta `integer` | US-1203 | Test, prima della PR | V50 con `integer`, prima della PR |

## 5. Decisioni prese durante lo sprint

| # | Decisione | Presa da | Dove |
|---|---|---|---|
| D1 | La produzione tiene tutto della demo tranne segnalazioni, utenti e beneficiari; le aziende vengono dal database attuale | PO (20/11) | US-1306 |
| D2 | La pulizia toglie anche l'audit degli oggetti tolti; resta quello della configurazione | PO (20/11) | US-1306 |
| D3 | Avviso del backup ogni mattina, via email di GitHub all'account `maubernardi` | PO (20/11, 21/11) | US-1302 |
| D4 | Blocco degli oggetti in modalità *Governance*, non *Compliance*: il PO può cancellare a mano | PO (21/11) | US-1302 |
| D5 | Contenitore a Berlino (`eu-central-3`), con le chiavi del PO | PO (21/11) | US-1302 |
| D6 | 13 punti invece di 10, per US-1306 portata a 8 | PO (20/11) | Planning |
| D7 | US-1203: avvisi solo al Tutor, giorni di calendario, blocco dal giorno dopo la fine | PO (22/11) | US-1203 |
| D8 | US-1204: testi distinti per app ed email, parte distintiva dell'oggetto modificabile, nello Sprint 16 | PO (22/11) | US-1204 |
| D9 | Backup su un'immagine con la stessa versione di PostgreSQL del server, alle 3:30, senza controllo di salute nel compose | Team | US-1302 |
| D10 | Pulizia in una transazione, con `TRUNCATE` senza `CASCADE` e le regole di sola aggiunta sospese solo dentro | Team | US-1306 |
| D11 | Il profilo predefinito resta `keycloak,seed` fino al passaggio; lo script mette `keycloak` nel `.env` | Team | US-1306 |
| D12 | Fasi degli avvisi sulla scheda, scritte senza toccarne la versione | Team | US-1203 |
| D13 | Testi predefiniti nel codice, nel database solo quelli cambiati; le notifiche nell'app prendono il testo dal backend | Team | US-1204 |
| D14 | Le email di Keycloak restano nel tema e si ridefiniscono dalla sua console (provato il 23/11) | Team | US-1204 |

## 6. Stato del prodotto rispetto alla roadmap

**R1** — sviluppo completo; resta **US-1301** (PWA su smartphone, a cura del PO: prova T-10.1).

**R2, R3** — ✅ complete.

**R4** — *produzione*: **185 punti su 201** fatti. Restano:

| Storia | Punti | Quando |
|---|---|---|
| US-1306 Passaggio in produzione | 8 | Sprint 17 (§9), nel giorno che scegli (§8.2) |
| US-1305 Revisione UX/UI | 8 (da rivedere) | Dopo la produzione (review dello Sprint 15, §8.7) |

Da stimare: **US-507** Richiesta di contatto di un'azienda dal Tutor. L'analisi prevista in questo sprint non si è
fatta (§8.5). Da aggiungere, se accetti: **EN-10** Aggiornamento a PostgreSQL 19 (§8.3).

**Product backlog:** 96 elementi, 393 punti (più US-507); completati 93 (376 punti), ne restano 3 (17 punti).

## 7. Retrospettiva

**Cosa ha funzionato**
- **Il backup è in funzione dal primo giorno** ed è provato davvero: ripristino con l'archivio vero, due volte, e una
  prova completa nella CI a ogni PR.
- **La prova generale sul backup vero** ha confermato pulizia, importazione, primo ADMIN e Keycloak con i dati veri.
  Ha anche trovato un difetto di privacy prima del passaggio.
- **Le due storie che hai aggiunto** sono passate dal backlog con le tue scelte prima del codice. Sono arrivate sulla
  demo il giorno stesso.
- **La CI e la lettura delle PR hanno fermato PostgreSQL 19**, che avrebbe aperto un database vuoto al posto di quello
  vero.

**Cosa non ha funzionato**
- **L'obiettivo dello sprint non è raggiunto.** Lo sprint è passato da 13 a 26 punti. Il passaggio dipende dalla tua
  preparazione e dal collaudo, che è indietro di sei sprint (T-22 … T-29).
- **Il ripristino di prova lasciava il backup in chiaro sul disco.** La prova automatica controllava i dati, non quello
  che restava dopo. L'abbiamo visto solo nella prova generale.
- **Il primo controllo del mattino è partito con 8 ore di ritardo**, perché era allo scoccare dell'ora.
- **Il guasto tra Windows e WSL** ha bloccato prima Docker, poi il tunnel per Keycloak. Prima di capire che il server
  non c'entrava abbiamo controllato server, SSH e Keycloak.
- **Sul blocco dei backup ti ho proposto *Compliance***, e hai scelto *Governance* solo quando IONOS te l'ha chiesto.
  Andavano presentate le modalità con le conseguenze già al planning.

**Azioni**

| # | Azione | Chi |
|---|---|---|
| A37 | I container di prova con dati si spengono sempre con `docker rm -f -v`; la prova del backup controlla anche che non restino volumi | Team |
| A38 | I workflow pianificati non partono mai allo scoccare dell'ora | Team |
| A39 | Se Docker non scarica le immagini, o compare «UtilAcceptVsock», per prima cosa `wsl --shutdown` | PO e Team |
| A40 | Le scelte di sicurezza (per esempio la modalità di un blocco) si presentano con tutte le opzioni e le conseguenze, prima di decidere | Team |
| A36 | (confermata) Per attendere la CI solo `--watch`; nessuna attività lasciata in background | Team |

## 8. Da decidere

### 8.1 Chiusura dello Sprint 16
*Proposta:* Sprint 16 chiuso con velocity **18**. US-1302, US-1203 e US-1204 fatte; US-1306 passa allo Sprint 17 con
la stessa stima (8). La chiusura è nella [PR #186](https://github.com/maubernardi/nexus/pull/186).

### 8.2 Data del passaggio in produzione
Lo script è pronto e provato. Servono:
- la tua preparazione: dati dell'ADMIN nel `.env`, file Access sul server, `nexus-deploy` reinstallato, controllo in
  Access delle 7 + 20 chiamate;
- un collaudo minimo.

*Proposta:* prima del passaggio T-24, T-25, T-28 e T-29, cioè le funzioni che userete dal primo giorno. Il resto può
seguire sulla produzione, con dati di prova da togliere a mano. Ti chiedo una data indicativa, anche solo il giorno
della settimana.

### 8.3 PostgreSQL 19
Dependabot propone PostgreSQL 19 in tre PR (#182–#184). Un salto di versione principale non legge i dati della 18:
l'immagine nuova troverebbe vuota la sua cartella e partirebbe con un database nuovo.

*Proposta:* una storia **EN-10 «Aggiornamento a PostgreSQL 19»**, 5 punti, dopo il passaggio in produzione, con questi
passi:
1. backup subito;
2. fermo del backend;
3. PostgreSQL 19 su un volume nuovo;
4. ripristino del backup;
5. riavvio.

Si prova prima in locale con lo script di ripristino sul backup vero; il volume vecchio resta come ritorno indietro.
Aggiorna anche le immagini di test e del backup, e chiude le tre PR.

*Alternativa:* restare sulla 18, supportata fino al 2030. Va però contro la regola delle versioni sempre aggiornate.

### 8.4 Contenuto dello Sprint 17
*Proposta:* finire la produzione, poi l'aggiornamento: US-1306 (il passaggio e la seconda PR) ed EN-10, 13 punti (§9).
Con meno punti della media resta tempo per quello che emerge dal collaudo e dal primo uso.

### 8.5 Analisi di US-507 (richiesta di contatto di un'azienda dal Tutor)
Era prevista in questo sprint (review dello Sprint 15, §8.6) e non si è fatta: lo sprint si è riempito con US-1203 e
US-1204. *Proposta:* la sessione nello Sprint 17, dopo il passaggio. La storia si stima dopo.

## 9. Proposta per lo Sprint 17

**Date:** 24 – 26 novembre 2026. **Obiettivo proposto:** *NEXUS è in produzione con i dati veri e il backup notturno;
dopo, PostgreSQL passa alla 19 con un ripristino provato.*

| Ordine | ID | Storia | Punti |
|---|---|---|---|
| 1 | US-1306 | Passaggio in produzione: lo script sul server, l'invito all'ADMIN, poi la seconda PR (profilo predefinito, realm e password demo, nomi «produzione», controllo ASVS) | 8 |
| 2 | EN-10 | Aggiornamento a PostgreSQL 19, con backup e ripristino provati prima in locale | 5 |
| | | **Totale** | **13** |

Prossima migrazione: **V52**. Nello sprint anche l'analisi di US-507 (§8.5).

## 10. Azioni a carico del PO

- [ ] Decidere i punti §8.1 … §8.5.
- [ ] Unire la [PR #186](https://github.com/maubernardi/nexus/pull/186) con la chiusura dello Sprint 16.
- [ ] Unire la #181 (aggiornamenti minori del backend); **non** unire #182–#184 (§8.3).
- [ ] Collaudo: T-26.1 e T-26.2 da spuntare, T-26.3 domani, poi T-24, T-25, T-28 e T-29 prima del passaggio (§8.2).
- [ ] Preparare il passaggio: dati dell'ADMIN nel `.env`, file Access sul server, `nexus-deploy` reinstallato,
  controllo in Access delle 7 chiamate (n. 11719, 26711, 28531, 31037, 34116, 36661, 40614) e delle 20 con possibili
  nomi.
- [ ] Scrivere nel registro dei trattamenti i 30 giorni dei backup, con il DPO.
