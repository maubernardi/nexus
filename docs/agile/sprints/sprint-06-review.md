# Sprint 6 — Review, retrospettiva e planning

**Sprint:** 28 – 30 ottobre 2026 (planning 28/10) · **Review:** 28/10/2026 · **Partecipanti:** Mauro Bernardi (Product
Owner), team di sviluppo (Claude Code) · **Demo:** https://portalenexus.it, versione `b8930e7` (tutto lo Sprint 6) ·
**Collaudo:** [docs/test/collaudo.md](../../test/collaudo.md), prove T-5.8, T-5.9 e sezione 11.

> Documento unico e asincrono: review (§1–§6), retrospettiva (§7), decisioni da prendere (§8) e proposta di planning
> dello Sprint 7 (§9). Ne discutiamo un punto alla volta; le decisioni le aggiungo in questa PR, da unire alla fine.

---

## 1. Sintesi

| | |
|---|---|
| **Obiettivo dello sprint** | La bacheca è operativa: il Call Center pubblica annunci (da una mansione libera o dal rilancio dopo un appuntamento fallito) e il Tutor li consulta senza vedere l'azienda. |
| **Esito** | ✅ **Raggiunto.** Il Call Center gestisce gli annunci dalla bozza all'archivio, li crea dalle mansioni libere o dal rilancio, e li apre a tutti dopo 7 giorni. Il Tutor li consulta dallo smartphone, senza azienda, titolo e note. Un annuncio vive solo finché la sua mansione è disponibile. |
| **Impegno / completato** | 20 punti / **20 punti** (6 storie su 6), tutte unite il 28/10 |
| **Fuori impegno** | Correzione [#68](https://github.com/maubernardi/nexus/pull/68): campi omessi dall'API (Home del Tutor in errore senza timer) |
| **Velocity** | Sprint 1: 21 · 2: 25 · 3: 21 · 4: 24 · 5: 21 · 6: **20** · media 22,0 |
| **PR unite** | 11 (#62–#72): 6 di funzionalità, 1 correzione, 4 di documentazione |
| **Migrazioni del database** | Nessuna: tabella, vincoli e dati demo della bacheca c'erano dalla V6 |
| **Deploy della demo** | 28/10: `b8930e7` (Sprint 6 completo) |

## 2. Cosa è stato consegnato

| ID | Storia | Punti | PR | In una frase |
|---|---|---|---|---|
| US-307 | Scadenza nella Home del Tutor | 2 | [#65](https://github.com/maubernardi/nexus/pull/65) | Colonna "Scadenza" nelle viste del Tutor, "in scadenza" dopo il promemoria, la più vicina in cima. Nata dalla tua nota operativa. |
| US-902 | Gestione della bacheca | 5 | [#66](https://github.com/maubernardi/nexus/pull/66) | Voce di menu "Bacheca" con bozze, pubblicati e archiviati; modifica, pubblicazione e archiviazione con conferma e audit. |
| US-904 | Annuncio da una mansione libera | 3 | [#67](https://github.com/maubernardi/nexus/pull/67) | "Crea annuncio" nella scheda dell'azienda; un solo annuncio attivo per mansione. |
| US-901 | Bacheca per il Tutor | 3 | [#69](https://github.com/maubernardi/nexus/pull/69) | Schede per lo smartphone con filtri, mai azienda, titolo o note; un annuncio si archivia quando la sua mansione non è più disponibile. |
| US-803 | Rilancio in bacheca | 5 | [#70](https://github.com/maubernardi/nexus/pull/70) | Casella nel modulo dell'appuntamento fallito; bozza riservata al progetto e notifica al Call Center. |
| US-903 | Sblocco a tutti dopo 7 giorni | 2 | [#71](https://github.com/maubernardi/nexus/pull/71) | "Rendi visibile a tutti" solo dopo 7 giorni solari dalla pubblicazione, con la data dello sblocco. |

### La vita di un annuncio dopo lo Sprint 6

```
Mansione libera ──(Crea annuncio)──┐
Appuntamento fallito ──(rilancio)──┴─► Bozza ──(Pubblica)──► Pubblicato ──(dopo 7 gg: Rendi visibile a tutti)──► Per tutti
                                         │                       │
                                         └──(Archivia)───────────┴──► Archiviato ◄── mansione abbinata, ritirata
                                                                                     o azienda disattivata (automatico)
```

## 3. Demo guidata

Le prove passo per passo sono nel **collaudo**:
- **T-5.8:** scadenza nella Home del Tutor;
- **T-5.9:** rilancio;
- **sezione 11:** bacheca, prove T-11.1–T-11.8.

Sulla demo ci sono già una bozza (#3) e due annunci pubblicati: il #1 per tutti e il #2 riservato a GOL, utile per lo
sblocco.

## 4. Qualità

### Test automatici

| | Fine Sprint 5 | Fine Sprint 6 |
|---|---|---|
| Backend – unit | 55 | 55 |
| Backend – integrazione (PostgreSQL reale) | 201 | 221 |
| Frontend – componenti (tutti con controllo axe) | 101 | 123 |

Un test controlla che la risposta della bacheca al Tutor non contenga **né il nome né l'identificativo dell'azienda**,
nemmeno quando titolo e note la citano.

### Accessibilità (WCAG 2.2 AA)

Verificate nell'applicazione vera:
- pagine della bacheca (gestione e Tutor) a 320 px, senza scorrimento laterale;
- conferme con il focus sulla domanda e ritorno al pulsante se si annulla;
- esiti annunciati;
- riservato e "in scadenza" indicati in testo.

**0 violazioni** nei test axe.

### Difetti

| Difetto | Dove | Come è emerso | Correzione |
|---|---|---|---|
| Test della scadenza rosso solo in CI: la data passava dal fuso della sessione del database (UTC in CI) | Test di US-307 | CI della #65 | Istanti calcolati in Java con il fuso di Roma; da allora i test con date girano anche in UTC |
| Campi età del modulo con "undefined" | Bacheca (US-902) | Prova nell'applicazione vera | Prima del merge |
| **Home del Tutor in errore** senza timer attivo; segnalazione senza operatore vista come "di un collega" | US-307, già su `main` | Prova di US-901 nell'applicazione vera | [#68](https://github.com/maubernardi/nexus/pull/68), **prima del deploy**: la demo non l'ha mai avuto |
| Giorno della settimana sbagliato in un esempio (mar invece di mer 4/11) | Backlog e spec di US-307 | Test del frontend | Corretto nella #64 |

Tre su quattro hanno la stessa radice: **l'API omette i campi vuoti**, mentre i dati simulati dei test usavano `null`.
Vedi l'azione A10.

## 5. Decisioni prese durante lo sprint

| # | Decisione | Presa da | Dove |
|---|---|---|---|
| D1 | Rilancio con una casella nel modulo dell'appuntamento fallito: bozza riservata al progetto, la pubblica il Call Center | PO (§8.1 review Sprint 5) | US-803 |
| D2 | Il Tutor non vede né titolo né note dell'annuncio, solo dati strutturati | PO (§8.2 review Sprint 5) | US-901 |
| D3 | **Un annuncio vive solo finché la sua mansione è disponibile**: abbinamento, ritiro o azienda disattivata lo archiviano, con il motivo nell'audit | PO (28/10) | US-901 |
| D4 | Il progetto di un annuncio si sceglie solo nella bozza; dopo, si apre a tutti solo con lo sblocco | Team | US-902, US-903 |
| D5 | Si crea o pubblica un annuncio solo per una mansione libera, a disposizione e di un'azienda attiva | Team | US-902, US-904 |
| D6 | `/bacheca` unica nel menu di tutti: schede per il Tutor, gestione per Call Center e ADMIN | Team | US-901 |
| D7 | Il file `NoteOperative` è il blocco appunti del PO: il team lo considera solo su richiesta | PO (28/10) | processo |

## 6. Stato del prodotto rispetto alla roadmap

**R1** — sviluppo completo; resta **US-1301** (PWA su smartphone, a cura del PO: prova T-10.1 del collaudo).

**R2** — *proposta, timer, appuntamenti, bacheca e fast-track*: fatti **55 punti su 69**. Restano 4 storie, **14 punti**:

| Gruppo | Storie | Punti |
|---|---|---|
| Fast-track | US-306 segnalazione speciale dalla bacheca (3), US-403 approvazione (5), US-404 rifiuto (3) | 11 |
| Cataloghi | US-106 zone e tipologie per l'ADMIN (3) | 3 |

**R3** — *tirocinio, documenti Word, chiusura, email*: 36 punti, ancora da iniziare. Le storie della scheda e dei
documenti (US-1001–US-1005) partono con i **segnaposto** (Q5).

**Product backlog:** 66 elementi attivi, 233 punti; completati 41 elementi (132 punti) negli Sprint 1–6.

## 7. Retrospettiva

**Cosa ha funzionato**
- Sei storie in una giornata, con la tua decisione sugli annunci presa e realizzata nello stesso giorno.
- La **prova nell'applicazione vera** prima di ogni PR ha trovato due difetti che i test non vedevano. Uno era già su
  `main`, ma l'ha preso prima del deploy.
- L'azione **A8** (controllo dei commit dopo il merge) ha recuperato subito le decisioni rimaste fuori dalla #62.
- La regola "date anche in UTC" ha evitato una seconda sorpresa in CI con lo sblocco dei 7 giorni.

**Cosa non ha funzionato**
- Un difetto è arrivato su `main` (#65): i dati simulati nei test non somigliavano a quelli dell'API.
- La #62 è stata unita mentre stavo ancora aggiungendo le decisioni (secondo merge parziale): la PR della review
  rimane "aperta in discussione" e va detto più chiaramente.
- **A7** (preparazione comune dei test) è ancora rimandata.

**Azioni**

| # | Azione | Chi |
|---|---|---|
| A10 | Nei test dei componenti almeno un dato simulato con i **campi vuoti omessi**, come li manda l'API; nel codice mai `=== null` su dati dell'API | Team |
| A11 | I test con date o scadenze girano anche con il fuso UTC prima della PR | Team |
| A12 | La PR della review si apre in **bozza** (draft) e passa a "pronta" solo a discussione finita | Team |
| A7 | (riportata) Preparazione comune dei test per segnalazioni in un certo stato: all'inizio dello Sprint 7, serve al fast-track | Team |

## 8. Da decidere

### 8.1 Più segnalazioni speciali sullo stesso annuncio (US-306, US-403)
Più Tutor possono candidare un beneficiario allo stesso annuncio #N. Quando il Call Center ne **approva** una (US-403)
la mansione si blocca e l'annuncio si archivia (D3); le altre segnalazioni speciali sullo stesso annuncio restano
fast-track su un annuncio che non c'è più.
*Proposta:* all'approvazione le altre perdono il fast-track **automaticamente**, come nel rifiuto (US-404): restano
SPECIAL, tornano in lavorazione normale con il motivo "annuncio assegnato alla segnalazione n. N", e il loro Tutor
riceve una notifica.

> ✅ **Deciso (PO, 28/10): proposta modificata.** Non possono esserci due candidature sullo stesso annuncio: **la
> prima blocca l'annuncio**. Finché la segnalazione speciale aspetta la decisione del Call Center, nessun altro Tutor
> può candidare un beneficiario a quell'annuncio. Poi:
> - se il Call Center approva (US-403), la mansione si blocca e l'annuncio si archivia;
> - se rifiuta la corsia veloce (US-404), l'annuncio torna candidabile.
>
> *Interpretazione del team, da correggere se non torna:* in bacheca l'annuncio bloccato resta visibile con la scritta
> «Candidatura in valutazione», senza il pulsante. Se l'annuncio si archivia mentre la candidatura aspetta (a mano,
> oppure perché la mansione non è più disponibile, D3), la segnalazione perde la corsia veloce con il motivo e il suo
> Tutor riceve una notifica.

### 8.2 Candidatura a un annuncio (US-306)
*Proposta:* nella scheda dell'annuncio, in bacheca, il Tutor trova **"Candida un beneficiario"**. Sceglie tra i suoi
beneficiari senza segnalazioni aperte e, se l'annuncio è riservato, tra i progetti a cui l'annuncio è riservato. La
segnalazione nasce speciale, fast-track e in lavorazione, con la tipologia della mansione dell'annuncio. Un Tutor non
può candidare lo stesso beneficiario due volte.

> ✅ **Deciso (PO, 28/10): proposta accettata.** Il pulsante "Candida un beneficiario" compare solo sugli annunci non
> bloccati da un'altra candidatura (§8.1).

### 8.3 Completamento dello Sprint 7 oltre R2
R2 si chiude con 14 punti; ne restano circa 6. Le possibilità:
- **A — US-1001** *Scheda amministrativa del tirocinio* (5), con **campi segnaposto** (Q5): avvia R3 sul percorso
  principale (appuntamento → tirocinio).
- **B — US-1202** *Email transazionali* (5): serve il **server SMTP IONOS** (credenziali da te); intanto si prova con
  un server di posta finto, e sulla demo resta spenta finché non hai le credenziali.

*Proposta:* **A**, perché non dipende da nessuno e porta avanti il flusso. B quando le credenziali sono pronte.

> ✅ **Deciso (PO, 28/10): A.** US-1001 entra nello Sprint 7 con campi segnaposto (Q5). US-1202 si fa quando ci
> saranno le credenziali SMTP.

### 8.4 Chiusura dello Sprint 6
*Proposta:* chiudere lo Sprint 6 con questa review (velocity 20) e avviare lo Sprint 7 alla conferma del planning.

> ✅ **Deciso (PO, 28/10).** Sprint 6 chiuso con velocity 20; lo Sprint 7 parte con il planning confermato (§9).

## 9. Proposta per lo Sprint 7 (3 giorni, ~20 punti)

**Obiettivo proposto:** *si chiude R2: il Tutor candida un beneficiario a un annuncio e il Call Center approva o
rifiuta la corsia veloce; l'ADMIN gestisce zone e tipologie; inizia la scheda del tirocinio.*

| Ordine | ID | Storia | Punti | Release |
|---|---|---|---|---|
| 1 | US-306 | Segnalazione speciale dalla bacheca | 3 | R2 |
| 2 | US-403 | Approvazione fast-track | 5 | R2 |
| 3 | US-404 | Rifiuto fast-track | 3 | R2 |
| 4 | US-106 | Cataloghi di zone e tipologie | 3 | R2 |
| 5 | US-1001 | Scheda amministrativa del tirocinio (segnaposto) | 5 | R3 |
| | | **Totale** | **19** | |

Dipende dalle decisioni §8.1–§8.3. Per il PO: collaudo (sezioni 5 e 11) e US-1301.

> ✅ **Deciso (PO, 28/10): piano confermato, 19 punti.** I punti non sono un tetto: «preferisco di più che di meno».
> Se avanza tempo si prosegue con gli extra, nell'ordine **US-1002** *Griglia oraria e sospensioni* (5) e **US-1003**
> *Avvio del tirocinio* (1). Sprint 7: [sprint-07.md](sprint-07.md).

## 10. Azioni a carico del PO

- [ ] Eseguire il collaudo sulla versione `b8930e7` (almeno T-5.8, T-5.9 e la sezione 11).
- [ ] US-1301: PWA su Android e iOS (collaudo T-10.1).
- [ ] Decidere i punti §8.1–§8.4 e confermare il planning (§9).
- [ ] Quando possibile: credenziali SMTP IONOS per le email (US-1202).
