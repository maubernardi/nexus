# Sprint 7 — Review, retrospettiva e planning

**Sprint:** 29 – 31 ottobre 2026 (planning 28/10, lavoro iniziato la sera del 28/10, chiuso il 29/10) · **Review:**
29/10/2026 · **Partecipanti:** Mauro Bernardi (Product Owner), team di sviluppo (Claude Code) · **Demo:**
https://portalenexus.it, versione `db3fdaa` (tutto lo Sprint 7 e il tema di accesso) · **Collaudo:**
[docs/test/collaudo.md](../../test/collaudo.md), sezioni 12–15.

> Documento unico e asincrono: review (§1–§6), retrospettiva (§7), decisioni da prendere (§8) e proposta di planning
> dello Sprint 8 (§9). La PR resta in **bozza** finché non abbiamo finito di discuterne (azione A12): le decisioni le
> aggiungo qui, poi la segno come pronta per il merge.

---

## 1. Sintesi

| | |
|---|---|
| **Obiettivo dello sprint** | Si chiude R2: il Tutor candida un beneficiario a un annuncio e il Call Center approva o rifiuta la corsia veloce; l'ADMIN gestisce zone e tipologie; inizia la scheda del tirocinio. |
| **Esito** | ✅ **Raggiunto e superato.** R2 è completa. La scheda del tirocinio, ampliata durante lo sprint (dati dell'azienda, tipi di tirocinio, sospensioni), è fatta insieme alla griglia oraria e al monte ore. |
| **Impegno / completato** | 26 punti (19 al planning + 7 dell'ampliamento del 29/10) / **26 punti** (7 storie su 7) |
| **Extra** | US-1002 *Griglia oraria e monte ore* (5); US-1003 *Avvio del tirocinio* non iniziata, torna al backlog |
| **Fuori piano** | US-1304 *Tema di Keycloak* (3), anticipata da R4 su tua richiesta (§8.1) |
| **Velocity** | Sprint 1: 21 · 2: 25 · 3: 21 · 4: 24 · 5: 21 · 6: 20 · 7: **31** · media 23,3 |
| **PR unite** | 13 (#73–#85): 9 di funzionalità, 1 correzione, 3 di documentazione |
| **Migrazioni del database** | 6: V19 (una candidatura per annuncio), V20 (nomi dei cataloghi), V21 (tipi di tirocinio), V22 (legale rappresentante e ATECO), V23 (scheda del tirocinio), V24 (griglia oraria) |
| **Deploy della demo** | 29/10: `db3fdaa` (Sprint 7 completo e tema di accesso); realm aggiornato da te dalla console |

## 2. Cosa è stato consegnato

| ID | Storia | Punti | PR | In una frase |
|---|---|---|---|---|
| US-306 | Segnalazione speciale dalla bacheca | 3 | [#74](https://github.com/maubernardi/nexus/pull/74) | "Candida un beneficiario" dalla bacheca; la candidatura blocca l'annuncio finché il Call Center non decide. |
| US-403 | Approvazione fast-track | 5 | [#75](https://github.com/maubernardi/nexus/pull/75) | Il Call Center vede mansione e azienda dell'annuncio e approva: proposta all'azienda e annuncio archiviato. |
| US-404 | Rifiuto fast-track | 3 | [#76](https://github.com/maubernardi/nexus/pull/76) | Rifiuto con motivo: la segnalazione resta in lavorazione normale e l'annuncio torna candidabile. |
| US-106 | Cataloghi di zone e tipologie | 3 | [#77](https://github.com/maubernardi/nexus/pull/77) | Pagina "Cataloghi" dell'ADMIN: aggiunta, nuovo nome, disattivazione e riattivazione, con audit. |
| US-108 | Tipi di tirocinio | 1 | [#79](https://github.com/maubernardi/nexus/pull/79) | Terzo catalogo, con Curriculare, Extracurriculare, Di inclusione, Di inserimento socio-terapeutico. |
| US-506 | Legale rappresentante e codici ATECO | 3 | [#80](https://github.com/maubernardi/nexus/pull/80) | Due sezioni nella scheda dell'azienda; codice fiscale controllato, audit senza valori. |
| US-1001 | Scheda amministrativa del tirocinio | 8 | [#81](https://github.com/maubernardi/nexus/pull/81), [#82](https://github.com/maubernardi/nexus/pull/82) | La scheda del solo Tutor, modificabile in qualunque momento, con i dati dell'azienda, del beneficiario e le sospensioni. |
| US-1002 | Griglia oraria e monte ore | 5 | [#83](https://github.com/maubernardi/nexus/pull/83) | Fasce modificabili, ore per giorno, festività escluse a scelta; monte ore calcolato su date e sospensioni. |
| US-1304 | Tema di Keycloak (fuori piano) | 3 | [#85](https://github.com/maubernardi/nexus/pull/85) | La pagina di accesso con l'aspetto del sito, solo in italiano; immagine `nexus-keycloak`. |

### Il percorso dopo lo Sprint 7

```
Bacheca ──(Candida un beneficiario)──► Segnalazione speciale ──(Approva corsia veloce)──► Proposta all'azienda
                                              │                                                  │
                                              └──(Rifiuta, con motivo)──► lavorazione normale     │
                                                                                                 ▼
Segnalazione ──► Coda ──► Abbinamento ──► Proposta ──► Accettata ──► Appuntamento fissato ──► Scheda del tirocinio
                                                                                               (dati, griglia, monte ore)
                                                                                                     │
                                                                              Sprint 8: avvio, documenti Word, chiusura
```

## 3. Demo guidata

Le prove passo per passo sono nel **collaudo**, sulla versione `db3fdaa`:
- **sezione 12:** candidature dalla bacheca e corsia veloce (T-12.1–T-12.4);
- **sezione 13:** cataloghi dell'ADMIN (T-13.1–T-13.3);
- **sezione 14:** dati dell'azienda, scheda del tirocinio, griglia oraria e monte ore (T-14.1–T-14.7);
- **sezione 15:** la nuova pagina di accesso (T-15.1–T-15.2).

Per la scheda serve una segnalazione con l'**appuntamento fissato** (prove della sezione 5).

## 4. Qualità

### Test automatici

| | Fine Sprint 6 | Fine Sprint 7 |
|---|---|---|
| Backend – unit | 55 | 69 |
| Backend – integrazione (PostgreSQL reale) | 221 | 280 |
| Frontend – componenti (tutti con controllo axe) | 123 | 156 |
| CI – pagina di accesso (Keycloak con il tema) | — | 1 job |

Da segnalare:
- **I vincoli nuovi sono anche nel database e hanno un test ciascuno**, compreso quello che impedisce le sospensioni
  sovrapposte (`EXCLUDE` sui range, senza estensioni).
- **Il monte ore della prova reale (502 ore) coincide con un calcolo indipendente**, fatto a parte con festività e
  sospensioni.

### Accessibilità (WCAG 2.2 AA)

Verificate nell'applicazione vera:
- pagina dei cataloghi e scheda del tirocinio: tabella della griglia con le intestazioni e le caselle con il nome del
  giorno; errori del server riportati sul campo giusto;
- pagina di accesso di Keycloak: axe-core sulla pagina servita da Keycloak, in chiaro e in scuro, con l'errore di
  accesso; a 390 px nessuno scorrimento laterale.

**0 violazioni**, sia nei test sia sulla pagina di accesso.

### Difetti

| Difetto | Dove | Come è emerso | Correzione |
|---|---|---|---|
| Riga vuota "In lavorazione → In lavorazione" nella cronologia dopo la presa in carico di una speciale | US-403 | Prova nell'applicazione vera | Prima del merge (#75), con un test che falliva senza la correzione |
| Zona della mansione etichettata "Zona di residenza" nella corsia veloce | US-403 | Prova nell'applicazione vera | Prima del merge (#75) |
| Fasce orarie spostate di un'ora: il fuso UTC pensato per gli istanti si applicava anche agli orari | US-1002 | Prova nell'applicazione vera | Prima della PR: orari in minuti e un test che rilegge il valore dal database |
| Scheda bloccata all'avvio del tirocinio: un limite che non avevi chiesto | US-1001 | Tu, dopo il merge | [#82](https://github.com/maubernardi/nexus/pull/82); vedi A14 |
| Primo caricamento della nuova immagine `nexus-keycloak` rifiutato da GitHub | Deploy | Deploy del 29/10 | Rilancio del job; la demo non è mai stata toccata. Nota nel README |
| Due test dei moduli del beneficiario al limite del tempo massimo, solo in locale e con la suite completa | Frontend | Verifiche prima delle PR | Da fare: A13 |

## 5. Decisioni prese durante lo sprint

| # | Decisione | Presa da | Dove |
|---|---|---|---|
| D1 | Una sola candidatura per annuncio: la prima lo blocca finché il Call Center non decide | PO (§8.1 review Sprint 6) | US-306, US-403, US-404 |
| D2 | Se l'annuncio si archivia mentre la candidatura aspetta, la segnalazione perde la corsia veloce e il Tutor è avvisato | Team | US-404 |
| D3 | Scheda ampliata: legale rappresentante, più tutor aziendali, sospensioni, codici ATECO dell'azienda scelti nel tirocinio | PO (28–29/10) | US-506, US-1001 |
| D4 | Tipi di tirocinio come catalogo dell'ADMIN | PO (29/10) | US-108 |
| D5 | **I documenti sono competenza del Tutor**: la scheda la vede solo il Tutor della segnalazione | PO (29/10) | US-1001, US-1004, US-1005 |
| D6 | **La scheda si corregge in qualunque momento**, anche a tirocinio avviato | PO (29/10) | US-1001, US-1003 |
| D7 | Griglia: due fasce modificabili, ore **per giorno**, festività nazionali escluse con una casella (attiva di default), massimo 40 ore settimanali | PO (29/10) | US-1002 |
| D8 | Pagina di accesso con l'aspetto del sito e **solo italiano** | PO (29/10) | US-1304 |
| D9 | Codice fiscale controllato con la data di nascita; sospensioni dentro il periodo del tirocinio; con l'appuntamento fallito della scheda restano solo i dati del beneficiario; un codice ATECO scelto in una scheda non si toglie dall'azienda | Team, senza obiezioni finora | US-1001 |

## 6. Stato del prodotto rispetto alla roadmap

**R1** — sviluppo completo; resta **US-1301** (PWA su smartphone, a cura del PO: prova T-10.1 del collaudo).

**R2** — ✅ **completa** (69 punti su 69) con lo Sprint 7.

**R3** — *tirocinio, documenti Word, chiusura, email*: fatti **17 punti su 43** (US-108, US-506, US-1001, US-1002).
Restano 7 storie, **26 punti**:

| Gruppo | Storie | Punti |
|---|---|---|
| Tirocinio e documenti | US-1003 avvio (1), US-1004 Convenzione in Word (5), US-1005 Progetto Formativo in Word (3) | 9 |
| Chiusura | US-1101 report di restituzione (5), US-1102 esito sulla mansione e audit trail (2) | 7 |
| Email | US-1202 email transazionali (5): servono le credenziali SMTP | 5 |
| Beneficiari | US-205 curriculum del beneficiario (5, Could) | 5 |

**R4** — *produzione*: anticipata **US-1304** (3 su 50).

**Product backlog:** 68 elementi, 240 punti; completati 50 elementi (166 punti) negli Sprint 1–7.

## 7. Retrospettiva

**Cosa ha funzionato**
- **31 punti**, il massimo finora, con lo sprint ampliato a metà strada: le richieste del 28–29/10 sono entrate e
  uscite nello stesso giorno.
- **La preparazione comune dei test (A7)**, finalmente fatta all'inizio dello sprint, ha reso veloci i test della
  corsia veloce e della scheda.
- **La prova nell'applicazione vera** ha trovato tre difetti prima del merge. Il più insidioso, l'ora spostata nelle
  fasce, i test non lo vedevano: girano nel fuso del database.
- **Le tue risposte rapide alle domande puntuali** (griglia, festività) hanno evitato rifacimenti su US-1002.
- **A10 e A11 applicate**: dati simulati con i campi omessi, test con le date anche in UTC.

**Cosa non ha funzionato**
- **Ho aggiunto un limite che non avevi chiesto**: la scheda in sola lettura dopo l'avvio. L'ho presentato come scelta
  da confermare, ma era già unito: è servita una seconda PR (#82).
- **Il primo deploy si è fermato**: GitHub ha rifiutato al primo colpo un'immagine nuova. Nessun danno, ma non era
  previsto né documentato.
- **Ti ho dato i passi della console in due riprese** (prima il tema, poi la lingua). Andava data subito una lista
  completa da spuntare.

**Azioni**

| # | Azione | Chi |
|---|---|---|
| A13 | Velocizzare i due test lenti dei moduli del beneficiario: la suite deve restare verde anche in locale | Team |
| A14 | Le scelte che **limitano** l'utente (blocchi, sola lettura, permessi tolti) si chiedono **prima** di realizzarle, non dopo | Team |
| A15 | I passi manuali sul server o nella console si danno come lista completa da spuntare, e si verificano subito dopo | Team |
| A12 | (confermata) La PR della review si apre in bozza; questa lo è | Team |

## 8. Da decidere

### 8.1 Tema di Keycloak anticipato (US-1304)
Il tema era già nel backlog come **US-1304** (Should, 3 punti, R4). L'abbiamo fatto il 29/10 come digressione.
*Proposta:* US-1304 Done nello Sprint 7, **fuori piano**, ma **senza contarla nella velocity** (resta 31), come le
correzioni fuori impegno degli sprint precedenti: così la velocity resta una base onesta per il planning.

> ✅ **Deciso (PO, 29/10): proposta accettata.** US-1304 Done (Sprint 7, fuori piano); velocity 31.

### 8.2 Quando la scheda è "completa" per l'avvio (US-1003)
L'avvio porta la segnalazione in IN_TIROCINIO solo con la scheda completa, e la scheda resta modificabile (D6).
*Proposta:* per l'avvio servono:
- tipo di tirocinio, data di inizio, data di fine prevista, sede, codice ATECO;
- almeno un tutor aziendale;
- la griglia oraria con almeno un giorno;
- codice fiscale, data e luogo di nascita, residenza del beneficiario;
- il legale rappresentante dell'azienda, con codice fiscale, nascita e residenza.

Sospensioni e note restano facoltative. Se manca qualcosa, la pagina dice **cosa** manca e porta al campo.

> ✅ **Deciso (PO, 29/10): proposta accettata.**

### 8.3 Modelli Word di Convenzione e Progetto Formativo (US-1004, US-1005)
I modelli veri non ci sono ancora (Q5) e dovranno essere modificabili, con modalità ancora da decidere.
*Proposta:*
- nello Sprint 8, **modelli segnaposto** nel codice, **uno per tipo di tirocinio**, che il Tutor scarica compilati dalla
  scheda;
- i segnaposto coprono tutti i dati della scheda, compresi `{GRIGLIA_ORARIA}` e `{MONTE_ORE_TOTALE}`;
- la modifica dei modelli diventa una **storia a parte**, quando avrai deciso come: per esempio l'ADMIN carica i
  `.docx` nella pagina "Cataloghi".

> ✅ **Deciso (PO, 29/10): proposta accettata.** Modelli segnaposto per tipo di tirocinio; la modifica dei modelli più
> avanti, in una storia a parte.

### 8.4 Voci dei menu del report di restituzione (US-1101)
Le formule di assunzione e i motivi della mancata assunzione "devono poter cambiare senza rilascio".
*Proposta:* due cataloghi in più nella pagina "Cataloghi" dell'ADMIN, come zone, tipologie e tipi di tirocinio. Sono
una storia nuova, **US-109** *Cataloghi del report di restituzione* (2 punti, R3), con le voci provvisorie di Q6 come
valori iniziali.

> ✅ **Deciso (PO, 29/10): proposta accettata.** US-109 entra nel backlog (2 punti, R3).

### 8.5 Chiusura dello Sprint 7
*Proposta:* chiudere lo Sprint 7 con questa review (velocity 31) e avviare lo Sprint 8 alla conferma del planning.

> ✅ **Deciso (PO, 29/10).** Sprint 7 chiuso con velocity 31; lo Sprint 8 parte con il planning confermato (§9).

## 9. Proposta per lo Sprint 8 (3 giorni, ~20 punti)

**Obiettivo proposto:** *il percorso arriva in fondo. Il Tutor avvia il tirocinio, scarica Convenzione e Progetto
Formativo compilati e chiude con il report di restituzione; la mansione torna disponibile se l'azienda lo dice.*

| Ordine | ID | Storia | Punti | Release |
|---|---|---|---|---|
| 1 | US-1003 | Avvio del tirocinio (scheda completa, §8.2) | 1 | R3 |
| 2 | US-1004 | Convenzione in Word (modelli segnaposto per tipo, §8.3) | 5 | R3 |
| 3 | US-1005 | Progetto Formativo in Word | 3 | R3 |
| 4 | US-109 | Cataloghi del report di restituzione (§8.4) | 2 | R3 |
| 5 | US-1101 | Report di restituzione | 5 | R3 |
| 6 | US-1102 | Esito sulla mansione e audit trail | 2 | R3 |
| | | **Totale** | **18** | |

**Extra**, nell'ordine:
- US-1202 *Email transazionali* (5), se arrivano le credenziali SMTP;
- altrimenti US-205 *Curriculum del beneficiario* (5);
- poi US-102 *Gestione progetti* (3), il primo passo di R4.

Ordine tecnico: prima A13 (test lenti), poi le storie. Dipende dalle decisioni §8.2–§8.4. Per il PO: collaudo
(sezioni 12–15) e US-1301.

> ✅ **Deciso (PO, 29/10): piano confermato, con le email nell'impegno.** Arrivano i dati SMTP, quindi **US-1202**
> *Email transazionali* (5) entra nello sprint: **23 punti**. Extra, nell'ordine: US-205 *Curriculum del beneficiario*
> (5), US-102 *Gestione progetti* (3). La password SMTP va solo nel `.env` del server, mai nel repository né in chat.
> Sprint 8: [sprint-08.md](sprint-08.md).

## 10. Azioni a carico del PO

- [ ] Eseguire il collaudo sulla versione `db3fdaa`: almeno le sezioni 12–15.
- [ ] US-1301: PWA su Android e iOS (collaudo T-10.1).
- [x] Decidere i punti §8.1–§8.5 e confermare il planning (§9).
- [ ] Dati SMTP IONOS per le email (US-1202): server, porta, utente e mittente al team; la password direttamente nel
  `.env` del server.
