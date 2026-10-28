# Sprint 5 — Review, retrospettiva e planning

**Sprint:** 27 – 29 ottobre 2026 (planning 27/10) · **Review:** 27/10/2026 · **Partecipanti:** Mauro Bernardi (Product
Owner), team di sviluppo (Claude Code) · **Demo:** https://portalenexus.it, versione `19bd629` (tutto lo Sprint 5) ·
**Collaudo:** [docs/test/collaudo.md](../../test/collaudo.md).

> Documento unico e asincrono: review (§1–§6), retrospettiva (§7), decisioni da prendere (§8) e proposta di planning
> dello Sprint 6 (§9). Ne discutiamo un punto alla volta.

---

## 1. Sintesi

| | |
|---|---|
| **Obiettivo dello sprint** | Dopo l'accettazione il percorso prosegue: promemoria e scadenza del timer, rifiuto della proposta, esito dell'appuntamento; Tutor e operatori ricevono le notifiche in app. |
| **Esito** | ✅ **Raggiunto.** Il ciclo della proposta è completo: accettazione o rifiuto, appuntamento fissato o fallito, promemoria e scadenza automatici, segnalazioni riaperte e di nuovo abbinabili; ogni passaggio avvisa la controparte. |
| **Impegno / completato** | 21 punti / **21 punti** (7 storie su 7), tutte unite il 27/10 |
| **Fuori impegno** | Documento di collaudo passo per passo, richiesto dal PO ([#58](https://github.com/maubernardi/nexus/pull/58)) |
| **Velocity** | Sprint 1: 21 · 2: 25 · 3: 21 · 4: 24 · 5: **21** · media 22,4 |
| **PR unite** | 10 (#52–#61): 7 di funzionalità, 3 di documentazione |
| **Migrazioni del database** | V18 (notifiche; l'avviso provvisorio "tipologia cambiata" diventa notifica) |
| **Deploy della demo** | 27/10: `27251c8` (fino a US-702) e `19bd629` (Sprint 5 completo) |

## 2. Cosa è stato consegnato

| ID | Storia | Punti | PR | In una frase |
|---|---|---|---|---|
| US-1201 | Notifiche in app | 5 | [#53](https://github.com/maubernardi/nexus/pull/53) | Campanella con il numero di non lette (annunciato agli screen reader) e pagina "Notifiche"; ogni passaggio avvisa la controparte. |
| US-801 | Appuntamento preso | 2 | [#54](https://github.com/maubernardi/nexus/pull/54) | Il Tutor registra l'appuntamento con una nota: il timer si ferma. |
| US-802 | Appuntamento fallito | 3 | [#55](https://github.com/maubernardi/nexus/pull/55) | Causa dal menu e nota; segnalazione riaperta allo stesso operatore, in cima a "In carico a me", di nuovo abbinabile; nessuna esclusione automatica. |
| US-702 | Rifiuto della proposta | 3 | [#56](https://github.com/maubernardi/nexus/pull/56) | Motivo obbligatorio; mansione libera e azienda esclusa per la segnalazione (Q3). |
| US-704 | Promemoria al 4° giorno | 3 | [#57](https://github.com/maubernardi/nexus/pull/57) | Prima attività pianificata del backend: controllo ogni ora, promemoria unico al Tutor. |
| US-705 | Scadenza del timer | 3 | [#59](https://github.com/maubernardi/nexus/pull/59) | Alla fine del 7° giorno lavorativo la segnalazione si riapre e la mansione si libera; solo il sistema può applicarla. |
| US-603 | Esclusione di un'azienda | 2 | [#60](https://github.com/maubernardi/nexus/pull/60) | Dalla finestra "Dettagli", con motivo; l'azienda sparisce dalle compatibili; elenco delle escluse. |

### Il ciclo della proposta dopo lo Sprint 5

```
Call Center: Proponi ─► Proposta all'azienda ─► (Tutor) Rifiuta ──────────────┐ azienda esclusa
                              │                                               ▼
                              └► (Tutor) Accetta ─► Proposta accolta ─► scadenza (Sistema) ─► Riaperto
                                   timer 7 gg lav.    │  promemoria al 4° giorno (Sistema)      ▲   │
                                                      └► Appuntamento fissato ─► fallito ───────┘   │
                                                                                                    ▼
                                                    Call Center: in cima a "In carico a me", nuova proposta
```

## 3. Demo guidata

Le prove passo per passo sono nel **collaudo** (sezioni 4, 5, 7 e 9): in particolare T-5.1–T-5.7 (rifiuto,
accettazione, appuntamento fissato e fallito, esclusione), T-7.1–T-7.3 (notifiche) e T-9.2 (scadenza: sulla demo la
segnalazione n. 6 dei dati demo è scaduta al primo controllo dopo il deploy).

## 4. Qualità

### Test automatici

| | Fine Sprint 4 | Fine Sprint 5 |
|---|---|---|
| Backend – unit | 52 | 55 |
| Backend – integrazione (PostgreSQL reale) | 185 | 201 |
| Frontend – componenti (tutti con controllo axe) | 93 | 101 |

CI verde su tutte le PR. Il controllo orario del timer è stato provato anche **nell'applicazione vera** (controllo ogni
20 secondi): promemoria unico e scadenza della n. 6 dei dati demo, con le notifiche.

### Accessibilità (WCAG 2.2 AA)

Verificate in browser reale, a 320 e 1280 px, in chiaro e scuro: campanella (44×44 px, nome con il numero), pagina
"Notifiche", azioni del Tutor sulla proposta (conferme con focus, esiti annunciati), esclusione nella finestra
"Dettagli". **0 violazioni**, nessuno scorrimento laterale.

### Difetti trovati e corretti durante lo sprint (prima del merge)

| Difetto | Dove | Come è emerso | Correzione |
|---|---|---|---|
| L'esito "Appuntamento registrato" non compariva: il pulsante spariva con il ricaricamento della segnalazione | Dettaglio (US-801) | Verifica in browser | Azione con `mutateAsync`; il test ora simula il ricaricamento nel nuovo stato |
| "Nuova:" attaccato al testo della notifica | Pagina "Notifiche" | Test automatico | Spazio come nodo di testo |
| Promemoria inviato anche a una segnalazione già scaduta | Timer (US-704) | Prova nell'applicazione vera | Con US-705 il controllo riapre prima le scadute |

### Note operative

- **Merge parziale della #52**: la review dello Sprint 4 è stata unita con la sola decisione §8.1; le altre decisioni e
  il planning dello Sprint 5 sono stati recuperati nella #61. Da ora, dopo ogni merge, il team verifica che tutti i
  commit del branch siano su `main`.

## 5. Decisioni prese durante lo sprint

| # | Decisione | Presa da | Dove |
|---|---|---|---|
| D1 | Accetta, rifiuta e registra l'appuntamento solo il Tutor della segnalazione | PO (27/10) | US-701, US-702, US-801, US-802 |
| D2 | Notifiche solo in app; testo composto dall'interfaccia, nessun dato personale; pagina invece di un menu a tendina | PO (§8.3) + Team | US-1201 |
| D3 | Segnalazioni riaperte allo stesso operatore, in cima a "In carico a me"; nessuna esclusione automatica dopo un appuntamento fallito | PO (§8.2) | US-802, US-705 |
| D4 | Cause dell'appuntamento fallito (voci provvisorie) e nota obbligatoria con "Altro" | PO (§8.2) | US-802 |
| D5 | Controllo del timer ogni ora; promemoria e scadenza applicati dal **sistema** (transizioni senza ruoli, mai da un utente) | PO (planning) + Team | US-704, US-705 |
| D6 | Nel controllo prima le scadenze, poi i promemoria | Team | US-705 |
| D7 | Un'azienda si esclude solo su segnalazioni in carico, in lavorazione o riaperte (non durante una proposta) | Team | US-603 |
| D8 | Collaudo passo per passo, aggiornato a ogni storia (Definition of Done punto 8) | PO | processo |

## 6. Stato del prodotto rispetto alla roadmap

**R1** — sviluppo completo; resta **US-1301** (PWA su smartphone, a cura del PO: prova T-10.1 del collaudo).

**R2** — *proposta, timer, appuntamenti, bacheca e fast-track*: fatti 35 punti su 67. Restano 9 storie, **32 punti**,
tutte su **bacheca e fast-track** più i cataloghi:

| Gruppo | Storie | Punti |
|---|---|---|
| Bacheca | US-902 gestione (5), US-904 annuncio da mansione libera (3), US-901 bacheca per il Tutor (3), US-803 rilancio dopo l'appuntamento fallito (5), US-903 sblocco a tutti dopo 7 giorni (2) | 18 |
| Fast-track | US-306 segnalazione speciale dalla bacheca (3), US-403 approvazione (5), US-404 rifiuto (3) | 11 |
| Cataloghi | US-106 zone e tipologie per l'ADMIN (3) | 3 |

**Product backlog:** 65 elementi attivi, 231 punti; completati 35 elementi (112 punti) negli Sprint 1–5.

## 7. Retrospettiva

**Cosa ha funzionato**
- Sette storie in una giornata, ciascuna con PR, change, test e verifica in browser: il ciclo della proposta è completo.
- La prova delle attività pianificate **nell'applicazione vera** ha trovato un caso (promemoria a una scaduta) che i
  test non coprivano.
- Il collaudo dà al PO una lista concreta da spuntare; le azioni A1 (deploy) e A2 (prova di archiviazione) sono stabili.

**Cosa non ha funzionato**
- Il merge parziale della #52: decisioni e planning sono restati fuori da `main` per una giornata.
- Un difetto (esito che spariva con il pulsante) è passato dai test unitari perché il dato simulato non cambiava dopo
  l'azione.
- Le azioni A5 (note operative del 25/10) e A7 (preparazione comune dei test) non sono state fatte.

**Azioni**

| # | Azione | Chi |
|---|---|---|
| A8 | Dopo ogni merge verificare che non restino commit del branch fuori da `main` | Team |
| A9 | Nei test delle azioni, simulare sempre il ricaricamento della segnalazione nel nuovo stato | Team |
| A5 | (riportata) Riprendere le note operative del 25/10 quando il PO le completa | PO + team |
| A7 | (riportata) Preparazione comune dei test per segnalazioni in un certo stato: da fare all'inizio dello Sprint 6 | Team |

## 8. Da decidere

### 8.1 Rilancio in bacheca dopo l'appuntamento fallito (US-803)
Il brief prevede il "fallimento con rilancio in bacheca". *Proposta:* nel modulo dell'appuntamento fallito (US-802) il
Tutor trova la casella **"Chiedi il rilancio in bacheca"**: nasce una **bozza** di annuncio precompilata dalla mansione,
riservata al progetto della segnalazione, e il Call Center riceve una notifica; pubblicarla resta una scelta del Call
Center (US-902).

> ✅ **Deciso (PO, 28/10): proposta accettata.** Nel modulo dell'appuntamento fallito la casella "Chiedi il rilancio in
> bacheca" crea una **bozza** precompilata dalla mansione, riservata al progetto della segnalazione e collegata a essa;
> il Call Center riceve una notifica e decide se pubblicarla (US-902). Criterio aggiunto a US-803.

### 8.2 Cosa vede il Tutor di un annuncio (US-901)
Il nome dell'azienda non arriva mai al Tutor. *Proposta:* titolo, tipologia, zona, fascia d'età, orario, durata, note
e numero dell'annuncio (#N); per i post riservati, l'indicazione "solo per il tuo progetto". Nessun contatto
dell'azienda.

> ✅ **Deciso (PO, 28/10): proposta modificata.** Per il timore che il nome dell'azienda compaia nel testo libero, il
> Tutor **non vede né il titolo né le note** dell'annuncio. Vede solo dati strutturati: numero (#N), tipologia, zona,
> fascia d'età, orario, durata e, per i post riservati, "solo per il tuo progetto". Titolo e note restano visibili a
> Call Center e ADMIN. Criterio aggiunto a US-901.

### 8.3 Note operative del 25/10 (A5)
Nel file `NoteOperative` ci sono tre note nuove (ordine per numero decrescente; nel Call Center solo "In carico a me" e
"Tutte"; stati come filtro a tasti), l'ultima incompleta. *Proposta:* le discutiamo all'inizio dello Sprint 6, prima
che il lavoro sulla bacheca tocchi la Home.

> ✅ **Deciso (PO, 28/10): proposta accettata.** Le note del 25/10 si discutono all'inizio dello Sprint 6, prima del
> lavoro sulla bacheca.

### 8.4 Chiusura dello Sprint 5
*Proposta:* chiudere lo Sprint 5 con questa review (velocity 21) e avviare lo Sprint 6 alla conferma del planning.

> ✅ **Deciso (PO, 28/10): proposta accettata.** Sprint 5 chiuso con velocity 21; lo Sprint 6 parte alla conferma del
> planning.

## 9. Proposta per lo Sprint 6 (3 giorni, ~20 punti)

**Obiettivo proposto:** *la bacheca è operativa: il Call Center pubblica annunci (da una mansione libera o dal rilancio
dopo un appuntamento fallito) e il Tutor li consulta senza vedere l'azienda.*

| Ordine | ID | Storia | Punti |
|---|---|---|---|
| 1 | US-902 | Gestione della bacheca (bozze, pubblicazione, archiviazione) | 5 |
| 2 | US-904 | Annuncio da una mansione libera | 3 |
| 3 | US-901 | Bacheca per il Tutor | 3 |
| 4 | US-803 | Rilancio in bacheca dopo l'appuntamento fallito | 5 |
| 5 | US-903 | Sblocco a tutti dopo 7 giorni | 2 |
| | | **Totale** | **18** |

**Extra**, se avanza tempo: US-306 *Segnalazione speciale dalla bacheca* (3), che apre il fast-track. Con lo Sprint 7
(US-403, US-404, US-106: 11 punti, più US-306 se non fatta) si chiude **R2**.

Dipende dalle decisioni §8.1 e §8.2. Per il PO: US-1301 (collaudo T-10.1) e le note operative (§8.3).

## 10. Azioni a carico del PO

- [ ] Eseguire il collaudo sulla versione `19bd629` (almeno sezioni 4, 5, 7, 9).
- [ ] US-1301: PWA su Android e iOS (collaudo T-10.1).
- [ ] Decidere i punti §8.1–§8.4 e confermare il planning (§9).
- [ ] Completare le note operative del 25/10 (§8.3).
