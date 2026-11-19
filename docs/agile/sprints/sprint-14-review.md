# Sprint 14 — Review, retrospettiva e planning

**Sprint:** 12 – 14 novembre 2026 (svolto dal 15 al 17/11, chiuso il 17/11) · **Review:** 19/11/2026 · **Partecipanti:**
Mauro Bernardi (Product Owner), team di sviluppo (Claude Code) · **Demo:** https://portalenexus.it, versione `2a123af` ·
**Collaudo:** [docs/test/collaudo.md](../../test/collaudo.md), T-24.1 … T-24.6 da fare (e T-23.1 … T-23.4, T-22.1 …
T-22.4 degli sprint precedenti).

> Documento unico e asincrono: review (§1–§6), retrospettiva (§7), decisioni da prendere (§8) e proposta di planning
> dello Sprint 15 (§9). La PR resta in **bozza** finché non abbiamo finito di discuterne (A12): le decisioni le aggiungo
> qui, poi la segno come pronta per il merge.

---

## 1. Sintesi

| | |
|---|---|
| **Obiettivo dello sprint** | Il Tutor indica perché un tirocinio finisce prima del previsto, lo proroga con una nuova convenzione e scarica convenzioni, progetti formativi e relazione finale compilati sui modelli reali della cooperativa, senza ritoccarli. |
| **Esito** | ✅ **Raggiunto** nel codice e sulla demo. I dati di GOL e degli enti sono nei Cataloghi (19/11); restano da caricare i 6 modelli e il collaudo. |
| **Impegno / completato** | 29 punti / **29 punti** (5 voci su 5): 21 al planning, più US-1013 (8) aggiunta dal PO il 16/11 |
| **Extra** | Maven 3.10.1 con il vincolo sulla serie 3.10.x ([#162](https://github.com/maubernardi/nexus/pull/162)), al posto della PR di Dependabot #156 |
| **Velocity** | Sprint 1: 21 · 2: 25 · 3: 21 · 4: 24 · 5: 21 · 6: 20 · 7: 31 · 8: 23 · 9: 25 · 10: 22 · 11: 15 · 12: 21 · 13: 22 · 14: **29** · media 22,9 |
| **PR unite** | 8: [#154](https://github.com/maubernardi/nexus/pull/154) (variazioni del PO), 5 per le storie (#155, #157–#160), [#161](https://github.com/maubernardi/nexus/pull/161) (chiusura), [#162](https://github.com/maubernardi/nexus/pull/162) (Maven); prima dello sprint #153 (review dello Sprint 13) |
| **Migrazioni del database** | 4, tutte solo additive: V41 (motivi di chiusura), V42 (proroghe), V43 (avviso e intestazione per progetto), V44 (promotore e variabili per progetto) |
| **Deploy della demo** | 1, a fine sprint: `2a123af` (17/11) |
| **CI** | Verde al primo tentativo in tutte le PR dello sprint. Rossa invece la PR di Dependabot #156, sostituita da #162 (§4) |

## 2. Cosa è stato consegnato

| ID | Storia | Punti | PR | In una frase |
|---|---|---|---|---|
| US-1103 | Motivo di chiusura del tirocinio | 2 | [#155](https://github.com/maubernardi/nexus/pull/155) | Nel report di restituzione, con una fine effettiva prima del previsto, il motivo dell'interruzione dal catalogo dell'ADMIN e la sua descrizione; altrimenti «Chiusura regolare». |
| US-1012 | Proroga del tirocinio | 8 | [#157](https://github.com/maubernardi/nexus/pull/157) | Una o più proroghe, ognuna una convenzione con i suoi progetti formativi; la fine prevista diventa quella dell'ultima, e monte ore e mesi contano solo i giorni coperti. |
| US-1011 | Documenti sui modelli reali (GOL) | 8 | [#158](https://github.com/maubernardi/nexus/pull/158) | Una convenzione per ogni convenzione, un progetto formativo per ogni progetto, la relazione finale a tirocinio chiuso; 120 segnaposto descritti per i modelli reali. |
| EN-8 | Preparazione dei modelli reali | 3 | [#159](https://github.com/maubernardi/nexus/pull/159) | I 6 modelli di GOL pronti fuori dal repository; il motore dei segnaposto non tocca più la formattazione dei modelli; caselle per la tipologia dell'ente. |
| US-1013 | Dati del promotore e variabili per progetto | 8 | [#160](https://github.com/maubernardi/nexus/pull/160) | I dati del promotore nel database, per progetto, al posto del `.env`; variabili libere per progetto che diventano segnaposto dei suoi modelli. |

## 3. Demo guidata

Sulla demo (`2a123af`), con le prove del collaudo:

- **T-24.1** motivo di chiusura: scelta obbligatoria con la fine effettiva anticipata, descrizione, «Chiusura
  regolare» automatica, catalogo dell'ADMIN. ⏳
- **T-24.2** proroga: date successive alla convenzione precedente, progetti formativi propri, fine prevista e monte ore
  aggiornati, sola lettura dopo il report. ⏳
- **T-24.3** intestazione e avviso per progetto, segnaposto per gruppo nella pagina dei modelli. ⏳
- **T-24.4** documenti per convenzione e per progetto, relazione finale a tirocinio chiuso. ⏳
- **T-24.5** i 6 modelli di GOL: caricamento, documenti con la formattazione originale, apertura in Word. ⏳ (modelli
  da caricare)
- **T-24.6** dati del promotore e variabili per progetto. ⏳ (i dati di GOL ci sono già, 19/11)

## 4. Qualità

### Test automatici

| | Fine Sprint 13 | Fine Sprint 14 |
|---|---|---|
| Backend – unit | 124 | 131 |
| Backend – integrazione (PostgreSQL e Keycloak veri) | 456 | 481 |
| Frontend – componenti (tutti con controllo axe) | 259 | 272 |

Da segnalare:
- **Controllo in un browser reale prima di ogni PR** (A26): per tutte le storie con interfaccia, con tastiera, axe,
  tema scuro e smartphone a 390 px. Per US-1013 l'estensione di Chrome si bloccava appena si apriva il modulo,
  probabilmente per LastPass: ho fatto il controllo con Chrome per Windows senza finestra e un profilo pulito, con il
  tema scuro vero invece di quello simulato (A29).
- **Modelli reali controllati prima del caricamento** (EN-8): caricati nel NEXUS locale, compilati con valori di prova
  e convertiti in PDF, pagina per pagina. L'apertura in Word è nel collaudo (T-24.5, punto 6).
- **Motore dei segnaposto**: test nuovi con formattazioni miste, tabulazioni, simboli e segnaposto spezzati, come nei
  modelli reali.

### Sicurezza e privacy

- **Dati del promotore** nel database per progetto (US-1013). Nome, luogo e data di nascita del legale rappresentante
  compaiono nell'audit solo come «cambiati».
- **La descrizione dell'interruzione** (US-1103) è testo libero e non entra nell'audit.
- **Modelli reali e script** che li prepara restano in `documenti_base/`, fuori dal repository: lo script contiene i
  testi originali, con i dati della cooperativa e degli enti.
- **Dependabot**: 0 avvisi di sicurezza.

### Difetti

| Difetto | Dove | Come è emerso | Correzione |
|---|---|---|---|
| Dopo un download il focus finiva sul `body` (c'era già prima) | US-1011 | Controllo in Chrome, prima della PR | Pulsante attivo con `aria-disabled` mentre scarica, nella stessa PR |
| I pulsanti dei documenti sbordavano a 390 px | US-1011 | Controllo in Chrome | Etichette a capo, nella stessa PR |
| Il motore dei segnaposto riscriveva la riga intera: grassetti, tabulazioni e caselle sparivano dalle righe compilate (c'era da US-1004) | EN-8 | Preparazione dei modelli reali | Sostituzione dei soli segnaposto, nella PR di EN-8 |
| Fasce orarie su quattro righe e «via Via Gramsci» nel progetto formativo | EN-8 | Controllo dei PDF | Corretti nei modelli, prima della PR |
| Tabella dei dati dei documenti: a 390 px faceva sbordare la pagina e, con il modulo aperto, non si scorreva da tastiera | US-1013 (tabella di US-1011) | Controllo in Chrome, axe | Regione scorrevole come le altre tabelle, nella PR di US-1013 |
| La PR di Dependabot #156 (Maven 3.10.1) non compilava: il `pom.xml` accettava solo la 3.10.0 esatta | Build | Tua domanda del 19/11 | [#162](https://github.com/maubernardi/nexus/pull/162), con il vincolo sulla serie 3.10.x |

## 5. Decisioni prese durante lo sprint

| # | Decisione | Presa da | Dove |
|---|---|---|---|
| D1 | Catalogo dei motivi di chiusura; «Chiusura regolare» automatica e non disattivabile | PO (15/11) | US-1103 |
| D2 | Con l'interruzione anticipata serve anche una descrizione, obbligatoria | PO (16/11) | US-1103 |
| D3 | La proroga è una nuova convenzione con i suoi progetti, con date successive alla precedente (non per forza il giorno dopo) e gli stessi modelli | PO (15/11) | US-1012 |
| D4 | Monte ore e mesi contano solo i giorni coperti da una convenzione | PO (16/11) | US-1012 |
| D5 | Un documento per ogni convenzione (proroghe comprese) e per ogni progetto formativo; relazione finale a tirocinio chiuso | PO (15/11) e team | US-1011 |
| D6 | Il motore dei segnaposto cambia solo il testo dei segnaposto | Team | EN-8 |
| D7 | La casella della L. 68/99 resta da spuntare a mano; del bollo resta solo «non è soggetta» | PO (16/11) | EN-8 |
| D8 | Dati del promotore nel database, per progetto, con le variabili libere; via le variabili del `.env` | PO (16/11) | US-1013 |
| D9 | Una variabile usata da un modello attivo non si toglie; la riattivazione di un modello ricontrolla i segnaposto | Team | US-1013 |
| D10 | Gli enti di presa in carico restano comuni a tutti i progetti | PO (18/11) | US-1013 |
| D11 | Il vincolo su Maven accetta la serie 3.10.x | PO (19/11) | #162 |
| D12 | US-605 nello Sprint 15; la richiesta di contatto di un'azienda (US-507) va analizzata | PO (15/11) | Backlog |

## 6. Stato del prodotto rispetto alla roadmap

**R1** — sviluppo completo; resta **US-1301** (PWA su smartphone, a cura del PO: prova T-10.1).

**R2, R3** — ✅ complete.

**R4** — *produzione*: **140 punti su 166** fatti (con le storie nuove del 15–16/11). Restano:

| Storia | Punti | Quando |
|---|---|---|
| US-605 Una mansione per più segnalazioni, una per tipo di tirocinio | 8 | Sprint 15 (proposta, §9) |
| US-1302 Backup del database | 5 | Sprint 15 se decidi la destinazione (§8.3) |
| US-1305 Revisione UX/UI | 8 (da rivedere) | Sprint dedicato |
| US-1306 Passaggio in produzione | 5 | Dopo il backup e il collaudo |

Da stimare: **US-507** Richiesta di contatto di un'azienda dal Tutor, dopo l'analisi (§8.4).

**Product backlog:** 90 elementi, 358 punti (più US-507); completati 85 (331 punti), ne restano 5 (27 punti).

## 7. Retrospettiva

**Cosa ha funzionato**
- **29 punti in due giorni**, lo sprint più grande dopo il 7: le variazioni del 15/11 erano chiare e i modelli reali
  davano tutti i dettagli.
- **Le tue variazioni sono entrate senza fermare lo sprint**: US-1013, decisa il 16/11, era unita il 17/11.
- **I modelli reali, controllati prima di caricarli**, hanno fatto emergere il problema del motore dei segnaposto prima
  che arrivasse sulla demo.
- **CI verde al primo tentativo** in tutte le PR dello sprint.

**Cosa non ha funzionato**
- **Il promotore nel `.env` è stato costruito e poi tolto.** La scelta del 09/11 non teneva conto che i dati possono
  cambiare da un progetto all'altro: configurazione, compose e README di US-1011 sono stati rifatti in US-1013.
- **La PR di Dependabot #156 è rimasta rossa tre giorni senza che te lo segnalassi**: me ne sono accorto solo quando me
  l'hai chiesto. Il vincolo esatto su Maven (EN-6) non era compatibile con gli aggiornamenti del wrapper.
- **Il limite del motore dei segnaposto è emerso solo in EN-8**: in US-1011 i test usavano modelli finti con una sola
  formattazione per riga.
- **L'estensione di Chrome si è bloccata sui moduli di US-1013**: ho dovuto rifare il controllo con un altro metodo.
- **Il collaudo è indietro**: sulla demo ci sono le funzioni di tre sprint (12, 13, 14) che non hai ancora provato.

**Azioni**

| # | Azione | Chi |
|---|---|---|
| A31 | Al planning, per ogni dato di configurazione chiedo anche se vale per tutta l'installazione o cambia per progetto (estende A30) | Team e PO |
| A32 | All'inizio di ogni review controllo le PR di Dependabot aperte e la loro CI; se una è rossa preparo subito la correzione | Team |
| A33 | Quando una storia usa modelli o file reali, i test usano anche esempi con le loro difficoltà (formattazioni miste, simboli, tabelle) | Team |
| A34 | Se l'estensione di Chrome si blocca, passo subito a Chrome senza finestra con un profilo pulito, e lo scrivo nella PR | Team |
| A26 | (confermata) Controllo in Chrome prima della PR | Team |

## 8. Da decidere

### 8.1 Chiusura dello Sprint 14
*Proposta:* Sprint 14 chiuso con velocity **29**: 21 punti pianificati più gli 8 di US-1013, aggiunta da te durante lo
sprint.

### 8.2 Contenuto dello Sprint 15
Al 15/11 lo Sprint 15 era **US-605 (8 punti)**. *Proposta:* aggiungere **US-1302 Backup del database (5 punti)**, se
scegli la destinazione (§8.3). Il backup sblocca il passaggio in produzione. *Alternative:*
- solo US-605, con le sessioni di analisi di US-507 (§8.4) e di raffinamento di US-1305;
- US-605 con la revisione UX/UI (US-1305), che però va prima raffinata con te.

### 8.3 Destinazione del backup (US-1302)
L'11/11 il backup è stato rinviato e la destinazione non è ancora decisa (review dello Sprint 13, §8.3). *Proposta:*
spazio di archiviazione **IONOS** (S3), 30 backup giornalieri cifrati, costo piccolo a consumo; le credenziali le metti
tu nel `.env` del server. *Alternative:* un altro server o un NAS via SSH; un servizio esterno (es. Backblaze B2).

### 8.4 Analisi di US-507 (richiesta di contatto di un'azienda dal Tutor)
La storia va analizzata prima di stimarla. *Proposta:* una sessione dedicata durante lo Sprint 15, sulle domande:
- chi può chiedere;
- cosa vede il Call Center e cosa risponde;
- se la richiesta crea un'azienda o una mansione;
- se servono notifiche.

### 8.5 Collaudo arretrato
Sulla demo ci sono le funzioni degli Sprint 12, 13 e 14 da collaudare (T-22, T-23, T-24). *Proposta:* collaudarle
prima del passaggio in produzione (US-1306), cominciando da T-24, che usa i dati e i modelli reali. Ti chiedo una data
indicativa, per pianificare US-1306.

## 9. Proposta per lo Sprint 15

**Date:** tre giorni da quando confermi il §8.2. **Obiettivo proposto:** *il Call Center propone la stessa mansione a
più segnalazioni, una per tipo di tirocinio, sapendo sempre se è già assegnata; i dati della produzione hanno un backup
giornaliero fuori dal server.*

| Ordine | ID | Storia | Punti |
|---|---|---|---|
| 1 | US-605 | Una mansione per più segnalazioni, al massimo una per tipo di tirocinio; tipo indicato dal Tutor nella segnalazione; avviso ben visibile | 8 |
| 2 | US-1302 | Backup giornaliero fuori dal server, con prova di ripristino documentata (se decidi il §8.3) | 5 |
| | | **Totale** | **8** (13 con US-1302) |

Il backup è indipendente da US-605 e può andare in parallelo. Prossima migrazione: **V45**.

## 10. Azioni a carico del PO

- [ ] Decidere i punti §8.1 … §8.5 e confermare il planning.
- [x] Dati di GOL (promotore, intestazione) e degli enti nei Cataloghi (19/11).
- [ ] Caricare i 6 modelli di GOL da «Modelli dei documenti» (T-24.5).
- [ ] Collaudo T-24.1 … T-24.6 sulla demo `2a123af`; T-23.1 … T-23.4 e T-22.1 … T-22.4, se non sono già fatti.
- [ ] Chiudere la PR di Dependabot #156, se Dependabot non la chiude da solo dopo #162.
- [ ] US-1301: PWA su Android e iOS (collaudo T-10.1).
