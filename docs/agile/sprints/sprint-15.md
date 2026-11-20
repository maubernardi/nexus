# Sprint 15 — 19 – 21 novembre 2026

**Stato:** 📋 pianificato, 27 punti. **Planning:** 19/11, sull'[analisi del database attuale](../../analisi/database-attuale.md)
(§7, §8) chiesta nella [review dello Sprint 14](sprint-14-review.md) (§8.2). Il PO ha scelto di mettere tutto nello
sprint, anche sopra la velocity media (22,9).

## Obiettivo

> Le aziende del database attuale possono entrare in NEXUS con tutti i loro dati: categoria, referenti, recapiti e
> chiamate in archivio, con un'importazione provata e i dati da completare riconoscibili. Il Call Center propone la
> stessa mansione a più segnalazioni, una per tipo di tirocinio.

## Impegno (27 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| US-508 | Partita IVA e nome da completare | 3 | ✅ Ready | `mark-companies-to-complete` | [#165](https://github.com/maubernardi/nexus/pull/165) |
| US-509 | Categoria merceologica e attività dell'azienda | 3 | ✅ Ready | `add-business-categories` | [#166](https://github.com/maubernardi/nexus/pull/166) |
| US-510 | Referenti e recapiti dell'azienda | 5 | ✅ Ready | `add-company-contacts` | [#167](https://github.com/maubernardi/nexus/pull/167) |
| US-1309 | Importazione dal database attuale | 8 | ✅ Ready | `add-legacy-import` | [#168](https://github.com/maubernardi/nexus/pull/168) |
| US-605 | Una mansione per più segnalazioni, una per tipo di tirocinio | 8 | ✅ Ready | `add-slot-per-internship-type` | |

Ordine: prima le tre storie che danno all'azienda i campi del database attuale, poi l'importazione, che li usa; per
ultima US-605, indipendente. Prossima migrazione: **V45**.

Ogni storia con interfaccia nuova si controlla in Chrome **prima** della PR (A26). Se l'estensione si blocca, il
controllo si fa con Chrome senza finestra e un profilo pulito (A34). Un difetto si segnala solo dopo aver escluso che
dipenda dalla simulazione (A29).

## Decisioni del planning (19/11)

- **Database attuale** ([analisi](../../analisi/database-attuale.md)):
  - è il registro delle aziende e delle chiamate del Call Center; i beneficiari sono «immersi» nelle chiamate, non c'è
    un'anagrafica;
  - NEXUS lo sostituisce in tutto: categoria merceologica, referenti e recapiti si aggiungono a NEXUS.
- **Partita IVA mancante**: «00000000000», che il programma non lascia salvare; l'unicità nel database esclude quel
  valore.
- **Nome mancante**: «Azienda senza nome n. ‹numero di Access›», con lo stesso vincolo.
- **Aziende**: si importano tutte.
- **Chiamate**: in una tabella di archivio, non visibile nell'interfaccia. I beneficiari sono indicati con le
  iniziali; 7 note da controllare prima dell'importazione.
- **Importazione**: provata su una copia in locale. Sulla demo restano i dati dimostrativi; l'importazione vera si fa
  con il passaggio in produzione (US-1306).
- **Backup** (US-1302): resta nel backlog fino a dopo questo sprint.
- **Collaudo** (T-22 … T-24): dopo l'analisi del database attuale, cioè da ora.

## Decisioni durante lo sprint

- **US-508**:
  - V45: l'unicità della partita IVA diventa un indice univoco parziale con lo stesso nome (`uq_company_vat_code`),
    che esclude «00000000000»;
  - il modulo rifiuta «00000000000» e ogni nome che comincia con «Azienda senza nome n.», anche con maiuscole e spazi
    diversi;
  - nella scheda i campi con il segnaposto si aprono vuoti, con la spiegazione;
  - anche il nome segnaposto, nei documenti, diventa la riga da completare: in una convenzione sarebbe sbagliato
    quanto gli zeri;
  - nella demo due aziende dimostrative da completare, per il collaudo (T-25.1).
- **US-509**:
  - catalogo «Categorie merceologiche» con le 53 categorie del database attuale: le 55 righe di Access meno le due
    vuote, mai usate. Corretti un refuso («edelizia») e le spaziature di «Telecomunicazioni, informatica, cinema e TV»;
    l'ADMIN può rinominarle;
  - l'id delle voci iniziali è fisso (893700000000000400 + il numero in Access): l'importazione (US-1309) ritrova la
    categoria anche dopo una rinomina;
  - la categoria si filtra dal menu dell'elenco, che si applica alla scelta; la ricerca resta per ragione sociale e
    partita IVA;
  - categoria e attività anche nel dettaglio dell'abbinamento.
- **US-510**:
  - catalogo «Ruoli dei referenti» con i 12 ruoli del database attuale, ripuliti: fuori le righe vuote, quelle che non
    sono ruoli («pelle», «DI», «ele») e il doppione di «Legale Rappresentante». L'id delle voci iniziali è fisso
    (893700000000000500 + il numero in Access), come per le categorie;
  - fino a 10 referenti per azienda, in ordine; di ognuno serve almeno il nome o il ruolo, perché nel database attuale
    1.129 referenti hanno il solo ruolo;
  - il referente di oggi diventa il primo, senza ruolo; la colonna `contact_person` resta per il ripristino della
    release precedente (A16) e si toglierà dopo il passaggio in produzione;
  - referenti, cellulare, fax e seconda email nell'audit solo come «cambiati»; la sede operativa con i valori;
  - nell'elenco la colonna «Referente» mostra il primo referente, con quanti sono gli altri.
- **US-1309**:
  - l'importazione è un'istanza del backend con il profilo `legacy-import`, che legge il file e si chiude, senza
    endpoint: il file non passa da HTTP;
  - il file attuale è «codificato» da Access: si legge con Jackcess e jackcess-encrypt, senza mdbtools sul server;
  - id fissi ricavati dai numeri di Access e una transazione sola: la seconda esecuzione non cambia nulla;
  - i comuni si abbinano con regole e alias (92% delle sedi legali nella prova); i recapiti che non stanno nei campi
    vanno in una nota del diario;
  - il resoconto ha solo numeri, conteggi e testi dei comuni, con le cifre lunghe nascoste;
  - la prova sul file vero ha trovato due difetti prima della PR: le email di Access sono collegamenti `http://…`, e
    alcune parole iniziali abbinavano comuni fuori Toscana («SESTO» → Sesto, BZ).
- **US-605**:
  - il tipo di tirocinio passa sulla segnalazione: lo indica il Tutor alla creazione (anche dalla bacheca) e lo
    corregge fino all'avvio, dalla segnalazione o dalla scheda, che ha sempre lo stesso tipo;
  - l'assegnazione della mansione sta sulla segnalazione (`job_slot_held`), con un indice univoco per mansione e
    tipo; lo stato BLOCCATA della mansione resta come riassunto, per la release precedente (A16);
  - le segnalazioni aperte prendono il tipo dalla scheda; senza scheda il Call Center vede «tipo non indicato» e non
    propone finché il Tutor non lo indica;
  - con il report e la mansione non più disponibile l'assegnazione resta per quel tipo, come prima il blocco;
  - nella demo la segnalazione n. 3 è Curriculare, per vedere l'avviso sulla mansione della n. 4.

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (19/11, dopo il planning) | 27 |
| US-508 unita (19/11) | 24 |
| US-509 unita (19/11) | 21 |
| US-510 unita (19/11) | 16 |
| US-1309 unita (20/11) | 8 |

## Per il PO durante lo sprint

- Unire questa PR con il planning, poi le PR delle storie.
- Controllare in Access le 7 chiamate con nome e cognome: n. 11719, 26711, 28531, 31037, 34116, 36661, 40614.
- Caricare i 6 modelli di GOL (T-24.5) e il collaudo T-22 … T-24.
- Chiudere la PR di Dependabot #156, se Dependabot non la chiude da solo.
