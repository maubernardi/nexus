# Sprint 2 — Sprint Review

**Sprint:** 2–8 ottobre 2026 (planning 01/10) · **Review:** 04/10/2026 · **Partecipanti:** Mauro Bernardi (Product Owner),
team di sviluppo (Claude Code) · **Demo:** https://portalenexus.it, versione `8b12b2f` (deploy riuscito il 02/10 e il 04/10).

> Documento di lavoro per la discussione con il PO. Le sezioni **«Da decidere»** contengono le domande aperte, con una
> proposta del team per ciascuna.

---

## 1. Sintesi

| | |
|---|---|
| **Obiettivo dello sprint** | L'operatore del Call Center prende in carico una segnalazione dalla coda, trova una mansione compatibile e la propone all'azienda; ogni passaggio è registrato nell'audit. |
| **Esito** | ✅ **Raggiunto**: obiettivo verificato in locale sul flusso completo, da tastiera, e in produzione demo (deploy riuscito). |
| **Impegno / completato** | 25 punti / **25 punti** (7 voci su 7) |
| **Extra** | Non presi (US-303, US-305): il lavoro si è concluso il 02/10, primo giorno dello sprint; restano disponibili. |
| **Velocity** | Sprint 1: 21 · Sprint 2: **25** · media 23 |
| **PR unite** | 8 (#19–#26): 6 di funzionalità, 2 di documentazione / decisioni del PO |
| **Migrazioni del database** | V10 (audit), V11 (zone di Firenze), V12 (versione delle aziende), V13 (mansioni ritirabili) |

## 2. Cosa è stato consegnato

| ID | Storia | Punti | PR | In una frase |
|---|---|---|---|---|
| EN-2 | Audit di ogni cambio di stato e scelta | 5 | [#21](https://github.com/maubernardi/nexus/pull/21) | Registro immodificabile (il database rifiuta modifiche, cancellazioni e svuotamenti): chi, quando, cosa, prima/dopo, motivo. |
| EN-3 | Zone iniziali: Firenze e hinterland | 2 | [#22](https://github.com/maubernardi/nexus/pull/22) | 30 quartieri + 10 comuni in ogni ambiente; zone segnaposto della demo disattivate e dati spostati. |
| US-402 | Presa in carico | 2 | [#23](https://github.com/maubernardi/nexus/pull/23) | "Prendi in carico" dalla coda; se un collega è più veloce l'operatore lo sa (409); nuova pagina "Le mie lavorazioni". |
| US-501 | Anagrafica aziende | 3 | [#24](https://github.com/maubernardi/nexus/pull/24) | Ricerca, nuova azienda, scheda con modifica e disattivazione; P.IVA di 11 cifre e univoca; nessuna cancellazione. |
| US-502 | Mansioni dell'azienda | 3 | [#25](https://github.com/maubernardi/nexus/pull/25) | Mansioni nella scheda azienda; stato Libera/Bloccata non modificabile a mano; ritiro invece di cancellazione. |
| US-601 | Ricerca mansioni compatibili | 5 | [#26](https://github.com/maubernardi/nexus/pull/26) | Solo mansioni libere, disponibili, di aziende attive e non escluse; filtri preimpostati e allargabili. |
| US-602 | Abbinamento e proposta | 5 | [#26](https://github.com/maubernardi/nexus/pull/26) | Proposta con conferma: mansione bloccata, segnalazione in "Proposta all'azienda", tracciata in audit, cronologia e storico dell'azienda. |

### Il flusso ora disponibile sulla demo

```
Tutor                          Call Center                                         Azienda
  │ registra il beneficiario
  │ invia la segnalazione ──► Coda (fast-track in cima)
                               │ Prendi in carico ──► Le mie lavorazioni
                               │ Lavora la segnalazione
                               │   mansioni compatibili (zona, tipologia)
                               │   Proponi ► Conferma ───────────────────────────► PROPOSTA_AZIENDA
                               │                                                    (mansione BLOCCATA)
```

Ogni freccia è un evento nell'audit; i cambi di stato sono anche nella cronologia della segnalazione.

## 3. Demo guidata (da fare sul sito)

Utenti demo: `tutor1` (progetto GOL), `operatore.cc` (Call Center). Password come da configurazione del server.

1. **Tutor** — *Nuovo beneficiario* → registra un beneficiario con zona **Novoli** → dalla conferma, *Segnala questo
   beneficiario* → mansione richiesta → *Invia segnalazione*. Annota il numero (es. n. 30).
2. **Call Center** — *Aziende* → *Nuova azienda* (P.IVA di 11 cifre) → nella scheda, *Aggiungi mansione* con zona
   **Novoli** e la stessa tipologia richiesta al punto 1.
3. **Call Center** — *Coda segnalazioni*: la n. 30 è in coda → *Prendi in carico* → *Lavora la segnalazione n. 30*.
4. Nella lavorazione la mansione del punto 2 compare tra le compatibili → *Proponi* → *Conferma proposta*.
5. Controlli: la segnalazione è in **Proposta all'azienda**; nella scheda azienda la mansione è **Bloccata –
   segnalazione n. 30** e non si può ritirare.
6. Prova del conflitto (facoltativa): con due browser e due operatori, proporre la stessa mansione a due segnalazioni:
   il secondo riceve il messaggio "non è più disponibile".

## 4. Qualità

### Test automatici

| | Inizio sprint | Fine sprint |
|---|---|---|
| Backend – unit | 29 | 30 |
| Backend – integrazione (PostgreSQL reale) | 108 | 141 |
| Frontend – componenti (tutti con controllo axe) | 41 | 62 |

CI verde su tutte le PR. Ogni vincolo nuovo del database è testato per nome (`ck_job_slot_active_free`,
`ck_audit_event_*`, immutabilità di `audit_event`).

### Accessibilità (WCAG 2.2 AA)

Ogni pagina nuova è stata verificata in un browser reale (Chromium + axe con contrasto) a 320, 390, 768 e 1280 px,
in tema chiaro e scuro, e percorsa **solo da tastiera**: Coda, Le mie lavorazioni, Aziende, Nuova azienda, Scheda
azienda con Mansioni, Lavorazione segnalazione. **0 violazioni** a fine sprint.

### Difetti trovati e corretti durante lo sprint (prima del merge)

| Difetto | Dove | Come è emerso | Correzione |
|---|---|---|---|
| La pagina scorreva di lato su mobile | Tabelle con pulsanti (coda) | Verifica in browser a 320 px | Testi nascosti per gli screen reader contenuti nella regione della tabella |
| Lo screen reader leggeva "Prendi in caricola segnalazione…" | Pulsanti di riga | Test sul nome accessibile | Spazio come nodo di testo separato (stessa soluzione delle etichette di US-201) |
| Messaggio "presso Demo Logistica S.r.l.." | Esito della proposta | Verifica in browser | Testo riformulato |
| Test frontend instabile (timeout) | Modulo del beneficiario con axe | CI locale con più test in parallelo | Timeout dei test a 15 s |

Nessun difetto segnalato dopo i deploy.

### Debito tecnico e note operative

- **Ordine delle migrazioni tra PR parallele**: V11 (#22) e V12 (#24) dovevano arrivare insieme sul server, altrimenti
  Flyway avrebbe rifiutato il deploy successivo. Gestito avvisando nella PR. *Proposta*: per le PR con migrazioni,
  numerazione assegnata al momento del merge (rinomino io il file se serve) — vedi §7.
- **Conflitti tra PR parallele** (#23/#24 su menu, rotte, testi): risolti con un rebase da parte del team.
- **Ambiente locale**: dopo un riavvio il Postgres di sviluppo era fermo e gli script di verifica cancellati; ripristinati.
  Nessun impatto sul prodotto.

## 5. Decisioni prese durante lo sprint

Già in vigore, da confermare o correggere in review:

| # | Decisione | Presa da | Dove |
|---|---|---|---|
| D1 | Compatibilità: default zona di residenza + mansione richiesta, allargabili | PO (planning) | US-601 |
| D2 | Audit: per i beneficiari solo i nomi dei campi, mai i valori; altrove prima/dopo completi | PO (planning) | EN-2 |
| D3 | Una PR per storia su `main`; in cascata solo se necessario | PO (planning) | processo |
| D4 | Pagina **"Le mie lavorazioni"** (non prevista dalla storia): senza, l'operatore non ritrova le segnalazioni prese | Team | US-402 |
| D5 | Le aziende e le mansioni si **disattivano/ritirano**, mai si cancellano; una mansione bloccata non si ritira | Team (dal criterio di US-501) | US-501, US-502 |
| D6 | **Conferma esplicita** prima della proposta all'azienda (blocca una posizione reale) | Team | US-602 |
| D7 | Abbina solo l'operatore che ha in carico la segnalazione; l'**ADMIN** può sempre | Team | US-602 |
| D8 | La proposta registra l'evento `PROPOSTA_INVIATA` nello **storico dell'azienda** (oltre all'audit) | Team (dal brief) | US-602 |
| D9 | P.IVA validata solo come **11 cifre** (nessun controllo della cifra di controllo) | Team (dal criterio) | US-501 |

## 6. Stato del prodotto rispetto alla roadmap

**Rilascio R1** — *"il tutor registra e segnala un beneficiario; il Call Center lo prende in carico e lo abbina a una
mansione"*: **flusso funzionante end-to-end**. Restano 7 voci per chiudere R1 (18 punti):

| ID | Storia | Punti | Perché serve |
|---|---|---|---|
| US-303 | I miei ticket | 3 | Oggi il Tutor **non vede** le segnalazioni inviate né il loro stato |
| US-305 | Dettaglio e cronologia del ticket | 3 | Mostrare cosa è successo e chi l'ha fatto (dati già registrati) |
| US-202 | I miei beneficiari | 3 | Ritrovare i beneficiari registrati |
| US-203 | Modifica beneficiario | 2 | Correggere i dati |
| US-204 | Visibilità dei beneficiari per progetto | 3 | Regole di accesso decise con Q11 |
| US-304 | Ticket dei miei progetti | 3 | Coordinamento tra tutor dello stesso progetto (Q11) |
| US-1301 | Verifica della PWA online | 1 | A cura del PO (smartphone) |

**Lacuna principale emersa:** il flusso è oggi "a senso unico" — il Call Center lavora, ma il Tutor non ha ancora
modo di seguire le proprie segnalazioni. È la priorità naturale dello Sprint 3.

**Product backlog:** 60 elementi, 218 punti; completati 12 elementi (46 punti) tra Sprint 1 e 2.

## 7. Da decidere (proposte del team)

### 7.1 Dati personali dei referenti aziendali nell'audit
Oggi, per le aziende, l'audit registra prima/dopo **completi**, compresi nome, telefono ed email del **referente**
(persona fisica). È coerente con D2, ma sono dati personali e l'audit non si può cancellare.
*Proposta:* trattare referente, telefono ed email come i dati dei beneficiari (solo "campo modificato", senza valori).
Alternativa: lasciare così, se la cooperativa ritiene i contatti aziendali dati di lavoro non soggetti a cancellazione.

> ✅ **Deciso (PO, 04/10): proposta accettata.** Referente, telefono ed email delle aziende si registrano nell'audit come
> "campo modificato", senza valori. Nuova voce **EN-4** (1 punto, R1). Gli eventi già registrati sulla demo restano
> (l'audit non si modifica): contengono solo dati fittizi.

### 7.2 Rilascio o riassegnazione di una segnalazione presa in carico
Se l'operatore che ha preso in carico una segnalazione è assente, oggi solo l'ADMIN può abbinarla al suo posto; non c'è
un modo per "restituirla" alla coda o passarla a un collega.
*Proposta:* nuova storia (2 punti): "Rilascia" (torna in coda) e "Assegna a…" (solo ADMIN), entrambi con motivo e audit.

### 7.3 Disattivazione di un'azienda con mansioni bloccate
Oggi un'azienda si può disattivare anche se ha una mansione bloccata da una proposta in corso; la proposta resta
valida, ma l'azienda sparisce dalla ricerca.
*Proposta:* impedirla finché ha mansioni bloccate (messaggio con il numero della segnalazione), come per il ritiro
della mansione.

### 7.4 Cadenza degli sprint
Entrambi gli sprint si sono chiusi nei primi 1–2 giorni: il vincolo reale non è lo sviluppo ma il tempo di revisione,
merge e prova del PO.
*Proposta:* mantenere lo sprint di una settimana (ritmo delle decisioni e delle prove sul campo) ma **alzare l'impegno
a ~35 punti** con extra espliciti, e unire le PR a blocchi (es. due momenti al giorno). In alternativa, sprint più
brevi (3 giorni) con impegni da ~20 punti.

### 7.5 Migrazioni in PR parallele
*Proposta:* il team verifica il numero della migrazione dopo ogni merge e, se una PR aperta ne ha una "superata",
rinomina il file prima del merge; nella descrizione di ogni PR con migrazioni compare il numero.

### 7.6 Chiusura dello Sprint 2
Il lavoro è completo dal 02/10; formalmente lo sprint termina l'08/10.
*Proposta:* chiudere oggi lo Sprint 2 (come per lo Sprint 1) e pianificare lo Sprint 3 dal 05/10.

## 8. Proposta per lo Sprint 3 (da discutere in planning)

**Obiettivo proposto:** *il Tutor segue le proprie segnalazioni e i propri beneficiari, con la cronologia completa;
R1 chiuso.*

| ID | Storia | Punti |
|---|---|---|
| US-303 | I miei ticket | 3 |
| US-305 | Dettaglio e cronologia del ticket (per Tutor e Call Center, letta dall'audit) | 3 |
| US-202 | I miei beneficiari | 3 |
| US-203 | Modifica beneficiario (con audit "solo campi") | 2 |
| US-204 | Visibilità dei beneficiari per progetto | 3 |
| US-304 | Ticket dei miei progetti | 3 |
| *nuova* | Rilascio/riassegnazione della presa in carico (§7.2), se approvata | 2 |
| *nuova* | Blocco della disattivazione di aziende con mansioni bloccate (§7.3), se approvata | 1 |
| | **Totale** | **17–20** |

Con l'impegno a ~35 punti (§7.4), si possono anticipare le prime storie di **R2**: US-107 *Calendario del progetto* (3),
US-703 *Calendario dei giorni lavorativi* (3), US-701 *Accettazione della proposta* (3), così che il ciclo
riparta dal lato del Tutor subito dopo la proposta all'azienda.

## 9. Azioni a carico del PO

- [ ] Provare la demo guidata (§3) e segnalare osservazioni.
- [ ] US-1301: verificare installazione e funzionamento offline della PWA su Android e iOS.
- [ ] Decidere i punti §7.1–§7.6.
- [ ] Q5: modelli Word reali e modalità di modifica dei modelli (servono per R3).
