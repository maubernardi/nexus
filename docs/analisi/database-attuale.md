# Il database attuale (Access) confrontato con NEXUS

**Data:** 19/11/2026 · **Richiesta del PO:** verificare se NEXUS copre i dati usati oggi e se si possono importare
per il passaggio in produzione (review dello Sprint 14, §8.2) · **Fonte:** `documenti_base/nexus.mdb`, fuori dal
repository (Access 2000–2003, 124 MB, dati fino al 09/09/2026).

> Il database contiene dati personali (referenti delle aziende, operatori, note libere). L'analisi è stata fatta in
> locale e qui ci sono solo struttura, conteggi e nomi di comuni, categorie e progetti: nessun dato di persone.

## 1. In sintesi

1. **È il registro delle aziende e delle telefonate del Call Center**, non dei beneficiari. Non ci sono beneficiari,
   segnalazioni, schede del tirocinio né documenti: per questi NEXUS parte da zero, o da un'altra fonte (§7, domanda
   1).
2. **Copertura:** NEXUS ha l'anagrafica di base dell'azienda e le sue note, ma non quello che oggi si usa di più:
   - lo **storico delle chiamate**, con esito, data in cui richiamare e progetto;
   - la **disponibilità dell'azienda** per tirocinio, stage, IST, borsa lavoro, assunzione, Giovanisì;
   - la **categoria merceologica** e la descrizione dell'attività;
   - un secondo referente, il cellulare, il fax, una seconda email, la sede operativa.
3. **Importazione:** possibile, con trasformazioni, ma con un **ostacolo bloccante**. In NEXUS la partita IVA è
   obbligatoria e unica, mentre il database attuale non ce l'ha: nessun campo, e un numero di 11 cifre compare solo in
   poche note.

## 2. Cosa c'è nel database attuale

| Tabella | Righe | Che cos'è | Corrisponde in NEXUS a |
|---|---|---|---|
| Contatti | 13.799 | Aziende: nome, referenti con ruolo, indirizzo, comune, telefoni, email, note, categoria | Azienda (in parte) |
| Chiamate | 38.789 | Telefonate alle aziende: data, oggetto, esito, data per richiamare, note, disponibilità, progetto, operatore | — (solo il diario delle note, US-505) |
| Profili | 163 | Profili professionali richiesti dalle aziende, con requisiti (lingue, patente, informatica, orario, contratto) | Mansione (in parte) |
| Candidature | 9 | Candidature ai profili | Proposta (praticamente inutilizzata) |
| Assistenti | 270 | Operatori: personale interno e assistenti sociali degli enti, con ente e area di intervento | Utenti e case manager (non direttamente) |
| data base aziende perla | 72 | Elenco di aziende di un progetto del 2017 | — |
| Progetti | 27 | Progetti (tra cui POLIS, GOL, REI, PON…) | Progetti |
| Nuove categorie / Merciologico / Categoria | 55 / 113 / 3 | Categorie merceologiche delle aziende | — (NEXUS ha l'ATECO per azienda e le tipologie di mansione) |
| Esiti, Esiti_2 | 6 + 10 | Esiti delle chiamate e delle candidature | — |
| Tipi di contatti | 19 | Ruoli dei referenti (titolare, responsabile del personale…) | — |
| Enti, Area Intervento | 7 + 16 | Enti e aree degli assistenti sociali | Enti di presa in carico (in parte) |
| Contratti, Lingue, Lingue_2 | 10 + 7 + 6 | Requisiti dei profili | — |
| nazionalità, Grande Area, Provincie | 218 + 14 + 111 | Elenchi | Paesi ISO, comuni e province ISTAT |
| Viario codici CAP | 2.694 | Vie di Firenze con CAP e quartiere | — (utile per ricavare la zona) |
| Offerte, Errori…, Switchboard Items | — | Vuote o tecniche (menu dell'applicazione) | — |

### Attività negli anni

| | 2015 | 2016 | 2017 | 2018 | 2019 | 2020 | 2021 | 2022 | 2023 | 2024 | 2025 | 2026 (al 9/9) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Chiamate | 2.818 | 1.403 | 2.881 | 4.078 | 6.550 | 6.199 | 4.658 | 3.406 | 1.597 | 1.797 | 1.374 | 933 |
| Aziende nuove | 1.961 | 1.824 | 891 | 1.339 | 2.875 | 1.819 | 999 | 610 | 274 | 315 | 264 | 136 |

Aziende con il nome, per anno dell'ultima chiamata: **9.255** dal 2019, **5.694** dal 2021, **2.497** dal 2023,
**1.200** dal 2025.

## 3. Azienda: campo per campo

| Database attuale | Compilato | NEXUS | Note |
|---|---|---|---|
| NomeSocietà | 88% | Ragione sociale (obbligatoria) | 1.587 aziende senza nome, 453 con chiamate |
| — | — | **Partita IVA (obbligatoria, unica, 11 cifre)** | **Non esiste**: bloccante (§6.1) |
| Indirizzo_1 | 98% | Indirizzo della sede legale | Testo libero; in 5.856 casi contiene anche il CAP |
| Sede Legale | 93% | Comune della sede legale (elenco ISTAT) | 79% si abbina subito all'elenco ISTAT; ~90% con una tabella di alias (§4) |
| Sede Locale | 16% | — | Sede operativa: NEXUS non la ha per l'azienda |
| Zona | 0% | Zona (della mansione) | Mai usata: si può ricavare dal comune o, a Firenze, dal viario |
| Nominativo_1 + ruolo | 36% | Referente | NEXUS ha un solo referente, senza ruolo |
| Nominativo_2 + ruolo | 12% | — | Secondo referente |
| TelefonoUfficio | 97% | Telefono | 1.089 numeri uguali su più aziende (2.367 righe): sedi della stessa azienda o duplicati |
| TelefonoCellulare, NumeroFax | 24%, 4% | — | |
| IndirizzoPostaElettronica_1 | 43% | Email | Salvate come collegamenti di Access («…#mailto:…#»): vanno ripulite |
| IndirizzoPostaElettronica_2 | 9% | — | |
| Note | 44% | Diario delle note dell'azienda (US-505) | Testo libero |
| Paese | 92% | — | In realtà è la **descrizione dell'attività** (HOTEL, RISTORANTE, PELLETTERIA…) |
| Id_nuova_cat | 97% | — | **Categoria merceologica** (55 voci: Albergo, Ristorante, Pulizie…) |
| data registrazione, operatore | 96%, 99% | Dati di creazione (audit) | |
| Cottattata | 32 sì | — | Praticamente inutilizzato |

## 4. Qualità dei dati

- **Comuni della sede legale:** 10.951 abbinati direttamente all'elenco ISTAT, 489 con la sola sigla della provincia
  (es. «FI»), 834 vuoti, 1.525 da sistemare. I casi più frequenti si risolvono con una tabella di alias:
  - abbreviazioni: «S FIORENTINO», «C BISENZIO»;
  - frazioni e quartieri: Osmannoro, Grassina, Isolotto;
  - comuni fusi dopo il 2014: Figline Valdarno, Scarperia, San Piero a Sieve;
  - nomi abbreviati: «San Casciano».
- **Duplicati:** 66 gruppi di aziende con lo stesso nome normalizzato (137 righe) e 1.089 telefoni condivisi.
- **Date anomale nelle chiamate:** alcune nel 1931 e nel 1971, altre nel 2030.
- **Collegamenti rotti:** 3.665 chiamate riferite ad aziende che non esistono più e 441 senza azienda.
- **Esiti:** il 60% delle chiamate ha «Richiamare» e 15.648 non sono «evase». Nessuna richiamata ha una data
  successiva all'ultima chiamata registrata.
- **Profili:** 163, compilati a metà (titolo nel 61%, compiti nel 47%), senza tipologia di mansione né zona. In
  NEXUS sono entrambe obbligatorie.

## 5. Cosa manca in NEXUS

Sono le funzioni che il database attuale sostiene e NEXUS no. Ognuna può diventare una storia, se serve davvero:

| # | Cosa | Peso nei dati | Nota |
|---|---|---|---|
| G1 | **Storico delle chiamate all'azienda**: data, oggetto, esito, data per richiamare, progetto, operatore | 38.789 chiamate, l'attività principale del Call Center | Oggi in NEXUS c'è solo il diario delle note (US-505) |
| G2 | **Richiamate da fare**: elenco delle aziende da richiamare, per data | 60% delle chiamate con esito «Richiamare» | Promemoria per l'operatore |
| G3 | **Disponibilità dell'azienda** per tipo di inserimento: tirocinio, stage, IST, borsa lavoro, assunzione, Giovanisì | 20.513 chiamate con «tirocinio», 6.544 con «stage» | In NEXUS i tipi di tirocinio esistono (US-108), ma non la disponibilità dell'azienda |
| G4 | **Categoria merceologica** e descrizione dell'attività, per cercare le aziende | 97% delle aziende | NEXUS ha l'ATECO, che però si inserisce nella scheda del tirocinio |
| G5 | **Più referenti con il ruolo**, cellulare, fax, seconda email, sede operativa | 12–36% | |
| G6 | **Requisiti delle mansioni** (lingue, patente, informatica, orario, contratto) | 163 profili, quasi tutti vecchi | Basso: si può rinviare |

## 6. Importazione: cosa serve decidere

### 6.1 Partita IVA (bloccante)
NEXUS vuole una partita IVA di 11 cifre, unica, per ogni azienda (US-501). Strade possibili:
- **a)** per le aziende importate la partita IVA diventa facoltativa e resta «da completare», obbligatoria solo quando
  serve, per esempio per la convenzione;
- **b)** si importano solo le aziende a cui qualcuno aggiunge la partita IVA prima del passaggio;
- **c)** si cerca la partita IVA nel registro delle imprese, con un servizio a pagamento o a mano.

### 6.2 Quali aziende
Tutte (13.799), solo quelle con il nome (12.212), oppure solo quelle vive: per esempio con l'ultima chiamata dal 2021
(5.694) o dal 2023 (2.497).

### 6.3 Chiamate
- **a)** come note del diario dell'azienda (US-505), una per chiamata, con data, esito e testo: niente funzioni nuove;
- **b)** come storico strutturato, se si fa G1, così si possono filtrare e contare.

### 6.4 Il resto
- **Profili e candidature:** non importarli, perché sono pochi e vecchi.
- **Operatori e assistenti sociali:** non diventano utenti di NEXUS, che passano da Keycloak. Al massimo una rubrica
  per i case manager.
- **Progetti:** solo quelli attivi, creati dall'ADMIN.
- **Duplicati e comuni:** si sistemano durante l'importazione, con un elenco dei casi da rivedere a mano.

## 7. Domande per il PO

1. I **beneficiari** di oggi dove sono (fogli Excel, un altro database)? Vanno portati in NEXUS?
2. **Partita IVA** (§6.1): quale strada?
3. **Quali aziende** importare (§6.2)?
4. Le **chiamate** (§6.3): note o storico strutturato? E delle funzioni mancanti (§5, G1–G6) quali servono prima del
   passaggio in produzione?
5. Il Call Center continuerà a fare **prospezione telefonica** delle aziende in NEXUS, come oggi? Da questo dipende il
   peso di G1–G3.
