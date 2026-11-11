# Sprint 13 — Review, retrospettiva e planning

**Sprint:** 11 – 13 novembre 2026 (chiuso l'11/11, in un giorno) · **Review:** 11/11/2026 · **Partecipanti:** Mauro
Bernardi (Product Owner), team di sviluppo (Claude Code) · **Demo:** https://portalenexus.it, versione `8d43ad3` ·
**Collaudo:** [docs/test/collaudo.md](../../test/collaudo.md), T-23.1 … T-23.4 da fare (e T-22.1 … T-22.4 dello
Sprint 12); T-21.6 (prime PR di Dependabot) lunedì 16/11.

> Documento unico e asincrono: review (§1–§6), retrospettiva (§7), decisioni da prendere (§8) e proposta di planning
> dello Sprint 14 (§9). La PR resta in **bozza** finché non abbiamo finito di discuterne (A12): le decisioni le aggiungo
> qui, poi la segno come pronta per il merge.

---

## 1. Sintesi

| | |
|---|---|
| **Obiettivo dello sprint** | Aziende e beneficiari hanno indirizzi con il comune ufficiale, il Tutor registra la convenzione con uno o più progetti formativi, la fine effettiva del tirocinio e il ruolo dei tutor aziendali, e l'ADMIN carica i modelli Word con le loro condizioni: dati e modelli per i documenti reali sono pronti. |
| **Esito** | ✅ **Raggiunto.** Tutto pubblicato sulla demo; controllo in Chrome fatto prima di ogni PR; collaudo del PO da fare. |
| **Impegno / completato** | 22 punti / **22 punti** (5 voci su 5) |
| **Extra** | La tua decisione dell'11/11 su convenzione e progetti per l'avvio ([#150](https://github.com/maubernardi/nexus/pull/150)), dentro US-1008 senza punti in più |
| **Velocity** | Sprint 1: 21 · 2: 25 · 3: 21 · 4: 24 · 5: 21 · 6: 20 · 7: 31 · 8: 23 · 9: 25 · 10: 22 · 11: 15 · 12: 21 · 13: **22** · media 22,4 |
| **PR unite** | 7 (#146–#152): 5 per le storie (US-1008 in due), 1 di chiusura; prima dello sprint #145 (review dello Sprint 12) |
| **Migrazioni del database** | 4, tutte solo additive: V37 (comune negli indirizzi), V38 (fine effettiva e ruolo del tutor aziendale), V39 (convenzione e progetti formativi), V40 (modelli dei documenti) |
| **Deploy della demo** | 1, a fine sprint: `8d43ad3` |
| **CI** | Verde al primo tentativo in tutte le PR dello sprint |

## 2. Cosa è stato consegnato

| ID | Storia | Punti | PR | In una frase |
|---|---|---|---|---|
| EN-9 | Test del frontend stabili | 1 | [#146](https://github.com/maubernardi/nexus/pull/146) | La causa era il focus spostato con `requestAnimationFrame`: ora si sposta dopo il render, e il lint impedisce di ricascarci. |
| US-1007 | Indirizzi con il comune | 3 | [#147](https://github.com/maubernardi/nexus/pull/147) | Sede legale, sede del tirocinio e residenza (comune, via, numero civico) con il comune scelto dall'elenco ufficiale, con una casella con suggerimenti accessibile; nei documenti «Via Roma 1, Firenze (FI)». |
| US-1009 | Fine effettiva del tirocinio e ruolo del tutor aziendale | 2 | [#148](https://github.com/maubernardi/nexus/pull/148) | Nel report di restituzione la fine effettiva, precompilata con quella prevista; per ogni tutor aziendale la funzione o il ruolo. |
| US-1008 | Convenzione e progetti formativi | 8 | [#149](https://github.com/maubernardi/nexus/pull/149), [#150](https://github.com/maubernardi/nexus/pull/150) | Convenzione e progetti formativi numerati nella scheda, con date dentro il tirocinio e senza sovrapposizioni, mesi e monte ore; richiesti per l'avvio. |
| US-1010 | Modelli dei documenti caricati dall'ADMIN | 8 | [#151](https://github.com/maubernardi/nexus/pull/151) | Pagina «Modelli dei documenti»: caricamento con le condizioni e il controllo dei segnaposto; al download il modello giusto, la scelta tra più modelli o quello generico. |

## 3. Demo guidata

Sulla demo (`8d43ad3`), con le prove del collaudo:

- **T-23.1** indirizzi con il comune: casella con suggerimenti da tastiera, avviso dei primi 20 comuni, comune
  obbligatorio da scegliere dall'elenco, documenti con l'indirizzo composto. ⏳
- **T-23.2** fine effettiva nel report di restituzione (precompilata, non prima dell'inizio) e ruolo dei tutor
  aziendali nella scheda e nei documenti. ⏳
- **T-23.3** convenzione e progetti formativi: proposte del pulsante «Aggiungi», controlli sulle date, durata e monte
  ore; convenzione e progetti tra i dati per l'avvio. ⏳
- **T-23.4** modelli dei documenti: rifiuto dei segnaposto sconosciuti, caricamento, scelta del modello nella scheda,
  disattivazione e ripiego sul modello generico. ⏳

Per T-23.4 servono due file Word di prova: la prova spiega come prepararli da una convenzione scaricata.

## 4. Qualità

### Test automatici

| | Fine Sprint 12 | Fine Sprint 13 |
|---|---|---|
| Backend – unit | 116 | 124 |
| Backend – integrazione (PostgreSQL e Keycloak veri) | 427 | 456 |
| Frontend – componenti (tutti con controllo axe) | 231 | 259 |

Da segnalare:
- **Controllo in un browser reale prima di ogni PR** (A26 applicata): per tutte le storie con interfaccia, in locale,
  con tastiera, axe, tema scuro e smartphone a 390 px. Nel caso di US-1010 il download con il modello scelto l'ho
  provato via API, per non salvare file nel tuo browser.
- **Test stabili** (EN-9): nessun falso rosso dopo #146, in locale e in CI.
- **Modelli Word nei test**: si generano nei test (`TestDocx`), i modelli reali restano fuori dal repository.

### Sicurezza e privacy

- I modelli caricati sono file Word controllati al caricamento: dimensione massima, formato e segnaposto noti. Si
  salvano nel database come i CV, quindi entrano nel backup e non sono raggiungibili da fuori; solo l'ADMIN li vede.
- La residenza del beneficiario resta nell'audit solo come «cambiata», ora che è fatta di tre parti. Il dettaglio della
  mansione dei progetti formativi, testo libero, si registra allo stesso modo.
- Dependabot: 0 avvisi.

### Difetti

| Difetto | Dove | Come è emerso | Correzione |
|---|---|---|---|
| Con «pra» Prato non compariva: il server manda al massimo 20 comuni e la casella non lo diceva | US-1007 | Controllo in Chrome, prima della PR | Avviso «Solo i primi 20 comuni: scrivi altre lettere…» in fondo all'elenco, già nella PR |
| Icona del calendario poco visibile nel tema scuro (segnalato come difetto di tutta l'app) | — | Controllo in Chrome di US-1009 | **Falso positivo** della mia simulazione del tema scuro: nessuna correzione (§7) |

## 5. Decisioni prese durante lo sprint

| # | Decisione | Presa da | Dove |
|---|---|---|---|
| D1 | Si salva la versione del comune: un cambio di nome o di provincia non tocca i dati già inseriti | Team | US-1007 |
| D2 | La residenza è comune, via e numero civico; la via resta nella colonna di prima, così la migrazione è solo additiva | Team | US-1007 |
| D3 | La fine effettiva sta nel report di restituzione, non nella scheda; senza limiti verso il futuro (proroghe, report inviati nell'ultima settimana) | Team | US-1009 |
| D4 | I progetti formativi si salvano con la scheda; il numero è la posizione nell'ordine delle date, e togliere un progetto rinumera i successivi | Team | US-1008 |
| D5 | Mesi dei progetti arrotondati al mese più vicino, da 15 giorni per eccesso (la regola di US-1011) | Team | US-1008 |
| D6 | Per avviare il tirocinio servono la convenzione completa e almeno un progetto formativo | PO (11/11) | US-1008 |
| D7 | Le condizioni di un modello non si modificano: si disattiva e se ne carica un altro; sostituire il file cancella quello vecchio | Team | US-1010 |
| D8 | La relazione finale si carica già come modello, ma si scarica con US-1011 | Team | US-1010 |
| D9 | Icona del calendario nel tema scuro: nessuna PR, era un falso positivo | PO (11/11), dopo la verifica | — |

## 6. Stato del prodotto rispetto alla roadmap

**R1** — sviluppo completo; resta **US-1301** (PWA su smartphone, a cura del PO: prova T-10.1).

**R2, R3** — ✅ complete.

**R4** — *produzione*: **111 punti su 140** fatti. Restano:

| Storia | Punti | Quando |
|---|---|---|
| US-1011 Documenti sui modelli reali (GOL) | 8 | Sprint 14 (proposta, §9) |
| EN-8 Preparazione dei modelli reali | 3 | Sprint 14 |
| US-1302 Backup del database | 5 | Sprint 14 se decidi la destinazione (§8.3), altrimenti dopo |
| US-1305 Revisione UX/UI | 8 (da rivedere) | Sprint dedicato |
| US-1306 Passaggio in produzione | 5 | Dopo il backup |

**Product backlog:** 86 elementi, 332 punti; completati 80 (302 punti), ne restano 6 (30 punti).

## 7. Retrospettiva

**Cosa ha funzionato**
- **Sprint chiuso in un giorno, con l'impegno pieno**, come lo Sprint 12: le storie erano chiare perché venivano dai
  modelli reali.
- **Il controllo in Chrome prima della PR (A26)** ha trovato l'avviso mancante dei 20 comuni prima del merge, senza PR
  in più.
- **Le tue decisioni sono diventate subito PR piccole**: convenzione e progetti per l'avvio in #150, verificata e unita
  nella stessa sera.
- **CI verde al primo tentativo** in tutte le PR; nessun test instabile dopo EN-9.

**Cosa non ha funzionato**
- **Ho segnalato un difetto che non c'era.** L'icona del calendario «poco visibile nel tema scuro» dipendeva da come
  simulavo il tema: copiavo i colori scuri lasciando il sistema in tema chiaro. Hai preso una decisione (PR a parte)
  su un problema inesistente. Me ne sono accorto preparando la correzione e non ho aperto la PR.
- **Un criterio mancava nel planning**: se convenzione e progetti fossero obbligatori per l'avvio. Te l'ho chiesto
  solo alla consegna di US-1008, ed è servita una seconda PR.
- **Nel controllo di US-1010 l'estensione di Chrome ha perso la pagina** una volta; è bastato ricaricarla.

**Azioni**

| # | Azione | Chi |
|---|---|---|
| A29 | Prima di segnalare un difetto visto nel controllo in Chrome, verifico che non dipenda dalla simulazione (tema scuro con `color-scheme`, smartphone nell'iframe) | Team |
| A30 | Al planning, per ogni dato nuovo della scheda chiedo se serve per l'avvio del tirocinio o per i documenti | Team e PO |
| A26 | (confermata) Controllo in Chrome prima della PR | Team (e PO per l'estensione) |

## 8. Da decidere

### 8.1 Chiusura dello Sprint 13
*Proposta:* Sprint 13 chiuso con velocity **22**.

### 8.2 Contenuto dello Sprint 14
Al planning del 09/11 lo Sprint 14 era **US-1011 + EN-8 (11 punti)**, metà della velocity media. *Proposta:* aggiungere
**US-1302 Backup del database (5 punti)**, se scegli la destinazione (§8.3). Il backup sblocca il passaggio in
produzione, e i documenti reali (US-1011, EN-8) dipendono dai dati che chiedo al §8.4, quindi possono avere tempi
morti. *Alternative:*
- solo US-1011 + EN-8, sprint più corto;
- iniziare la revisione UX/UI (US-1305), che però va prima raffinata con te in una sessione dedicata.

### 8.3 Destinazione del backup (US-1302)
Riprende la domanda della review dello Sprint 11 (§8.5). *Proposta:* spazio di archiviazione **IONOS** (S3), 30 backup
giornalieri cifrati, costo piccolo a consumo; le credenziali le metti tu nel `.env` del server. *Alternative:* un altro
server o un NAS via SSH; un servizio esterno (es. Backblaze B2).

### 8.4 Dati che servono per i documenti reali (US-1011)
Per compilare i documenti senza righe vuote servono, prima o durante lo Sprint 14:
- i **dati del soggetto promotore** (denominazione, sede, codice fiscale, legale rappresentante con luogo e data di
  nascita, contatti, posizione INAIL, polizza RC con scadenza 22/07/2027). Come deciso il 09/11, vanno nel `.env` del
  server: preparo i nomi delle variabili, i valori li inserisci tu;
- i **dati reali degli enti** (referenti, legali rappresentanti), nei Cataloghi;
- per ogni progetto, il **riferimento all'avviso** e la **riga con codice e CUP**, che l'ADMIN potrà modificare con
  US-1011.

*Proposta:* li prepari mentre sviluppo US-1011; i modelli reali (EN-8) li carichiamo quando sono pronti.

## 9. Proposta per lo Sprint 14

**Date:** tre giorni da quando confermi il §8.2. **Obiettivo proposto:** *il Tutor scarica convenzione, progetti
formativi e relazione finale compilati sui modelli reali della cooperativa, senza ritoccarli; i dati della produzione
hanno un backup giornaliero fuori dal server.*

| Ordine | ID | Storia | Punti |
|---|---|---|---|
| 1 | US-1011 | Documenti sui modelli reali: segnaposto nuovi, un progetto formativo per ogni progetto, relazione finale a tirocinio chiuso | 8 |
| 2 | EN-8 | Preparazione dei 6 modelli di GOL (Extracurriculare e Inclusione), fuori dal repository, caricati dall'ADMIN | 3 |
| 3 | US-1302 | Backup giornaliero fuori dal server, con prova di ripristino documentata (se decidi il §8.3) | 5 |
| | | **Totale** | **11** (16 con US-1302) |

Prima US-1011, perché EN-8 usa i suoi segnaposto (§8.4 della review dello Sprint 12); il backup è indipendente e può
andare in parallelo mentre aspettiamo i dati del §8.4. Prossima migrazione: **V41**.

## 10. Azioni a carico del PO

- [ ] Decidere i punti §8.1 … §8.4 e confermare il planning.
- [ ] Collaudo dello Sprint 13 sulla demo `8d43ad3` (T-23.1 … T-23.4).
- [ ] Collaudo dello Sprint 12 (T-22.1 … T-22.4), se non è già fatto.
- [ ] I dati del §8.4 per i documenti reali.
- [ ] T-21.6: lunedì 16/11, le prime PR di Dependabot.
- [ ] US-1301: PWA su Android e iOS (collaudo T-10.1).
