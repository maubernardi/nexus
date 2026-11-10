# Sprint 12 — Review, retrospettiva e planning

**Sprint:** 12 – 14 novembre 2026 (iniziato il 09/11 sera dopo il planning, chiuso il 10/11, in un giorno) · **Review:**
10/11/2026 · **Partecipanti:** Mauro Bernardi (Product Owner), team di sviluppo (Claude Code) · **Demo:**
https://portalenexus.it, versione `ed260ad` · **Collaudo:** [docs/test/collaudo.md](../../test/collaudo.md), T-22.1 …
T-22.4 da fare; T-21.6 (prime PR di Dependabot) lunedì 16/11.

> Documento unico e asincrono: review (§1–§6), retrospettiva (§7), decisioni da prendere (§8) e proposta di planning
> dello Sprint 13 (§9). La PR resta in **bozza** finché non abbiamo finito di discuterne (A12): le decisioni le aggiungo
> qui, poi la segno come pronta per il merge.

---

## 1. Sintesi

| | |
|---|---|
| **Obiettivo dello sprint** | NEXUS conosce comuni e province italiane, le sedi di competenza, i tipi di tirocinio di ogni progetto e gli enti che hanno in carico i beneficiari; il Tutor indica la sede di competenza della segnalazione e i contatti, l'ente e il case manager del beneficiario. |
| **Esito** | ✅ **Raggiunto.** Tutto pubblicato sulla demo; controllo in Chrome fatto, con due difetti corretti; collaudo del PO da fare. |
| **Impegno / completato** | 21 punti / **21 punti** (4 storie su 4) |
| **Extra** | Le correzioni del controllo in Chrome ([#144](https://github.com/maubernardi/nexus/pull/144)), senza punti |
| **Velocity** | Sprint 1: 21 · 2: 25 · 3: 21 · 4: 24 · 5: 21 · 6: 20 · 7: 31 · 8: 23 · 9: 25 · 10: 22 · 11: 15 · 12: **21** · media 22,4 |
| **PR unite** | 6 (#139–#144): 4 storie, 1 di correzioni, 1 di chiusura; prima dello sprint #137 (review dello Sprint 11) e #138 (planning) |
| **Migrazioni del database** | 4: V33 (comuni e province), V34 (cataloghi dei documenti), V35 (presa in carico del beneficiario), V36 (sede di competenza della segnalazione) |
| **Deploy della demo** | 2, riusciti e controllati online: `c8e810d` (le quattro storie) e `ed260ad` (le correzioni di #144) |
| **CI** | Verde al primo tentativo in tutte le PR dello sprint |

## 2. Cosa è stato consegnato

| ID | Storia | Punti | PR | In una frase |
|---|---|---|---|---|
| US-114 | Comuni e province | 8 | [#139](https://github.com/maubernardi/nexus/pull/139) | Tutti i comuni e le province italiane con le date di validità; l'ADMIN carica il file dell'ISTAT e vede le differenze prima di confermare (nella demo 7896 comuni e 107 province). |
| US-115 | Sedi di competenza, tipi per progetto, enti di presa in carico | 5 | [#140](https://github.com/maubernardi/nexus/pull/140) | Nei Cataloghi le sedi assegnate ai progetti, i tipi di tirocinio ammessi da ogni progetto (GOL: Extracurriculare e Inclusione) e la scheda dei quattro enti. |
| US-206 | Contatti, ente di presa in carico e case manager del beneficiario | 5 | [#141](https://github.com/maubernardi/nexus/pull/141) | Mail e telefono, ente e case manager; l'ente non cambia durante il tirocinio; «Di inclusione» solo con ente e case manager completi. |
| US-308 | Sede di competenza della segnalazione | 3 | [#142](https://github.com/maubernardi/nexus/pull/142) | Il Tutor sceglie la sede quando crea la segnalazione (anche dalla bacheca); dopo la correggono solo ADMIN e Coordinatore, con la traccia nella cronologia. |
| — | Correzioni del controllo in Chrome | — | [#144](https://github.com/maubernardi/nexus/pull/144) | I due difetti del §4, corretti e ricontrollati in Chrome. |

## 3. Demo guidata

Sulla demo (`ed260ad`), con le prove del collaudo:

- **T-22.1** comuni e province: stato dell'elenco, ricerca, anteprima di un file dell'ISTAT e rifiuto di un file
  sbagliato. ⏳
- **T-22.2** cataloghi: sedi con i loro progetti, tipi di tirocinio di GOL, dati di un ente. ⏳
- **T-22.3** beneficiario: contatti, ente e case manager; «Di inclusione» non disponibile con i dati mancanti; ente
  bloccato durante il tirocinio. ⏳
- **T-22.4** segnalazione: sede obbligatoria per GOL, correzione dell'ADMIN con la cronologia, «Non indicata» in un
  progetto senza sedi. ⏳

## 4. Qualità

### Test automatici

| | Fine Sprint 11 | Fine Sprint 12 |
|---|---|---|
| Backend – unit | 108 | 116 |
| Backend – integrazione (PostgreSQL e Keycloak veri) | 384 | 427 |
| Frontend – componenti (tutti con controllo axe) | 215 | 231 |

Da segnalare:
- **Controllo in un browser reale** (punto 3 della Definition of Done): tutte le schermate nuove passano axe con il
  contrasto, in tema chiaro e scuro e su smartphone a 390 px. US-114 e US-115 le ho controllate durante lo sviluppo,
  US-206 e US-308 solo dopo il merge, perché l'estensione di Chrome era scollegata (§7).
- **La lettura del file dell'ISTAT è provata con il file vero** (7896 comuni), non solo con file finti.
- **Due test instabili** (§8.3): `UserProjectsPage` (il focus dopo un errore arriva in ritardo) è stato rosso una volta
  in CI il 09/11, su una PR di sola documentazione; il test della sezione dei comuni è fallito una volta in locale il
  10/11. Rilanciati, sono passati entrambi.

### Sicurezza e privacy

L'ente di presa in carico è un dato delicato come la L. 68/99: si vede solo dove si vede il beneficiario, non finisce
mai nei log, l'audit registra quali campi cambiano senza i valori e l'anonimizzazione lo cancella insieme a mail,
telefono e case manager. Dependabot: 0 avvisi.

### Difetti

| Difetto | Dove | Come è emerso | Correzione |
|---|---|---|---|
| Lo screen reader non leggeva perché «Di inclusione» non è disponibile: la spiegazione non era collegata alla tendina | US-206 | Controllo in Chrome, dopo il merge | [#144](https://github.com/maubernardi/nexus/pull/144): spiegazione collegata al campo (`aria-describedby`) |
| Nei progetti senza sedi attive l'ADMIN vedeva «Correggi la sede di competenza», con una tendina vuota | US-308 | Controllo in Chrome, dopo il merge | [#144](https://github.com/maubernardi/nexus/pull/144): il comando compare solo se il progetto ha sedi attive; la sede resta «Non indicata» |

## 5. Decisioni prese durante lo sprint

| # | Decisione | Presa da | Dove |
|---|---|---|---|
| D1 | Il comune si riconosce dal codice catastale, che non cambia quando passa a un'altra provincia (il codice ISTAT sì) | Team | US-114 |
| D2 | File dell'ISTAT riconosciuto dai nomi delle colonne; rifiutato con meno di 7000 comuni o con una data non successiva all'ultimo caricamento | Team | US-114 |
| D3 | Nella demo l'elenco dell'ISTAT si carica da solo se le tabelle sono vuote; in produzione lo carica l'ADMIN la prima volta | Team | US-114 |
| D4 | Un progetto senza tipi indicati ammette tutti i tipi; la scheda del tirocinio propone i tipi ammessi più quello già scelto | Team | US-115 |
| D5 | Tirocinio aperto = segnalazione con la scheda del tirocinio e non ancora in FORM_RESTITUZIONE | Team, da confermare (§8.2) | US-206 |
| D6 | Nell'audit della presa in carico solo quali campi cambiano, senza i valori | Team | US-206 |
| D7 | «Di inclusione» resta visibile ma disattivato, con l'elenco dei dati mancanti | Team | US-206 |
| D8 | La sede si sceglie anche nella candidatura dalla bacheca; con una sola sede nel progetto è già selezionata | Team | US-308 |
| D9 | Progetto senza sedi attive: la sede resta «Non indicata» e la correzione non si propone | PO (10/11) | US-308 |
| D10 | Il team risponde sempre in italiano, in tutte le chat | PO (10/11) | Processo |

## 6. Stato del prodotto rispetto alla roadmap

**R1** — sviluppo completo; resta **US-1301** (PWA su smartphone, a cura del PO: prova T-10.1).

**R2, R3** — ✅ complete.

**R4** — *produzione*: **89 punti su 139** fatti (la release è cresciuta con i documenti reali). Restano:

| Storia | Punti | Quando |
|---|---|---|
| US-1007 Indirizzi con il comune | 3 | Sprint 13 (proposta, §9) |
| US-1008 Convenzione e progetti formativi | 8 | Sprint 13 |
| US-1009 Fine effettiva e ruolo del tutor aziendale | 2 | Sprint 13 |
| US-1010 Modelli dei documenti caricati dall'ADMIN | 8 | Sprint 13 |
| US-1011 Documenti sui modelli reali (GOL) | 8 | Sprint 14 |
| EN-8 Preparazione dei modelli reali | 3 | Sprint 14 |
| US-1305 Revisione UX/UI | 8 (da rivedere) | Sprint dedicato |
| US-1302 Backup del database | 5 | Dopo i documenti reali |
| US-1306 Passaggio in produzione | 5 | Dopo il backup |

**Product backlog:** 85 elementi, 331 punti; completati 75 (280 punti), ne restano 10 (51 punti).

## 7. Retrospettiva

**Cosa ha funzionato**
- **Sprint chiuso in un giorno, con l'impegno pieno**: le storie erano chiare perché il planning era partito dai
  modelli reali della cooperativa. Anche A25 ha funzionato: ora sono pronti gli Sprint 13 e 14.
- **CI verde al primo tentativo** in tutte le PR dello sprint.
- **Il controllo in Chrome ha trovato due difetti** che i test automatici non vedevano: un testo non collegato al
  suo campo e un comando che portava a una tendina vuota.
- **Merge sempre verificati** (A23, A24): nessuna PR data per unita mentre era ancora aperta.

**Cosa non ha funzionato**
- **L'estensione di Chrome si è scollegata** durante US-206 e US-308: le due PR sono arrivate al merge senza il
  controllo nel browser (punto 3 della Definition of Done). L'ho scritto nelle PR, ma i difetti sono emersi solo dopo
  e sono costati una PR in più (#144) e un secondo deploy.
- **Una risposta in inglese** dopo una compattazione del contesto (la consegna di US-114). Corretto con una regola
  generale, valida in tutte le chat.
- **Due test instabili**: a ogni falso rosso tocca rilanciare e capire se il problema è vero.

**Azioni**

| # | Azione | Chi |
|---|---|---|
| A26 | Se Chrome non è collegato, lo dico **prima** di aprire la PR e ti chiedo di ricollegarlo: il controllo nel browser si fa prima del merge, non dopo | Team (e PO per ricollegare l'estensione) |
| A27 | Rendere stabili i test instabili del frontend (§8.3) | Team |
| A28 | Risposte sempre in italiano, anche dopo una compattazione (regola già attiva) | Team |
| A20 | (confermata) Vedere le modifiche visibili prima di consegnarle | Team |

## 8. Da decidere

### 8.1 Chiusura dello Sprint 12
*Proposta:* Sprint 12 chiuso con velocity **21**.

### 8.2 Quando un tirocinio è «aperto» (US-206)
Al planning: l'ente di presa in carico si blocca con un tirocinio aperto, cioè una segnalazione non ancora in
FORM_RESTITUZIONE. Ho aggiunto una condizione: la segnalazione deve avere già la **scheda del tirocinio**. Così, finché
la segnalazione è in coda o in abbinamento, il Tutor può ancora correggere l'ente; dalla compilazione della scheda alla
restituzione resta bloccato. *Proposta:* confermare.

### 8.3 Test instabili
Due test del frontend falliscono ogni tanto senza un difetto vero (§4). *Proposta:* una piccola attività tecnica
nello Sprint 13 (**1 punto**) per renderli stabili, all'inizio dello sprint. *Alternativa:* lasciarli e rilanciarli
quando capita.

### 8.4 I segnaposto dei modelli caricati (US-1010)
Al caricamento NEXUS rifiuta i modelli con segnaposto che non conosce. I segnaposto nuovi dei documenti reali (dati
dell'ente, convenzione, progetti formativi, date effettive…) arrivano con US-1011, nello Sprint 14. Quindi nello
Sprint 13 la pagina «Modelli dei documenti» accetta i segnaposto di oggi, e i modelli reali di GOL (EN-8) si caricano
nello Sprint 14, dopo US-1011. *Proposta:* confermare questo ordine. *Alternativa:* nello Sprint 13 accettare già
tutti i segnaposto di US-1011, che però resterebbero vuoti nei documenti fino allo Sprint 14.

### 8.5 Cosa fare nello Sprint 13
*Proposta:* quanto deciso al planning del 09/11, cioè US-1007, US-1008, US-1009 e US-1010 (21 punti), più l'attività
del §8.3 se la approvi. Dettaglio al §9.

## 9. Proposta per lo Sprint 13

**Date:** tre giorni da quando confermi il §8.5. **Obiettivo proposto:** *aziende e beneficiari hanno indirizzi con il
comune ufficiale, il Tutor registra la convenzione con uno o più progetti formativi, la fine effettiva del tirocinio e
il ruolo dei tutor aziendali, e l'ADMIN carica i modelli Word con le loro condizioni: dati e modelli per i documenti
reali sono pronti.*

| Ordine | ID | Storia | Punti |
|---|---|---|---|
| 0 | — | Test instabili del frontend (se approvato, §8.3) | 1 |
| 1 | US-1007 | Indirizzi con il comune: sede legale e sede del tirocinio dell'azienda, residenza del beneficiario | 3 |
| 2 | US-1009 | Fine effettiva del tirocinio nel report di restituzione e ruolo del tutor aziendale | 2 |
| 3 | US-1008 | Convenzione e progetti formativi numerati, con date dentro il tirocinio e senza sovrapposizioni | 8 |
| 4 | US-1010 | Pagina «Modelli dei documenti»: caricamento con le condizioni, controllo dei segnaposto, scelta del modello | 8 |
| | | **Totale** | **21** (22 con il §8.3) |

Prima le due storie piccole, che aggiungono dati ai moduli esistenti; poi US-1008, il cambiamento più grande del
modello dei dati; per ultima US-1010, che non dipende dalle altre. Una PR per storia; prima migrazione: **V37**.

Da sapere per il collaudo: con US-1007 indirizzo e comune diventano campi separati. Come dice la storia, gli indirizzi
già inseriti come testo libero si ripassano a mano scegliendo il comune; nella demo i dati di prova si rigenerano.

## 10. Azioni a carico del PO

- [ ] Decidere i punti §8.1 … §8.5.
- [ ] Collaudo dello Sprint 12 sulla demo `ed260ad` (T-22.1 … T-22.4).
- [ ] Quando vuoi, i dati reali degli enti (referenti, legali rappresentanti) dai Cataloghi: servono ai documenti dello
      Sprint 14.
- [ ] T-21.6: lunedì 16/11, le prime PR di Dependabot.
- [ ] US-1301: PWA su Android e iOS (collaudo T-10.1).
- [ ] Backup su IONOS (§8.5 della review dello Sprint 11) quando si riprende US-1302.
