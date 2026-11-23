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

## 7. Decisioni del PO (19/11)

| # | Domanda | Decisione |
|---|---|---|
| 1 | Dove sono i beneficiari? | Non c'è un'anagrafica: sono «immersi» nelle segnalazioni, cioè nelle chiamate. Non si importano come beneficiari. |
| 2 | Partita IVA | Quando manca si importa **«00000000000»**. Il programma non lascia salvare un'azienda con quel valore, così la si corregge alla prima modifica. Il vincolo di unicità del database esclude quel valore. |
| 3 | Quali aziende | **Tutte.** Le 1.587 senza nome sono quasi tutte vecchie (1.261 registrate fino al 2019; solo 7 chiamate dal 2023), ma hanno telefono, indirizzo e attività: nome «Azienda senza nome n. ‹numero di Access›», con lo stesso vincolo della partita IVA. |
| 4 | Chiamate | Corrispondono alle segnalazioni: si importano in una **tabella di archivio**, per averle se serviranno. |
| 4b | Dati personali nelle chiamate | Per regola interna i beneficiari sono indicati con le **iniziali**. Verifica sulle note: le iniziali compaiono in circa 15.500 chiamate; nome e cognome dopo «utente», «candidato» o «ragazzo» solo in **7**, da controllare a mano (n. 11719, 26711, 28531, 31037, 34116, 36661, 40614). Controllate dal PO il 23/11 in Access, con le 20 segnalate dalla prova generale: nessun nome di beneficiario. Circa 3.000 note citano «sig./sig.ra» e un cognome, e circa 900 un'email o un cellulare: dal contesto sembrano i referenti delle aziende. |
| 5 | Prospezione in NEXUS | Sì: NEXUS sostituisce il database attuale in tutto. |
| 5b | Dati senza un campo in NEXUS (G4, G5) | Si **aggiungono a NEXUS**: categoria merceologica e attività, più referenti con il ruolo, cellulare, fax, seconda email. |

Lo storico delle chiamate e le richiamate (G1, G2) corrispondono al flusso delle segnalazioni di NEXUS. I requisiti
dei profili (G6) restano fuori.

## 8. Proposta per lo Sprint 15

| Ordine | Storia (proposta) | Punti | Contenuto |
|---|---|---|---|
| 1 | Partita IVA e nome da completare | 3 | «00000000000» e «Azienda senza nome n. …» ammessi solo dall'importazione; il programma non li lascia salvare; «Da completare» nell'elenco e nella scheda, con un filtro; nei documenti la riga da completare. La migrazione toglie quei valori dall'unicità. |
| 2 | Categoria merceologica e attività dell'azienda | 3 | Catalogo dell'ADMIN con le 55 categorie di oggi; categoria e descrizione dell'attività nella scheda dell'azienda, con il filtro nell'elenco. |
| 3 | Referenti e recapiti dell'azienda | 5 | Più referenti con nome, ruolo (catalogo dai «Tipi di contatti» di oggi, ripulito), telefono, cellulare ed email; fax e seconda email dell'azienda; sede operativa con il comune. |
| 4 | Importazione dal database attuale | 8 | Lettura del file Access sul server, senza dati nel repository: tutte le aziende con comune (con gli alias), email ripulite, note nel diario con la data originale, referenti e categoria; chiamate nella tabella di archivio. Alla fine un resoconto con le righe da rivedere: duplicati, comuni non trovati, aziende senza nome. Si prova su una copia e si esegue con il passaggio in produzione (US-1306). |
| — | US-605 Una mansione per più segnalazioni | 8 | Pianificata il 15/11 |

Le prime quattro valgono **19 punti** e preparano il passaggio in produzione; con US-605 si arriva a 27, sopra la
velocity media (22,9). *Proposta:* Sprint 15 = le prime quattro, US-605 nello Sprint 16.

> ✅ **Deciso (PO, 19/11):** tutto nello Sprint 15, anche US-605, anche se si va sopra la velocity (27 punti). Aziende
> senza nome come «Azienda senza nome n. ‹numero di Access›», con lo stesso vincolo della partita IVA. Importazione
> provata su una copia in locale ed eseguita con il passaggio in produzione (US-1306). Storie: US-508, US-509, US-510,
> US-1309; [sprint-15.md](../agile/sprints/sprint-15.md).

## 9. Importazione (US-1309)

L'importazione legge il file Access in sola lettura, scrive in una transazione e chiude l'applicazione: 0 se è
riuscita, 1 se no. Il file e il resoconto restano fuori dal repository.

**Che cosa importa** (dettagli nella change `add-legacy-import`):
- tutte le aziende:
  - nome, oppure «Azienda senza nome n. …», e la partita IVA da completare;
  - sedi con il comune abbinato all'elenco ufficiale;
  - recapiti ripuliti, referenti con il ruolo, categoria, attività e la nota nel diario.
- tutte le chiamate, nell'archivio `legacy_call`.

Una seconda esecuzione non duplica e non tocca le aziende che ci sono già.

**Prima di partire:**
- l'elenco dei comuni deve essere caricato (Cataloghi → Comuni, US-114);
- il PO controlla in Access le 7 chiamate con nome e cognome (§7, 4b): fatto il 23/11, con le 20 della prova
  generale, senza nomi di beneficiari.

### 9.1 Prova in locale, su una copia

```bash
docker run -d --rm --name nexus-prova-pg -e POSTGRES_USER=nexus -e POSTGRES_PASSWORD=nexus -e POSTGRES_DB=nexus \
  -p 55432:5432 postgres:19.0-alpine
cd backend
DB_URL=jdbc:postgresql://localhost:55432/nexus PROFILES_ACTIVE=postgresql,security-mock,seed,legacy-import \
  NEXUS_LEGACY_IMPORT_FILE="$PWD/../documenti_base/nexus.mdb" ./mvnw -q spring-boot:run
```

- Il profilo `seed` carica l'elenco dei comuni della demo.
- Il resoconto va accanto al file: `documenti_base/nexus.mdb-resoconto.txt`, ignorato da git.

### 9.2 Sul server, con il passaggio in produzione (US-1306)

Lo fa lo script del passaggio (`infra/deploy/passaggio-in-produzione.sh`, passo 6; README, «Passaggio in produzione»),
subito dopo la pulizia della demo. Il comando è questo; provato in locale il 21/11 su una demo pulita, con lo stesso
esito del §9.3.

```bash
sudo install -d -o "$USER" -m 700 /opt/nexus/import   # poi copiare il file con scp in /opt/nexus/import/nexus.mdb
cd /opt/nexus/current
docker compose --project-directory . -f compose.yaml --env-file /opt/nexus/.env --env-file release.env \
  run --rm --user "$(id -u):$(id -g)" -v /opt/nexus/import:/import \
  -e PROFILES_ACTIVE=keycloak,legacy-import -e NEXUS_LEGACY_IMPORT_FILE=/import/nexus.mdb backend
```

- L'istanza dell'importazione non ha attività pianificate e usa una porta libera: può girare accanto a quella in
  servizio.
- Il resoconto è in `/opt/nexus/import/nexus.mdb-resoconto.txt`.
- Dopo il controllo, il file Access si cancella dal server: contiene dati personali.

### 9.3 Prova del 19/11 sul file del PO

Il file ha dati fino al 09/09/2026; l'importazione ha impiegato circa 11 secondi. Solo conteggi:

| | Risultato |
|---|---|
| Aziende | 13.799, di cui 1.587 senza nome |
| Comune della sede legale | 11.986 abbinati su 12.965 compilati (92%): nome esatto 9.764, nome e sigla 1.187, alias 800, parole iniziali 218, nome tagliato 17 |
| Comune della sede operativa | 2.154 abbinati su 2.281 |
| Comuni non trovati | 1.106 righe in 568 testi; il più frequente è la sola sigla «FI» (428) |
| Categoria, attività | 13.460, 12.799 |
| Referenti | 7.786; 16 con un ruolo sconosciuto, entrati senza ruolo |
| Recapiti | telefono 13.344, cellulare 3.311, fax 644, email 5.934, seconda email 1.116 |
| Recapiti fuori formato | 838 aziende, con il testo originale in una nota del diario |
| Note nel diario | 6.142 di Access più le 838 dei recapiti |
| Chiamate | 38.789 nell'archivio, 34.683 collegate a un'azienda |
| Da rivedere | 23 gruppi di duplicati per nome; 20 chiamate con possibili nomi di beneficiari |

Le email di Access sono quasi tutte collegamenti `http://indirizzo`: l'importazione li riduce all'indirizzo.
