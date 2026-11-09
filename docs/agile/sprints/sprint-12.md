# Sprint 12 — 12 – 14 novembre 2026

**Stato:** 📋 pianificato. **Planning:** 09/11, con il Product Owner, dopo la raccolta dei modelli Word reali della
cooperativa (Q5).

## Obiettivo

> NEXUS conosce comuni e province italiane, le sedi di competenza, i tipi di tirocinio di ogni progetto e gli enti che
> hanno in carico i beneficiari; il Tutor indica la sede di competenza della segnalazione e i contatti, l'ente e il
> case manager del beneficiario. Sono le fondamenta dei documenti reali (Sprint 13 e 14).

## Impegno (21 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| US-114 | Comuni e province, temporizzati, caricati dall'ADMIN dal file ISTAT | 8 | ✅ Ready | — | — |
| US-115 | Sedi di competenza, tipi di tirocinio per progetto, enti di presa in carico | 5 | ✅ Ready | — | — |
| US-206 | Contatti, ente di presa in carico e case manager del beneficiario | 5 | ✅ Ready | — | — |
| US-308 | Sede di competenza della segnalazione | 3 | ✅ Ready | — | — |

Ordine: US-114 → US-115 → US-206 → US-308 (US-206 e US-308 usano i cataloghi di US-115). Una PR per storia; gli stati
del backlog si aggiornano nella PR di documentazione dopo i merge. Prossima migrazione: **V33**.

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
| Inizio (12/11) | 21 |

## Per il PO durante lo sprint

- T-21.6: le PR di Dependabot del lunedì, una alla volta, con il deploy.
- I dati reali degli enti (referenti, legali rappresentanti) si inseriscono dai Cataloghi quando US-115 è online.
