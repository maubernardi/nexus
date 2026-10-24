# Sprint 3 — Review, retrospettiva e planning

**Sprint:** 24 – 26 ottobre 2026, prolungato su richiesta del PO (planning 24/10) · **Review:** 24/10/2026 ·
**Partecipanti:** Mauro Bernardi (Product Owner), team di sviluppo (Claude Code) · **Demo:** https://portalenexus.it,
ultimo deploy il 05/10 (`db15fc8`): **non contiene ancora il lavoro dello Sprint 3** (vedi §9).

> Documento unico e asincrono (metodo dal 05/10): review (§1–§6), retrospettiva (§7), decisioni da prendere (§8) e
> proposta di planning dello Sprint 4 (§9). Ne discutiamo un punto alla volta, quando il PO è disponibile.

---

## 1. Sintesi

| | |
|---|---|
| **Obiettivo dello sprint** | Il Tutor segue le proprie segnalazioni e i propri beneficiari, con la cronologia completa; il Call Center lavora in modo condiviso. Il rilascio R1 è chiuso. |
| **Esito** | ✅ **Raggiunto per la parte del Tutor e del Call Center.** ⚠️ **R1 non chiuso**: le note operative del PO (24/10) gli hanno aggiunto 10 punti (US-503, US-505, US-604), oltre a US-1301 a cura del PO. |
| **Impegno / completato** | 21 punti / **21 punti** (9 voci su 9; US-304 assorbita in US-303) |
| **Fuori piano** | Suggerimenti dei campi in un'icona "i" con tooltip, richiesta del PO ([#34](https://github.com/maubernardi/nexus/pull/34)) |
| **Velocity** | Sprint 1: 21 · Sprint 2: 25 · Sprint 3: **21** (primo sprint corto) · media 22 |
| **PR unite** | 10 (#29–#38): 8 di funzionalità o correzione, 2 di documentazione |
| **Migrazioni del database** | Nessuna (ultima resta V14) |

## 2. Cosa è stato consegnato

| ID | Storia | Punti | PR | In una frase |
|---|---|---|---|---|
| EN-5 | Controllo della numerazione delle migrazioni nella CI | 1 | [#29](https://github.com/maubernardi/nexus/pull/29) | La CI rifiuta una PR con una migrazione non successiva all'ultima su `main`, duplicata o che modifica una già unita. |
| EN-4 | Contatti dei referenti aziendali nell'audit senza valori | 1 | [#30](https://github.com/maubernardi/nexus/pull/30) | Referente, telefono ed email: nell'audit solo "campo modificato". |
| US-504 | Disattivazione impedita con mansioni bloccate | 1 | [#32](https://github.com/maubernardi/nexus/pull/32) | Messaggio con la mansione e il numero della segnalazione che la blocca. |
| US-405 | Lavorazione condivisa tra operatori | 2 | [#33](https://github.com/maubernardi/nexus/pull/33) | Ogni operatore lavora ogni segnalazione presa in carico; "Prendila in carico tu" la sposta su di sé, con audit. |
| US-303 | Elenco delle segnalazioni (Home) | 5 | [#35](https://github.com/maubernardi/nexus/pull/35) | Home unica per tutti con viste per ruolo, filtri per stato e progetto; spariscono "Coda" e "Le mie lavorazioni" (i vecchi indirizzi reindirizzano). |
| US-305 | Dettaglio e cronologia del ticket | 3 | [#36](https://github.com/maubernardi/nexus/pull/36) | Pagina unica `/segnalazioni/:id` per tutti i ruoli; cronologia con data, autore, azione, stato e nota, letta dall'audit. |
| US-202 | I miei beneficiari | 3 | [#37](https://github.com/maubernardi/nexus/pull/37) | Elenco del Tutor con ricerca per nome e cognome, zona e segnalazione aperta. |
| US-203 | Modifica beneficiario | 2 | [#37](https://github.com/maubernardi/nexus/pull/37) | Solo il proprietario; nell'audit i soli nomi dei campi cambiati. |
| US-204 | Visibilità dei beneficiari per progetto | 3 | [#37](https://github.com/maubernardi/nexus/pull/37) | Scheda completa anche ai Tutor dei progetti in cui il beneficiario ha segnalazioni; per gli altri "non trovato". |

### Il flusso dopo lo Sprint 3

```
Tutor                                          Call Center
  Home: Le mie segnalazioni / Del mio progetto   Home: Da prendere in carico / In carico a me / Tutte
  Beneficiari: elenco, scheda, modifica            │ Prendi in carico  (o "Prendila in carico tu" da un collega)
  │ registra e segnala ──────────────────────────► │ Mansioni compatibili ► Proponi ► PROPOSTA_AZIENDA
  └─ apre la segnalazione: dati e cronologia ◄─────┘ ogni passo è nella cronologia, con autore
```

Il flusso non è più "a senso unico": il Tutor vede stato e storia delle proprie segnalazioni e di quelle del progetto.

## 3. Demo guidata (dopo il deploy, §9)

Utenti demo: `tutor1` e `tutor2` (entrambi nel progetto GOL; `tutor2` anche in POLIS), `operatore.cc`.

1. **Tutor** (`tutor1`) — si apre la Home "Segnalazioni", vista *Le mie segnalazioni*. Provare la vista *Del mio
   progetto* e il filtro per stato.
2. *Beneficiari* → *Nuovo beneficiario* → registrarlo → *Segnala questo beneficiario* → inviare. Tornare su
   *Beneficiari*: il beneficiario ha la segnalazione aperta (n. N). Cercarlo per cognome e nome.
3. Aprire la scheda → *Modifica* → cambiare un dato → *Salva modifiche*: il messaggio "Modifiche salvate." riceve il
   focus.
4. **Call Center** (`operatore.cc`) — Home, vista *Da prendere in carico*: la n. N è in coda → *Prendi in carico* →
   aprirla → mansioni compatibili → *Proponi* → *Conferma*.
5. **Tutor** (`tutor1`) — aprire la n. N dalla Home: la cronologia mostra "Segnalazione inviata", "Presa in carico"
   (con l'operatore), "Proposta all'azienda" («mansione» presso azienda), ciascuna con autore e data.
6. **Collega di progetto** (`tutor2`) — la n. N compare in *Del mio progetto*; la scheda del beneficiario è visibile,
   completa, ma senza *Modifica*.
7. **Lavoro condiviso** (facoltativo, due operatori): il secondo apre una segnalazione in carico al primo, può abbinarla
   oppure usare *Prendila in carico tu*.

## 4. Qualità

### Test automatici

| | Fine Sprint 2 | Fine Sprint 3 |
|---|---|---|
| Backend – unit | 26¹ | 27 |
| Backend – integrazione (PostgreSQL reale) | 141 | 149 |
| Frontend – componenti (tutti con controllo axe) | 62 | 73 |

¹ Ricontato sul codice di fine Sprint 2: la review precedente riportava 30 per un errore di conteggio. Lo Sprint 3 ha
spostato la verifica soprattutto nei test di integrazione (elenco, dettaglio, cronologia, beneficiari). CI verde su
tutte le PR.

### Accessibilità (WCAG 2.2 AA)

Ogni pagina nuova o cambiata è stata verificata in un browser reale (Chromium + axe) a 320 e 1280 px, in tema chiaro e
scuro: Home con le viste per ruolo, dettaglio della segnalazione con cronologia, I miei beneficiari, scheda e modifica
del beneficiario, suggerimenti dei campi. **0 violazioni**, nessuno scorrimento laterale; ogni esito di salvataggio
riceve il focus.

### Difetti trovati e corretti durante lo sprint (prima del merge)

| Difetto | Dove | Come è emerso | Correzione |
|---|---|---|---|
| Campi disallineati dal testo di aiuto sotto l'etichetta | Moduli (P.IVA, anno di nascita…) | Segnalazione del PO | Icona "i" con tooltip conforme a WCAG 1.4.13 (#34) |
| Tooltip oltre il bordo dello schermo a 320 px; chiuso dal clic dopo il passaggio del mouse | Suggerimenti dei campi | Verifica in browser | Ancorato alla larghezza del campo; il clic apre sempre |
| Richieste del Tutor partite prima di conoscere l'utente (errori 400/403) | Home | Verifica in browser | Richieste avviate solo a utente caricato |
| Area cliccabile del numero spostata | Tabella della Home | Verifica in browser | `aria-label` sul link al posto di un testo nascosto |
| Eventi doppi nella cronologia delle segnalazioni nate prima dell'audit | Dettaglio della segnalazione | Verifica sui dati demo | Confronto per passaggio di stato, non per istante |

Nessun difetto noto sul codice unito; la verifica sul campo attende il deploy.

### Debito tecnico e note operative

- **Documentazione nelle PR di funzionalità**: #29 e #30 aggiornavano righe vicine del backlog e sono andate in
  conflitto. Da allora gli stati si aggiornano in un'unica PR di documentazione dopo i merge (#38).
- **Delta OpenSpec**: un requisito nuovo dichiarato come "modificato" (US-405) passa la validazione della CI ma blocca
  l'archiviazione; corretto in #38. Vedi l'azione in §7.
- **Ambiente di lavoro condiviso**: due PR (#29, #30) avevano incluso per errore le note personali del PO
  (`NoteOperative`); rimosse prima del merge, e da allora il team aggiunge ai commit solo i propri file.

## 5. Decisioni prese durante lo sprint

| # | Decisione | Presa da | Dove |
|---|---|---|---|
| D1 | Home unica "Segnalazioni" per tutti i ruoli, con viste; US-304 assorbita | PO (note operative) | US-303 |
| D2 | Pagina di dettaglio unica per tutti i ruoli; per il Call Center include la lavorazione | Team | US-305 |
| D3 | Cronologia letta dall'audit; per le segnalazioni precedenti all'audit si integra lo storico degli stati | Team | US-305 |
| D4 | Un Tutor vede un beneficiario altrui se questo ha una segnalazione (anche conclusa) in un suo progetto | Team (da Q11) | US-204 |
| D5 | "I miei beneficiari" resta solo del proprietario; quelli dei colleghi si raggiungono dalle segnalazioni | Team | US-202 |
| D6 | Nel menu del Tutor "Beneficiari" sostituisce "Nuovo beneficiario" (diventa un pulsante dell'elenco) | Team → PO | US-202 — ✅ confermata ed estesa a "Nuova segnalazione" (§8.1) |
| D7 | Voce del menu attiva anche nelle pagine interne della sezione | Team | navigazione |
| D8 | Nessun blocco ottimistico sul beneficiario (lo modifica solo il suo Tutor) | Team | US-203 |
| D9 | In modifica la zona già assegnata resta valida anche se disattivata | Team | US-203 |
| D10 | Suggerimenti dei campi in un'icona "i" invece del testo sotto l'etichetta | PO | moduli |
| D11 | Stati del backlog e archiviazione delle change in una PR di documentazione dopo i merge | Team | processo |

## 6. Stato del prodotto rispetto alla roadmap

**Rilascio R1** — *"il tutor registra e segnala un beneficiario; il Call Center lo prende in carico e lo abbina a una
mansione"*: flusso completo e seguibile da entrambe le parti. Per chiuderlo restano 4 voci, 11 punti:

| ID | Storia | Punti | Perché serve |
|---|---|---|---|
| US-503 | Storico e pregressi dell'azienda | 3 | Valutare l'azienda prima di proporle un beneficiario (nota 5) |
| US-505 | Diario delle note dell'azienda | 2 | Condividere informazioni tra operatori (nota 2) |
| US-604 | Dettagli di azienda e mansione durante l'abbinamento | 5 | Scegliere la proposta con tutte le informazioni (nota 5); dipende da US-503 e US-505 |
| US-1301 | Verifica della PWA online | 1 | A cura del PO (smartphone) |

**Product backlog:** 65 elementi attivi, 230 punti; completati 21 elementi (67 punti) negli Sprint 1–3.

## 7. Retrospettiva

**Cosa ha funzionato**
- Lo sprint corto con ~20 punti: impegno completato nella prima giornata, con PR piccole e unibili una alla volta.
- Le note operative discusse una per una hanno cambiato US-303 prima di svilupparla, non dopo.
- Verifica in browser reale prima di ogni PR: 5 difetti trovati prima del merge (§4).

**Cosa non ha funzionato**
- La demo è ferma al 05/10: le prove del PO non vedono il lavoro dello sprint.
- Conflitti tra PR per righe vicine del backlog; note personali del PO finite in due PR.
- Un errore nella delta OpenSpec di US-405 è emerso solo all'archiviazione.
- L'obiettivo "R1 chiuso" è stato superato dalle note operative, aggiunte a sprint iniziato.

**Azioni**

| # | Azione | Chi |
|---|---|---|
| A1 | Deploy della demo dopo ogni gruppo di merge, o almeno prima di ogni review | PO (il team lo ricorda in ogni PR di documentazione) |
| A2 | Prima di aprire una PR, il team prova l'archiviazione della change in una copia locale, oltre alla validazione | Team |
| A3 | Le storie aggiunte a sprint iniziato entrano nello sprint successivo; l'obiettivo si aggiorna solo se il PO lo chiede | Team + PO |
| A4 | Già in vigore: gli stati del backlog si aggiornano solo nella PR di documentazione; nei commit solo i file del team | Team |

## 8. Da decidere

### 8.1 Voce "Beneficiari" nel menu del Tutor (D6)
Oggi il menu del Tutor ha *Segnalazioni*, *Beneficiari*, *Nuova segnalazione*; "Nuovo beneficiario" è un pulsante in
cima all'elenco dei beneficiari.
*Proposta:* tenerlo così (menu corto, anche su smartphone). Alternativa: rimettere "Nuovo beneficiario" nel menu
(4 voci).

> ✅ **Deciso (PO, 24/10): proposta accettata, resa uniforme.** "Nuovo beneficiario" è il pulsante in cima all'elenco
> dei beneficiari e, allo stesso modo, **"Nuova segnalazione" diventa un pulsante in cima all'elenco delle
> segnalazioni** (Home del Tutor) ed esce dal menu. Il menu del Tutor resta *Segnalazioni*, *Beneficiari*. Piccola
> modifica al codice, senza punti.

### 8.2 Notifica al Tutor quando l'operatore respinge la nuova tipologia (US-302)
La storia chiede una notifica, ma le notifiche (US-1201, in app ed email) arrivano più avanti.
*Proposta:* in US-302 il Tutor vede l'esito nella cronologia della segnalazione e un'evidenza nella Home ("tipologia
cambiata dal Call Center"); la notifica vera arriverà con US-1201.

> ✅ **Deciso (PO, 24/10): proposta accettata.** Finché non arrivano le notifiche (US-1201):
> - **cronologia**: evento "Tipologia cambiata dal Call Center" (proposta → scelta), con motivo, autore e data;
> - **Home** del Tutor: evidenza testuale "Tipologia cambiata" sulla segnalazione, finché il Tutor non la apre (segno
>   "già visto" per Tutor), non affidata al solo colore.
>
> Con US-1201 lo stesso evento produrrà anche notifica in app ed email. Criterio aggiunto a US-302 (resta 5 punti).

### 8.3 Collegamento dalla segnalazione al beneficiario
Nel dettaglio della segnalazione il nome del beneficiario è solo testo.
*Proposta:* renderlo un link alla scheda del beneficiario (piccola modifica, nessun punto): la visibilità è già
garantita dalle stesse regole.

### 8.4 Chiusura dello Sprint 3
*Proposta:* chiudere lo Sprint 3 con questa review e avviare lo Sprint 4 alla conferma del planning (§9).

## 9. Proposta per lo Sprint 4 (3 giorni, ~20 punti)

**Obiettivo proposto:** *l'operatore sceglie la proposta con lo storico e le note dell'azienda (R1 chiuso); il Tutor
accetta la proposta e parte il timer sui giorni lavorativi del progetto.*

| ID | Storia | Punti | Release |
|---|---|---|---|
| US-505 | Diario delle note dell'azienda | 2 | R1 |
| US-503 | Storico e pregressi dell'azienda | 3 | R1 |
| US-604 | Dettagli di azienda e mansione durante l'abbinamento | 5 | R1 |
| US-107 | Calendario del progetto (patrono, chiusure) | 3 | R2 |
| US-703 | Calendario dei giorni lavorativi | 3 | R2 |
| US-701 | Accettazione della proposta (timer di 7 giorni lavorativi) | 3 | R2 |
| | **Totale** | **19** | |

Ordine: prima le tre storie di R1 (US-604 dipende dalle altre due), poi la catena US-107 → US-703 → US-701.
Tutte soddisfano la Definition of Ready (Q4 e Q13 già risolte). **Extra**, se avanza tempo: US-302 *Nuova tipologia dal
Tutor* (5), dopo la decisione §8.2. Per il PO: US-1301 e il deploy della demo (A1).

*Alternativa:* al posto della catena del calendario, US-302 (5) e US-701 senza timer; sconsigliata, perché US-701 senza
calendario andrebbe rifatta.

## 10. Azioni a carico del PO

- [ ] Deploy della demo con il lavoro dello Sprint 3 (`main` attuale) e prova della demo guidata (§3).
- [ ] US-1301: installazione e funzionamento offline della PWA su Android e iOS.
- [ ] Decidere i punti §8.1–§8.4 e confermare il planning (§9).
- [ ] Q5: modelli Word reali e modalità di modifica dei modelli (servono per R3).
