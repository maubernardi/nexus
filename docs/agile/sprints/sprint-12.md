# Sprint 12 — 12 – 14 novembre 2026

**Stato:** 🎉 concluso il 10/11, prima delle date previste: impegno di 21 punti completato, demo a `c8e810d`; collaudo
T-22.1 … T-22.4 da fare. **Planning:** 09/11, con il Product Owner, dopo la raccolta dei modelli Word reali della
cooperativa (Q5).

## Obiettivo

> NEXUS conosce comuni e province italiane, le sedi di competenza, i tipi di tirocinio di ogni progetto e gli enti che
> hanno in carico i beneficiari; il Tutor indica la sede di competenza della segnalazione e i contatti, l'ente e il
> case manager del beneficiario. Sono le fondamenta dei documenti reali (Sprint 13 e 14).

## Impegno (21 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| US-114 | Comuni e province, temporizzati, caricati dall'ADMIN dal file ISTAT | 8 | 🎉 Done | `add-municipalities` | [#139](https://github.com/maubernardi/nexus/pull/139) |
| US-115 | Sedi di competenza, tipi di tirocinio per progetto, enti di presa in carico | 5 | 🎉 Done | `add-document-catalogs` | [#140](https://github.com/maubernardi/nexus/pull/140) |
| US-206 | Contatti, ente di presa in carico e case manager del beneficiario | 5 | 🎉 Done | `add-beneficiary-care` | [#141](https://github.com/maubernardi/nexus/pull/141) |
| US-308 | Sede di competenza della segnalazione | 3 | 🎉 Done | `add-ticket-competence-site` | [#142](https://github.com/maubernardi/nexus/pull/142) |

Ordine: US-114 → US-115 → US-206 → US-308 (US-206 e US-308 usano i cataloghi di US-115). Una PR per storia; gli stati
del backlog si aggiornano nella PR di documentazione dopo i merge. Prossima migrazione: **V33**.

**Velocità:** 21 punti. Migrazioni V33–V36. Un deploy alla fine, su richiesta del PO, riuscito e controllato online:
nuove sezioni nel bundle; nei log del backend le quattro migrazioni applicate e l'elenco ISTAT caricato (7896 comuni,
107 province). Prossima migrazione: **V37**.

## Decisioni durante lo sprint

- **US-114**:
  - il comune è identificato dal codice catastale, che non cambia quando passa a un'altra provincia (il codice ISTAT sì);
  - file ISTAT riconosciuto dai nomi delle colonne (CSV con `;`, Latin-1 o UTF-8); rifiutato se ha meno di 7000
    comuni o una data non successiva all'ultimo caricamento;
  - ricerca dalla seconda lettera, prima i nomi che iniziano con il testo;
  - nella demo l'elenco si carica da solo se le tabelle sono vuote; in produzione lo carica l'ADMIN la prima volta.
- **US-115**: un progetto senza tipi indicati ammette tutti i tipi; la scheda del tirocinio propone i tipi ammessi più
  quello già scelto.
- **US-206**:
  - tirocinio aperto = segnalazione non ancora in FORM_RESTITUZIONE con la scheda del tirocinio;
  - nell'audit solo quali campi cambiano, senza i valori;
  - «Di inclusione» resta visibile ma disattivato, con l'elenco dei dati mancanti.
- **US-308**: la sede si sceglie anche nella candidatura dalla bacheca; con una sola sede nel progetto è già
  selezionata.
- **Controllo in browser reale** (punto 3 della Definition of Done): US-114 e US-115 controllate in Chrome in locale;
  per US-206 e US-308 l'estensione di Chrome era scollegata. Le coprono i test dei componenti con axe; il controllo a
  vista si fa con il collaudo (T-22.3, T-22.4).

## Decisioni del planning (09/11)

Raccolta dei modelli di GOL: convenzione, progetto formativo e relazione finale per l'**Extracurriculare** (sinonimo di
non curricolare) e per l'**Inclusione** (Comune di Firenze e tre Società della Salute).

- **Modelli**: caricati dall'ADMIN da una pagina di NEXUS, con le condizioni (documento, progetto, tipo di tirocinio,
  ente); uno compatibile → si usa, più di uno → sceglie l'utente, nessuno → modello generico di ripiego. Mai nel
  repository: `documenti_base/` è in `.gitignore`.
- **Inclusione**: un solo modello per documento per tutti e quattro gli enti (i testi sono identici); i dati dell'ente
  (tipologia, denominazione, codice fiscale, sede, legale rappresentante, referente) vanno nel catalogo degli enti.
- **Comuni e province** italiani, tutti, temporizzati; li carica l'ADMIN dal file dell'ISTAT, con l'anteprima delle
  differenze (US-114 da 5 a 8 punti); il luogo di nascita resta testo libero.
- **Privacy**: l'ente di presa in carico è un dato delicato come la L. 68/99 (visibilità del beneficiario, mai nei log,
  anonimizzazione con email, telefono e case manager).
- **Sede di competenza** (non «APL»): copre più comuni, vale per uno o più progetti; obbligatoria alla creazione della
  segnalazione, poi bloccata; la correggono ADMIN e Coordinatore.
- **Tipi di tirocinio per progetto**: GOL ammette Extracurriculare e Inclusione; gli altri progetti tutti, fino al
  censimento.
- **Beneficiario**: email e telefono facoltativi; ente di presa in carico bloccato con un tirocinio aperto (= segnalazione
  non ancora in FORM_RESTITUZIONE); per l'Inclusione servono l'ente e nome, mail e telefono del case manager.
- **Convenzione e progetti formativi**: una convenzione per segnalazione, uno o più progetti formativi numerati, con
  date (dentro il tirocinio, senza sovrapposizioni) e mansione propri; l'orario resta quello del tirocinio.
- **Relazione finale**: a tirocinio chiuso; data di fine effettiva nel report di restituzione; mesi arrotondati; luogo =
  sede di competenza, data = chiusura.
- **Dati fissi del Girasole** (legale rappresentante, contatti, INAIL, polizza RC con scadenza 22/07/2027) nel `.env`
  del server; riferimento all'avviso e riga del CUP modificabili dall'ADMIN per ogni progetto.
- **Ordine**: backup e passaggio in produzione (US-1302, US-1306) dopo i documenti reali.

## Dopo questo sprint

| Sprint | Storie | Punti |
|---|---|---|
| 13 | US-1007 Indirizzi con il comune · US-1008 Convenzione e progetti formativi · US-1009 Fine effettiva e ruolo del tutor aziendale · US-1010 Modelli caricati dall'ADMIN | 21 |
| 14 | US-1011 Documenti sui modelli reali · EN-8 Preparazione dei modelli | 11 |
| poi | US-1305 Revisione UX/UI · US-1302 Backup · US-1306 Passaggio in produzione | — |

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (09/11, dopo il planning) | 21 |
| US-114 unita (10/11) | 13 |
| US-115 unita (10/11) | 8 |
| US-206 unita (10/11) | 3 |
| US-308 unita (10/11) | 0 |

## Per il PO durante lo sprint

- Collaudo T-22.1 … T-22.4 sulla demo: è anche il primo controllo a vista di US-206 e US-308.
- I dati reali degli enti (referenti, legali rappresentanti) si inseriscono dai Cataloghi: US-115 è online.
- T-21.6: le PR di Dependabot del lunedì, una alla volta, con il deploy.
