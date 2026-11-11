# Sprint 13 — 11 – 13 novembre 2026

**Stato:** 🎉 concluso l'11/11, prima delle date previste: impegno di 22 punti completato; deploy della demo e
collaudo T-23.1 … T-23.4 su richiesta del PO. **Planning:** 10/11, nella [review dello Sprint 12](sprint-12-review.md)
(§9); proposta del team confermata dal Product Owner, con EN-9 (§8.3).

## Obiettivo

> Aziende e beneficiari hanno indirizzi con il comune ufficiale, il Tutor registra la convenzione con uno o più progetti
> formativi, la fine effettiva del tirocinio e il ruolo dei tutor aziendali, e l'ADMIN carica i modelli Word con le loro
> condizioni: dati e modelli per i documenti reali sono pronti.

## Impegno (22 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| EN-9 | Test del frontend stabili | 1 | 🎉 Done | — (solo test, nessun comportamento nuovo) | [#146](https://github.com/maubernardi/nexus/pull/146) |
| US-1007 | Indirizzi con il comune | 3 | 🎉 Done | `add-municipality-addresses` | [#147](https://github.com/maubernardi/nexus/pull/147) |
| US-1009 | Fine effettiva del tirocinio e ruolo del tutor aziendale | 2 | 🎉 Done | `add-internship-actual-end` | [#148](https://github.com/maubernardi/nexus/pull/148) |
| US-1008 | Convenzione e progetti formativi | 8 | 🎉 Done | `add-training-projects`, `require-agreement-for-start` | [#149](https://github.com/maubernardi/nexus/pull/149), [#150](https://github.com/maubernardi/nexus/pull/150) |
| US-1010 | Modelli dei documenti caricati dall'ADMIN | 8 | 🎉 Done | `add-document-templates` | [#151](https://github.com/maubernardi/nexus/pull/151) |

Ordine: EN-9 → US-1007 → US-1009 → US-1008 → US-1010. Prima i test stabili, così le verifiche delle storie non hanno
falsi rossi; poi le due storie piccole, che aggiungono dati ai moduli esistenti; poi US-1008, il cambiamento più grande
del modello dei dati; per ultima US-1010, che non dipende dalle altre. Una PR per storia; i nomi delle change sono
indicativi; gli stati del backlog si aggiornano nella PR di documentazione dopo i merge. Prossima migrazione: **V37**.

Ogni storia con interfaccia nuova si controlla in Chrome **prima** del merge (A26): se l'estensione è scollegata, lo
segnalo prima di aprire la PR.

**Velocità:** 22 punti in un giorno. Migrazioni V37–V40, tutte solo additive (A16). Nessun deploy durante lo sprint:
la demo è ancora a `ed260ad`. Prossima migrazione: **V41**.

## Decisioni durante lo sprint

- **EN-9**: la causa era il focus spostato con `requestAnimationFrame`, che nei test finiva dopo le verifiche. Ora c'è
  `useFocusAfterRender` (effetto dopo il render) e il lint vieta `requestAnimationFrame`. Niente timeout più lunghi
  né ripetizioni.
- **US-1007**:
  - si salva la versione del comune, così un cambio di nome o di provincia non tocca i dati già inseriti;
  - la residenza è comune, via e numero civico: la via usa la colonna di prima (migrazione solo additiva);
  - la casella con suggerimenti segue il pattern combobox delle WAI-ARIA APG. Dal controllo in Chrome è venuto
    l'avviso «Solo i primi 20 comuni…»;
  - nella demo le aziende dimostrative hanno il comune.
- **US-1009**:
  - la fine effettiva sta nel report di restituzione, non nella scheda: vale solo per la relazione finale;
  - nel database è facoltativa, per i report già inviati e per A16; non può precedere l'inizio e non ha limiti verso
    il futuro (proroghe, report inviati nell'ultima settimana);
  - il ruolo del tutor aziendale entra nella riga dei tutor dei documenti di oggi.
- **US-1008**:
  - la convenzione sta nella scheda;
  - i progetti formativi si salvano con la scheda, come le sospensioni, e il numero è la posizione nell'ordine delle
    date (togliere un progetto rinumera i successivi);
  - mesi arrotondati con la regola di US-1011 (15 giorni o più = un mese);
  - il dettaglio della mansione va nell'audit solo come «cambiato».
  - **PO, 11/11:** per avviare il tirocinio servono la convenzione completa e almeno un progetto formativo
    ([#150](https://github.com/maubernardi/nexus/pull/150)).
- **US-1010**:
  - le condizioni di un modello non si modificano: si disattiva il modello e se ne carica un altro;
  - sostituire il file cancella quello vecchio;
  - la relazione finale si carica già, ma si scarica con US-1011;
  - la cronologia registra il modello usato.
- **Controllo in browser reale** (punto 3 della Definition of Done): fatto in Chrome in locale per tutte le storie con
  interfaccia, prima della PR (A26). L'icona del calendario poco visibile nel tema scuro, segnalata durante US-1009, era
  un falso positivo della simulazione del tema scuro: `index.html` dichiara già `color-scheme: light dark`. Nessuna
  correzione, anche se il PO aveva chiesto una PR a parte.

## Decisioni del planning (10/11)

- **Tirocinio aperto** (US-206, confermato): segnalazione con la scheda del tirocinio e non ancora in
  FORM_RESTITUZIONE.
- **Segnaposto noti** (US-1010): quelli che NEXUS sa compilare oggi; i nuovi arrivano con US-1011 nello Sprint 14,
  quindi i modelli reali di GOL (EN-8) si caricano nello Sprint 14. Nei test si usano modelli finti.
- **Test instabili** (EN-9): la causa si corregge, non si nasconde con timeout più lunghi o ripetizioni.

## Dopo questo sprint

| Sprint | Storie | Punti |
|---|---|---|
| 14 | US-1011 Documenti sui modelli reali · EN-8 Preparazione dei modelli | 11 |
| poi | US-1305 Revisione UX/UI · US-1302 Backup · US-1306 Passaggio in produzione | — |

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (10/11, dopo il planning) | 22 |
| EN-9 unita (11/11) | 21 |
| US-1007 unita (11/11) | 18 |
| US-1009 unita (11/11) | 16 |
| US-1008 unita (11/11, con #150) | 8 |
| US-1010 unita (11/11) | 0 |

## Per il PO dopo lo sprint

- Deploy dello Sprint 13 quando vuoi, poi il collaudo T-23.1 … T-23.4.
- Collaudo dello Sprint 12 sulla demo `ed260ad` (T-22.1 … T-22.4), se non è già fatto.
- T-21.6: lunedì 16/11, le prime PR di Dependabot, una alla volta, con il deploy.
