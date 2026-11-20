# Sprint 15 — Review, retrospettiva e planning

**Sprint:** 19 – 21 novembre 2026 (svolto il 19 e il 20/11, chiuso il 20/11) · **Review:** 20/11/2026 ·
**Partecipanti:** Mauro Bernardi (Product Owner), team di sviluppo (Claude Code) · **Demo:** https://portalenexus.it,
versione `da10853` · **Collaudo:** [docs/test/collaudo.md](../../test/collaudo.md), T-25.1 … T-25.5 da fare (e T-24,
T-23, T-22 degli sprint precedenti).

> Documento unico e asincrono: review (§1–§6), retrospettiva (§7), decisioni da prendere (§8) e proposta di planning
> dello Sprint 16 (§9). La PR resta in **bozza** finché non abbiamo finito di discuterne (A12): le decisioni le aggiungo
> qui, poi la segno come pronta per il merge. La chiusura dello sprint (change archiviate, backlog, collaudo) è nella
> [PR #171](https://github.com/maubernardi/nexus/pull/171).

---

## 1. Sintesi

| | |
|---|---|
| **Obiettivo dello sprint** | Le aziende del database attuale possono entrare in NEXUS con tutti i loro dati: categoria, referenti, recapiti e chiamate in archivio, con un'importazione provata e i dati da completare riconoscibili. Il Call Center propone la stessa mansione a più segnalazioni, una per tipo di tirocinio. |
| **Esito** | ✅ **Raggiunto** nel codice e sulla demo. L'importazione è provata sul file vero, in locale; quella definitiva si fa con il passaggio in produzione (US-1306). |
| **Impegno / completato** | 27 punti / **27 punti** (5 storie su 5), sopra la media come avevi scelto al planning |
| **Extra** | Analisi delle utenze nel database attuale (20/11), su tua domanda (§8.4) |
| **Velocity** | Sprint 1: 21 · 2: 25 · 3: 21 · 4: 24 · 5: 21 · 6: 20 · 7: 31 · 8: 23 · 9: 25 · 10: 22 · 11: 15 · 12: 21 · 13: 22 · 14: 29 · 15: **27** · media 23,1 |
| **PR unite** | 6: [#164](https://github.com/maubernardi/nexus/pull/164) (analisi del database attuale e planning) e 5 per le storie (#165–#169); prima dello sprint #161 (chiusura dello Sprint 14), #162 (Maven) e #163 (review dello Sprint 14). [#170](https://github.com/maubernardi/nexus/pull/170) (rubrica dei case manager) chiusa senza merge (§5, D12) |
| **Migrazioni del database** | 5, tutte solo additive: V45 (unicità della partita IVA), V46 (categorie merceologiche), V47 (referenti e recapiti), V48 (archivio delle chiamate), V49 (tipo di tirocinio sulla segnalazione e assegnazione per tipo) |
| **Deploy della demo** | 1, a fine sprint: `da10853` (20/11) |
| **CI** | Verde al primo tentativo in tutte le PR dello sprint |

## 2. Cosa è stato consegnato

| ID | Storia | Punti | PR | In una frase |
|---|---|---|---|---|
| US-508 | Partita IVA e nome da completare | 3 | [#165](https://github.com/maubernardi/nexus/pull/165) | Le aziende senza partita IVA o senza nome hanno un segnaposto che non si può salvare, compaiono come «Da completare» e si filtrano; nei documenti diventano la riga da completare. |
| US-509 | Categoria merceologica e attività dell'azienda | 3 | [#166](https://github.com/maubernardi/nexus/pull/166) | Catalogo delle 53 categorie del database attuale, categoria e attività nella scheda dell'azienda, filtro nell'elenco e nel dettaglio dell'abbinamento. |
| US-510 | Referenti e recapiti dell'azienda | 5 | [#167](https://github.com/maubernardi/nexus/pull/167) | Fino a 10 referenti per azienda con ruolo e recapiti, catalogo dei 12 ruoli, cellulare, fax, seconda email e sede operativa. |
| US-1309 | Importazione dal database attuale | 8 | [#168](https://github.com/maubernardi/nexus/pull/168) | Le 13.799 aziende con sedi, recapiti, referenti, categoria e note, più le 38.789 chiamate in archivio, con un resoconto delle righe da rivedere; ripetibile senza duplicati. |
| US-605 | Una mansione per più segnalazioni, una per tipo di tirocinio | 8 | [#169](https://github.com/maubernardi/nexus/pull/169) | Il Tutor indica il tipo di tirocinio nella segnalazione; la stessa mansione si propone a una segnalazione per tipo, con l'avviso «Già assegnata alla segnalazione n. …». |

## 3. Demo guidata

Sulla demo (`da10853`), con le prove del collaudo:

- **T-25.1** aziende da completare: segnaposto rifiutati dal modulo, «Da completare» nell'elenco e nella scheda,
  filtro, riga da completare nei documenti. ⏳
- **T-25.2** categoria merceologica e attività: catalogo dell'ADMIN, scheda, filtro dell'elenco, dettaglio
  dell'abbinamento. ⏳
- **T-25.3** referenti, recapiti e sede operativa: aggiunta, ordine e rimozione dei referenti, catalogo dei ruoli. ⏳
- **T-25.4** importazione dal database attuale: in locale, sul tuo PC, con il file Access (procedura nell'analisi,
  §9.1). ⏳
- **T-25.5** una mansione per più segnalazioni: tipo nella segnalazione, correzione del Tutor, avvisi
  nell'abbinamento, stato delle mansioni dell'azienda. ⏳ La segnalazione n. 3 della demo è Curriculare, per vedere
  l'avviso sulla mansione della n. 4.

## 4. Qualità

### Test automatici

| | Fine Sprint 14 | Fine Sprint 15 |
|---|---|---|
| Backend – unit | 131 | 190 |
| Backend – integrazione (PostgreSQL e Keycloak veri) | 481 | 528 |
| Frontend – componenti (tutti con controllo axe) | 272 | 292 |

Da segnalare:
- **Importazione provata sul file vero** prima della PR, in un database di prova: 13.799 aziende in circa 11 secondi.
  Ha trovato due difetti che i dati inventati dei test non avrebbero mostrato (A33). I test d'integrazione usano un
  file Access costruito nel test, con dati inventati.
- **Il file di Access è «codificato»**: si legge con Jackcess e jackcess-encrypt, con BouncyCastle all'ultima versione
  stabile (1.87). Sul server non serve mdbtools.
- **Compatibilità con la release precedente (A16)** anche per US-605, che cambia il modello della mansione: un test
  applica V48 con i dati, poi V49, e controlla che stato e blocco della mansione restino coerenti.
- **Controllo in un browser reale prima di ogni PR** (A26), con tastiera, axe, tema scuro e smartphone a 390 px.
  L'estensione di Chrome si è bloccata ancora sui moduli dell'azienda in US-508 e US-509 (LastPass): ho fatto subito il
  controllo senza finestra (A34), come scritto nelle PR.

### Sicurezza e privacy

- **Il file Access e il resoconto restano fuori dal repository.** L'importazione non ha endpoint: il file non passa da
  HTTP. Il resoconto e i log hanno solo numeri e conteggi, con le cifre lunghe nascoste.
- **Nell'analisi e in chat solo dati aggregati** del database attuale: nessun nome, telefono, email o testo di nota.
- **Referenti, cellulare, fax e seconda email** compaiono nell'audit solo come «cambiati».
- **Le chiamate** sono nell'archivio, non visibili nell'interfaccia (D4). Le note citano i beneficiari con le
  iniziali; 7 note con nome e cognome e 20 con possibili nomi vanno controllate prima dell'importazione vera (§10).
- **Dependabot** (A32): nessuna PR aperta, 0 avvisi di sicurezza; la PR #156 è chiusa.

### Difetti

| Difetto | Dove | Come è emerso | Correzione |
|---|---|---|---|
| Cambiare solo i referenti non incrementava la versione dell'azienda: un collega con la scheda aperta poteva sovrascrivere la modifica | US-510 | Test d'integrazione | Versione incrementata a ogni cambio dei referenti, nella stessa PR |
| Le email di Access sono collegamenti `http://indirizzo`: lo stesso indirizzo finiva anche come seconda email | US-1309 | Prova sul file vero | Collegamenti ridotti all'indirizzo, prima della PR |
| Alcuni comuni scritti a metà venivano abbinati fuori Toscana («SESTO FIORENTION» → Sesto, BZ) | US-1309 | Prova sul file vero | Regola sulle province di casa e alias, prima della PR |

## 5. Decisioni prese durante lo sprint

| # | Decisione | Presa da | Dove |
|---|---|---|---|
| D1 | NEXUS sostituisce in tutto il database attuale: categoria, referenti e recapiti entrano in NEXUS | PO (19/11) | Planning |
| D2 | Partita IVA mancante «00000000000» e nome «Azienda senza nome n. …», che il programma non lascia salvare | PO (19/11) | US-508 |
| D3 | Si importano tutte le aziende | PO (19/11) | US-1309 |
| D4 | Le chiamate vanno in un archivio, non visibile nell'interfaccia | PO (19/11) | US-1309 |
| D5 | L'importazione si prova in locale; sulla demo restano i dati dimostrativi; quella vera si fa con US-1306 | PO (19/11) | US-1309 |
| D6 | Unicità della partita IVA come indice univoco parziale; anche il nome segnaposto diventa, nei documenti, la riga da completare | Team | US-508 |
| D7 | Categorie e ruoli iniziali con l'id fisso legato al numero di Access, così l'importazione li ritrova anche dopo una rinomina | Team | US-509, US-510 |
| D8 | Fino a 10 referenti, ognuno con almeno il nome o il ruolo; `contact_person` resta per il ripristino (A16) | Team | US-510 |
| D9 | L'importazione è un'istanza del backend che legge il file e si chiude, senza endpoint | Team | US-1309 |
| D10 | L'assegnazione della mansione sta sulla segnalazione, con un indice univoco per mansione e tipo; lo stato BLOCCATA resta come riassunto (A16) | Team | US-605 |
| D11 | Le segnalazioni aperte senza scheda restano «tipo non indicato» e non si propongono finché il Tutor non lo indica | PO (15/11) | US-605 |
| D12 | Niente rubrica dei case manager: si inseriscono a mano nella scheda del beneficiario; gli assistenti sociali di Access non si importano | PO (20/11) | Analisi delle utenze, PR #170 chiusa |
| D13 | Il backup resta nel backlog fino a dopo questo sprint | PO (19/11) | US-1302 |

## 6. Stato del prodotto rispetto alla roadmap

**R1** — sviluppo completo; resta **US-1301** (PWA su smartphone, a cura del PO: prova T-10.1).

**R2, R3** — ✅ complete.

**R4** — *produzione*: **167 punti su 185** fatti. Restano:

| Storia | Punti | Quando |
|---|---|---|
| US-1302 Backup del database | 5 | Sprint 16 (proposta, §9), se decidi la destinazione (§8.3) |
| US-1306 Passaggio in produzione | 5 | Sprint 16 (proposta, §9), dopo il collaudo (§8.5) |
| US-1305 Revisione UX/UI | 8 (da rivedere) | Sprint dedicato (§8.7) |

Da stimare: **US-507** Richiesta di contatto di un'azienda dal Tutor, dopo l'analisi (§8.6).

**Product backlog:** 94 elementi, 377 punti (più US-507); completati 90 (358 punti), ne restano 4 (19 punti).

## 7. Retrospettiva

**Cosa ha funzionato**
- **27 punti in due giorni**: l'analisi del database attuale aveva già risposto alle domande, quindi le storie sono
  partite tutte Ready.
- **La prova sul file vero** ha trovato due difetti dell'importazione prima della PR (A33).
- **Gli id fissi presi da Access** rendono l'importazione ripetibile e indipendente dalle rinomine dei cataloghi.
- **CI verde al primo tentativo** in tutte le PR, e nessun avviso di Dependabot (A32).
- **US-605 ha cambiato il modello della mansione senza rompere la release precedente** (A16), con un test di migrazione
  sui dati.

**Cosa non ha funzionato**
- **La rubrica dei case manager è arrivata fino alla PR ed è stata scartata** (#170). L'avevo proposta io partendo dai
  dati di Access. Bastava una domanda su come inserite oggi i case manager per capire subito che non serviva.
- **Alcune attese della CI sono rimaste appese in background.** Usavano un'opzione (`--json`) che qui non esiste e
  giravano senza fine. Le ho fermate quando mi hai chiesto se c'erano attività in corso.
- **L'estensione di Chrome si è bloccata su due storie su cinque** (LastPass). A34 ha evitato di perdere tempo, ma il
  controllo senza finestra non prova la tastiera come quello con l'estensione.
- **Il collaudo è indietro di quattro sprint** (T-22 … T-25) e i 6 modelli di GOL non sono ancora caricati. Ora è
  quello che separa NEXUS dalla produzione.

**Azioni**

| # | Azione | Chi |
|---|---|---|
| A35 | Quando dall'analisi dei dati nasce l'idea di una storia, prima di scriverla chiedo come lavorate oggi su quel punto | Team |
| A36 | Per attendere la CI uso solo `gh pr checks N --watch`; alla fine di ogni storia controllo che non restino attività in background | Team |
| A34 | (confermata) Se l'estensione di Chrome si blocca, controllo senza finestra con un profilo pulito, e lo scrivo nella PR | Team |
| A26 | (confermata) Controllo in Chrome prima della PR | Team |

## 8. Da decidere

### 8.1 Chiusura dello Sprint 15
*Proposta:* Sprint 15 chiuso con velocity **27**, tutto l'impegno del planning (chiusura nella
[PR #171](https://github.com/maubernardi/nexus/pull/171)).

> ✅ **Deciso (PO, 20/11): proposta accettata.**

### 8.2 Contenuto dello Sprint 16
Nel backlog restano il backup, il passaggio in produzione, la revisione UX/UI e US-507 da analizzare. *Proposta:* uno
sprint per **andare in produzione**: US-1302 (backup) e US-1306 (passaggio in produzione), 10 punti (§9). Con meno
punti della media resta tempo per correggere quello che emerge dal collaudo. Servono prima le decisioni §8.3, §8.4 e
§8.5. *Alternative:*
- prima la revisione UX/UI (US-1305), poi la produzione: va però raffinata con te;
- prima l'analisi di US-507, se ti serve in produzione dal primo giorno.

> ✅ **Deciso (PO, 20/11): proposta accettata**, Sprint 16 = US-1302 + US-1306. Prima del planning, un approfondimento
> sui costi del backup (§8.3).

### 8.3 Destinazione del backup (US-1302)
È in sospeso dalla review dello Sprint 13. *Proposta:* spazio di archiviazione **IONOS** (S3), 30 backup giornalieri
cifrati, costo piccolo a consumo; le credenziali le metti tu nel `.env` del server. *Alternative:* un altro server o un
NAS via SSH; un servizio esterno (es. Backblaze B2).

> ✅ **Deciso (PO, 20/11): IONOS va bene, ma prima di partire il PO vuole capire i costi.** Approfondimento del 20/11:
> - il backup compresso con tutti i dati del database attuale importati pesa circa **3,6 MB** (prova in locale);
>   crescerà soprattutto con i CV, che l'anonimizzazione cancella dopo 2 anni;
> - listino IONOS Cloud in vigore dal 29/10/2026: **0,007 € per GB ogni 30 giorni**, IVA esclusa; richieste gratuite;
>   traffico in uscita gratuito fino a 2 TB al mese;
> - con 30 copie giornaliere la spesa resta sotto 1 € al mese anche con molti CV. Il prezzo del proprio contratto si
>   controlla nell'area clienti IONOS prima di attivare il servizio;
> - prezzo del contratto del PO, visto nell'area clienti alla creazione: **0,008 € per GiB ogni 30 giorni**. Con 30
>   copie: meno di 0,01 € al mese oggi, circa 0,06 € con 500 CV da 0,5 MB, circa 0,60 € nel caso estremo (IVA esclusa).

### 8.4 Ambiente e utenze della produzione (US-1306)
- **Dove gira la produzione.** Il server di oggi ospita la demo e ha poca memoria per due installazioni complete
  (Keycloak, backend e database) una accanto all'altra. *Proposta:* lo stesso server diventa la produzione: via i dati
  dimostrativi e gli utenti di prova, dentro le utenze vere e l'importazione del database attuale. Le prove successive
  si fanno in locale. *Alternativa:* un secondo server per la demo.
- **Le utenze.** Dall'analisi del 20/11:
  - i Tutor non sono nel database attuale: l'elenco (nome, cognome, email, progetti) deve arrivare dalla cooperativa;
  - degli operatori del Call Center ce ne sono 35, ma solo 6 attivi dal 2025 e nessuno con l'email;
  - gli altri 237 «assistenti» sono assistenti sociali degli enti, cioè case manager (D12).

  *Proposta:* mi mandi un elenco con nome, cognome, email, ruolo e progetti di ognuno. Se le persone sono una
  trentina o meno, l'ADMIN le invita a mano da «Nuovo utente»; se sono di più, ne parliamo prima del planning.

> ✅ **Deciso (PO, 20/11): lo stesso server diventa la produzione.** Al passaggio c'è solo l'utenza dell'ADMIN; gli
> altri account li crea il PO.

### 8.5 Collaudo prima della produzione
Sulla demo ci sono le funzioni degli Sprint 12–15 da collaudare (T-22 … T-25) e i 6 modelli di GOL da caricare
(T-24.5). *Proposta:* collaudo prima di US-1306, cominciando da T-24 e T-25, che usano i modelli e i dati veri. Ti
chiedo una data indicativa, per fissare il passaggio in produzione.

> ✅ **Deciso (PO, 20/11): la data si decide durante lo Sprint 16.**

### 8.6 Analisi di US-507 (richiesta di contatto di un'azienda dal Tutor)
In sospeso dalla review dello Sprint 14 (§8.4): le domande sono lì. *Proposta:* una sessione durante lo Sprint 16;
la storia si stima dopo, e può entrare in produzione in un secondo momento.

> ✅ **Deciso (PO, 20/11): proposta accettata.**

### 8.7 Revisione UX/UI (US-1305)
Hai deciso l'08/11 che si fa in uno sprint dedicato. *Proposta:* dopo il passaggio in produzione, partendo dalle prime
settimane d'uso reale di Tutor e Call Center: è quello che serve per studiare i flussi veri.

> ✅ **Deciso (PO, 20/11): proposta accettata.**

### 8.8 Chiamate del database attuale
Il 19/11 hai deciso che restano nell'archivio, non visibili (D4). Il 20/11 ti ho proposto di mostrarle in sola lettura
nella pagina dell'azienda (circa 3 punti), e non l'hai scelta. *Proposta:* resta valida la decisione del 19/11; i dati
restano nell'archivio e la storia si può scrivere quando servirà.

> ✅ **Deciso (PO, 20/11): proposta accettata.**

## 9. Proposta per lo Sprint 16

**Date:** tre giorni da quando confermi il §8.2. **Obiettivo proposto:** *NEXUS va in produzione con le utenze vere, i
dati del database attuale importati e un backup giornaliero fuori dal server, provato con un ripristino.*

| Ordine | ID | Storia | Punti |
|---|---|---|---|
| 1 | US-1302 | Backup giornaliero fuori dal server, cifrato, con prova di ripristino documentata | 5 |
| 2 | US-1306 | Passaggio in produzione: via i dati dimostrativi, utenze vere, importazione del database attuale sul server (US-1309, §9.2 dell'analisi), checklist di sicurezza | 5 |
| | | **Totale** | **10** |

Prima del planning servono: la destinazione del backup (§8.3), l'ambiente e l'elenco delle utenze (§8.4), il collaudo
(§8.5). US-1306 va raffinata insieme al planning: oggi ha un criterio solo. Prossima migrazione: **V50**.

> ✅ **Deciso (PO, 20/11): proposta accettata.** Il planning parte dopo l'approfondimento sui costi del backup (§8.3).

## 10. Azioni a carico del PO

- [x] Decidere i punti §8.1 … §8.8 (20/11).
- [ ] Unire la [PR #171](https://github.com/maubernardi/nexus/pull/171) con la chiusura dello Sprint 15.
- [ ] Controllare in Access le 7 chiamate con nome e cognome (n. 11719, 26711, 28531, 31037, 34116, 36661, 40614) e le
  20 che il resoconto indica come possibili nomi, prima dell'importazione vera.
- [x] Controllare nell'area clienti IONOS il prezzo dell'Object Storage del proprio contratto (§8.3): 0,008 €/GiB
  ogni 30 giorni.
- [ ] Creare gli altri account della produzione dopo il passaggio; l'utenza dell'ADMIN arriva con US-1306 (§8.4).
- [ ] Caricare i 6 modelli di GOL da «Modelli dei documenti» (T-24.5).
- [ ] Collaudo T-25.1 … T-25.5 sulla demo `da10853` (T-25.4 in locale); T-24.1 … T-24.6, T-23.1 … T-23.4 e
  T-22.1 … T-22.4, se non sono già fatti.
- [ ] US-1301: PWA su Android e iOS (collaudo T-10.1).
