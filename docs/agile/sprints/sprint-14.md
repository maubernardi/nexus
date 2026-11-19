# Sprint 14 — 12 – 14 novembre 2026

**Stato:** 🎉 concluso il 17/11: impegno di 29 punti completato (21 al planning, più US-1013 aggiunta dal PO il 16/11),
demo a `2a123af`; collaudo T-24.1 … T-24.6 da fare, dopo l'analisi del database attuale;
[review](sprint-14-review.md) del 19/11. **Planning:** 11/11, nella
[review dello Sprint 13](sprint-13-review.md) (§8.2, §9); il 15/11 il PO ha proposto delle variazioni funzionali, e due
completano lo sprint (motivo di chiusura e proroga).

## Obiettivo

> Il Tutor indica perché un tirocinio finisce prima del previsto, lo proroga con una nuova convenzione e scarica
> convenzioni, progetti formativi e relazione finale compilati sui modelli reali della cooperativa, senza ritoccarli.

## Impegno (29 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| US-1103 | Motivo di chiusura del tirocinio | 2 | 🎉 Done | `add-closure-reasons` | [#155](https://github.com/maubernardi/nexus/pull/155) |
| US-1012 | Proroga del tirocinio | 8 | 🎉 Done | `add-internship-extensions` | [#157](https://github.com/maubernardi/nexus/pull/157) |
| US-1011 | Documenti sui modelli reali (GOL) | 8 | 🎉 Done | `add-real-documents` | [#158](https://github.com/maubernardi/nexus/pull/158) |
| EN-8 | Preparazione dei modelli reali | 3 | 🎉 Done | `prepare-real-templates` (i modelli restano fuori dal repository) | [#159](https://github.com/maubernardi/nexus/pull/159) |
| US-1013 | Dati del promotore e variabili per progetto | 8 | 🎉 Done | `add-project-promoter-variables` | [#160](https://github.com/maubernardi/nexus/pull/160) |

Ordine: prima il motivo di chiusura e la proroga, perché cambiano i dati della relazione finale e delle convenzioni,
così i segnaposto di US-1011 si scrivono una volta sola; poi US-1011; per ultima EN-8, che usa i segnaposto di US-1011
(review dello Sprint 12, §8.4). I 6 modelli di GOL
(3 per l'Extracurriculare, 3 per l'Inclusione) restano fuori dal repository (`documenti_base/`) e li carica l'ADMIN
dalla pagina «Modelli dei documenti» (US-1010). Nei test si usano modelli finti. Il backup (US-1302) è rinviato (PO,
11/11). Prossima migrazione: **V41**.

Ogni storia con interfaccia nuova si controlla in Chrome **prima** della PR (A26), e un difetto si segnala solo dopo
aver escluso che dipenda dalla simulazione (A29).

**Velocità:** 29 punti in due giorni (16–17/11). Migrazioni V41–V44, tutte solo additive (A16). Un deploy alla fine, su
richiesta del PO, riuscito e controllato online (`2a123af`, 17/11: `/api/v1/me` → 401, bundle nuovo con le pagine
dello sprint). Prossima migrazione: **V45**.

## Decisioni durante lo sprint

- **US-1103**:
  - catalogo dei motivi di chiusura; «Chiusura regolare» è automatica e non si disattiva;
  - il motivo è obbligatorio quando la fine effettiva precede la fine prevista;
  - **PO, 16/11:** con l'interruzione anticipata serve anche una descrizione, obbligatoria. Come testo libero non entra
    nell'audit.
- **US-1012**:
  - la proroga è una convenzione con i suoi progetti formativi; la fine prevista del tirocinio diventa quella
    dell'ultima convenzione;
  - si registra solo a tirocinio avviato; dopo il report di restituzione resta in sola lettura;
  - monte ore e mesi contano solo i giorni coperti da una convenzione.
- **US-1011**:
  - un documento per ogni convenzione (proroghe comprese) e per ogni progetto formativo;
  - relazione finale solo a tirocinio chiuso (`409` prima);
  - 120 segnaposto descritti, in 9 gruppi, nella pagina dei modelli;
  - dal controllo in Chrome: il focus non si perde più dopo il download e i pulsanti non sbordano a 390 px.
- **EN-8**:
  - il motore dei segnaposto cambia solo il testo dei segnaposto: grassetti, tabulazioni, caselle e piè di pagina dei
    modelli reali restano;
  - segnaposto a casella per la tipologia dell'ente, così l'Allegato B vale per tutti e quattro gli enti;
  - i 6 modelli sono in `documenti_base/modelli-nexus/`, con lo script che li genera dai file della cooperativa;
  - **PO, 16/11:** la casella della L. 68/99 resta da spuntare a mano; del bollo resta solo «non è soggetta».
- **US-1013** (**PO, 16/11**):
  - i dati del promotore passano dal `.env` al database, **per progetto**, perché possono cambiare da un progetto
    all'altro; le variabili `NEXUS_PROMOTER_*` non esistono più;
  - per ogni progetto l'ADMIN aggiunge variabili libere, che diventano segnaposto dei modelli di quel progetto;
  - una variabile usata da un modello attivo non si toglie; la riattivazione di un modello ricontrolla i segnaposto;
  - **PO, 18/11:** gli enti di presa in carico restano comuni a tutti i progetti.
- **Controllo in browser reale** (punto 3 della Definition of Done): fatto per tutte le storie con interfaccia, prima
  della PR (A26). Per US-1013 l'estensione di Chrome si bloccava appena si apriva il modulo, probabilmente per
  LastPass: il controllo è stato fatto con Chrome per Windows senza finestra e con un profilo pulito, tema scuro vero
  compreso.

## Dati da preparare

Servono a US-1011 per compilare i documenti senza righe vuote. Li prepara il PO mentre sviluppo; i valori stanno già
nei modelli reali della cooperativa.

### Soggetto promotore: nei Cataloghi, per progetto (US-1013)

Il 09/11 si era deciso di metterli nel `.env` del server. Il 16/11 il PO ha scelto il database, **per progetto**,
perché possono cambiare da un progetto all'altro (US-1013): l'ADMIN li inserisce in «Cataloghi» per GOL, insieme a
eventuali variabili libere. Le variabili `NEXUS_PROMOTER_*` qui sotto non servono più: la tabella resta come elenco
dei dati da raccogliere. Finché un dato manca, il documento lascia la riga vuota da completare a mano.

| Variabile | Contenuto | Esempio di forma |
|---|---|---|
| `NEXUS_PROMOTER_NAME` | Denominazione | `Cooperativa sociale "Il Girasole"` |
| `NEXUS_PROMOTER_ADDRESS` | Sede legale: via, numero civico e comune | `Via …, n. …, Firenze` |
| `NEXUS_PROMOTER_FISCAL_CODE` | Codice fiscale / partita IVA | 11 cifre |
| `NEXUS_PROMOTER_REPRESENTATIVE` | Nome e cognome del legale rappresentante | |
| `NEXUS_PROMOTER_REPRESENTATIVE_ROLE` | Ruolo, come lo scrivono le convenzioni di inclusione | `Rappresentante Legale` |
| `NEXUS_PROMOTER_REPRESENTATIVE_BIRTH_PLACE` | Luogo di nascita del legale rappresentante | |
| `NEXUS_PROMOTER_REPRESENTATIVE_BIRTH_DATE` | Data di nascita del legale rappresentante | `gg/mm/aaaa` |
| `NEXUS_PROMOTER_PHONE` | Telefono | |
| `NEXUS_PROMOTER_EMAIL` | Email | |
| `NEXUS_PROMOTER_PEC` | PEC | |
| `NEXUS_PROMOTER_INAIL_POSITION` | Posizione INAIL (i due modelli la scrivono in modo diverso: va indicata quella giusta) | |
| `NEXUS_PROMOTER_RC_POLICY` | Numero della polizza di responsabilità civile | |
| `NEXUS_PROMOTER_RC_INSURER` | Compagnia assicuratrice | |
| `NEXUS_PROMOTER_RC_EXPIRY` | Scadenza della polizza | `22/07/2027` |

Il tutore del soggetto promotore è il Tutor della segnalazione: non serve una variabile.

### Enti di presa in carico: nei Cataloghi

In «Cataloghi» → «Enti di presa in carico», per ognuno dei quattro enti (Comune di Firenze e tre Società della Salute):
- tipologia;
- codice fiscale;
- sede legale;
- rappresentante con ruolo, luogo e data di nascita;
- referente con telefono e email.

Rappresentante e sede si trovano nelle convenzioni di inclusione della cooperativa.

### Progetto GOL: con US-1011

Due dati per progetto, che l'ADMIN potrà modificare dopo US-1011:
- il **riferimento all'avviso**: proporrò come testo predefinito quello dei modelli («Avviso pubblico n. 10 per
  l'attuazione del Programma Garanzia Occupabilità dei Lavoratori…»);
- la **riga con codice e CUP** (es. «Firenze in Rete 2 – Codice 9014358 – CUP B81D24000440006»).

## Variazioni proposte dal PO (15/11)

| Variazione | Storia | Dove |
|---|---|---|
| Motivo di chiusura: interruzione anticipata per volontà dell'azienda o del destinatario, altrimenti chiusura regolare; catalogo dell'ADMIN | US-1103 | Sprint 14 |
| Proroga: una nuova convenzione con date successive alla precedente, gli altri dati in comune, stessi modelli | US-1012 | Sprint 14 |
| Una mansione per più segnalazioni, al massimo una per tipo di tirocinio; tipo indicato dal Tutor nella segnalazione; avviso ben visibile al Call Center | US-605 | Sprint 15 |
| Richiesta del Tutor al Call Center di contattare un'azienda | US-507 | Da analizzare in una sessione dedicata |

## Dopo questo sprint

| Sprint | Storie | Punti |
|---|---|---|
| 15 | Da definire dopo il confronto con il database attuale (PO, 19/11); in partenza US-605 Una mansione per più segnalazioni, una per tipo di tirocinio | 8 |
| poi | US-507 (dopo l'analisi) · US-1305 Revisione UX/UI · US-1302 Backup (dopo lo Sprint 15) · US-1306 Passaggio in produzione | — |

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (15/11, con le variazioni del PO) | 21 |
| US-1103 unita (16/11) | 19 |
| US-1012 unita (16/11) | 11 |
| US-1011 unita (16/11) | 3 |
| EN-8 unita (16/11) | 0 |
| US-1013 aggiunta dal PO (16/11) | 8 |
| US-1013 unita (17/11) | 0 |

## Per il PO dopo lo sprint

- ~~Dati del promotore di GOL e degli enti nei Cataloghi~~: inseriti il 19/11.
- Caricare i 6 modelli di GOL da «Modelli dei documenti» (T-24.5).
- Collaudo T-24.1 … T-24.6 sulla demo `2a123af`; quello dello Sprint 13 (T-23.1 … T-23.4), se non è già fatto.
- T-21.6: la PR di Dependabot [#156](https://github.com/maubernardi/nexus/pull/156), con il deploy.
