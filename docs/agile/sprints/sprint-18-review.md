# Sprint 18 — Review, retrospettiva e planning

**Sprint:** 26 – 28 novembre 2026, concluso il 26/11 · **Review:** 26/11/2026 · **Partecipanti:** Mauro Bernardi
(Product Owner), team di sviluppo (Claude Code) · **Produzione:** https://portalenexus.it, versione `119cb88`, ancora
pre-produzione · **Collaudo:** [docs/test/collaudo.md](../../test/collaudo.md): tutte le prove dello sprint riuscite
(T-35 … T-39, 24 prove).

> Documento unico e asincrono: review (§1–§6), retrospettiva (§7), decisioni da prendere (§8) e proposta di planning
> dello Sprint 19 (§9). La PR resta in **bozza** finché non abbiamo finito di discuterne (A12). Le storie sono già
> chiuse e le change archiviate nelle PR di chiusura (#211, #213, #215, #217); questa PR chiude lo sprint nel suo file.

---

## 1. Sintesi

| | |
|---|---|
| **Obiettivo dello sprint** | La scheda dell'azienda dice quanto è affidabile e interessata al progetto, come si sono trovati i tirocinanti e se è una nave scuola; il Tutor vede le aziende. |
| **Esito** | ✅ **Raggiunto.** Le cinque storie sono in produzione dal 26/11, lo stesso giorno della conferma del planning. Ognuna è passata dal collaudo sul tuo PC prima del merge, e sei tue richieste fatte durante il collaudo sono entrate nella stessa PR. |
| **Impegno / completato** | 18 punti al planning; **23** dopo i cambi del 26/11 (+US-1312 dal collaudo, +US-511 anticipata, −US-1311 allo Sprint 19); completati **23** (5 storie su 5) |
| **Extra** | Avviso SSH «post-quantum» risolto sul server (§4); conversione dei simboli del database attuale verificata sui dati veri, solo con conteggi |
| **Velocity** | Sprint 1: 21 · 2: 25 · 3: 21 · 4: 24 · 5: 21 · 6: 20 · 7: 31 · 8: 23 · 9: 25 · 10: 22 · 11: 15 · 12: 21 · 13: 22 · 14: 29 · 15: 27 · 16: 18 · 17: 29 · 18: **23** · media 23,2 |
| **PR unite** | 11: #207 (review dello Sprint 17), #208 (US-514), #209, #210 (US-1312), #211 (chiusura di US-514 e US-1312), #212, #213 (US-511), #214, #215 (US-1104), #216, #217 (US-515). Aperte: nessuna |
| **Migrazioni del database** | 4: **V52** (stelle e pollici, simboli tolti dai nomi), **V53** (zona dell'azienda), **V54** (valutazione nel report di restituzione), **V55** (nave scuola, avvisi verso un'azienda). La prossima è **V56** |
| **Deploy** | 4, tutti il 26/11: `1603fcc` (10:40, US-514 e US-1312), `3a64364` (16:44, US-511), `5095004` (17:57, US-1104), `119cb88` (23:05, US-515) |
| **CI** | Verde in tutte le PR unite; nella #217 un errore di Maven Central, sparito rilanciando il controllo (§4) |

## 2. Cosa è stato consegnato

| ID | Storia | Punti | PR | In una frase |
|---|---|---|---|---|
| US-514 | Affidabilità e interesse al progetto dell'azienda | 5 | [#208](https://github.com/maubernardi/nexus/pull/208), [#211](https://github.com/maubernardi/nexus/pull/211) | Da 1 a 4 stelle e uno o due pollici, scelti con un clic nella scheda, con due filtri nell'elenco. I simboli `$` e `£` all'inizio dei nomi importati sono diventati stelle e pollici, e i nomi sono puliti. |
| US-1312 | Tema chiaro o scuro a scelta e intestazione fissa delle schede | 3 | [#209](https://github.com/maubernardi/nexus/pull/209), [#210](https://github.com/maubernardi/nexus/pull/210), [#211](https://github.com/maubernardi/nexus/pull/211) | Il pulsante 🌙/☀️ nella barra in alto sceglie il tema. Titolo e ritorno all'elenco restano in cima mentre la scheda scorre, e il ritorno in fondo non c'è più. |
| US-511 | Zona dell'azienda e aziende per il Tutor | 5 | [#212](https://github.com/maubernardi/nexus/pull/212), [#213](https://github.com/maubernardi/nexus/pull/213) | L'azienda ha una zona del catalogo. Il Tutor vede le aziende attive con i soli dati essenziali e le filtra per zona e categoria. In tutti e due gli elenchi c'è «Azzera i filtri». |
| US-1104 | Valutazione dell'azienda alla fine del tirocinio | 5 | [#214](https://github.com/maubernardi/nexus/pull/214), [#215](https://github.com/maubernardi/nexus/pull/215) | Nel report di restituzione il Tutor dà tre voti con le faccine, più una nota facoltativa. La scheda dell'azienda mostra la media e l'elenco a pagine, con il nome del Tutor. |
| US-515 | Azienda «nave scuola» | 5 | [#216](https://github.com/maubernardi/nexus/pull/216), [#217](https://github.com/maubernardi/nexus/pull/217) | L'operatore la segna nella scheda; un bollino oro con la nave la segnala ovunque si sceglie l'azienda, tranne nella bacheca dei Tutor. Il Tutor la propone nel report, e l'operatore riceve un avviso che porta alla scheda dell'azienda. |

## 3. Cosa vedi in produzione

La produzione è ancora una pre-produzione (review dello Sprint 17, §8.3). Le storie le hai collaudate sul PC con dati
finti; in produzione c'è stato un solo controllo, fatto di conteggi.

- **T-35.1 … T-35.5** affidabilità e interesse al progetto, sul PC; **T-35.6** in produzione, dopo il deploy
  `1603fcc`: 271 aziende con le stelle, 90 con i pollici, nessun nome rimasto con i simboli. ✅
- **T-36** tema a scelta e intestazione fissa, sul PC. ✅
- **T-37** zona dell'azienda, aziende per il Tutor e «Azzera i filtri», sul PC. ✅
- **T-38** valutazione dell'azienda, sul PC. ✅
- **T-39** nave scuola, con il bollino e la colonna della ragione sociale (T-39.6 e T-39.7, dal collaudo), sul PC. ✅

Le prove sono 182, e ne sono spuntate 78. Le 103 delle sezioni 1–23 restano come storico (review dello Sprint 17,
§8.3).

## 4. Qualità

### Test automatici

| | Fine Sprint 17 | Fine Sprint 18 |
|---|---|---|
| Backend – unit | 246 | 254 |
| Backend – integrazione (PostgreSQL e Keycloak veri) | 575 | 596 |
| Frontend – componenti (tutti con controllo axe) | 324 | 370 |
| Prove da capo a fondo nella CI, a ogni PR | backup e ripristino, aggiornamento di PostgreSQL, ambiente di collaudo, profilo utente di Keycloak | uguali |

Da segnalare:
- **Ogni vincolo nuovo è nel database e ha il suo test**:
  - stelle da 1 a 4, pollici 1 o 2;
  - un nome che non inizia con i simboli del database attuale;
  - la zona e l'azienda che esistono davvero;
  - voti da 1 a 5;
  - l'azienda di un avviso.
- **La conversione dei simboli è stata provata sui dati veri** prima della migrazione, contando soltanto:
  - stelle 143 + 107 + 20 + 1 (da 1 a 4);
  - pollici 89 + 1;
  - 343 nomi da pulire, nessuno rimasto vuoto.

  In produzione T-35.6 ha dato gli stessi numeri.
- **La bacheca dei Tutor resta anonima**: un test controlla che la nave scuola non ci arrivi, come già il nome
  dell'azienda.

### Sicurezza e privacy

- **Minimizzazione per il Tutor** (US-511): nella lista e nella scheda delle aziende il Tutor vede nome, categoria,
  zona, indirizzo, stelle, pollici, nave scuola e valutazioni. Non vede partita IVA, recapiti, referenti, note né
  storico, e un test lo controlla.
- **Valutazioni dei tirocini** (US-1104):
  - nessun dato del beneficiario, nemmeno il nome;
  - la nota è facoltativa e l'aiuto chiede di non scriverci dati personali;
  - i voti entrano nell'audit, la nota no;
  - il nome del Tutor è visibile, come hai deciso.
- **Avviso della nave scuola** (US-515): nell'email c'è solo il numero della segnalazione, non il nome dell'azienda,
  perché una ditta individuale porta il nome di una persona.
- **SSH «post-quantum»**: il 26/11 hai attivato il sottocriterio `DEFAULT:PQ` sul server. Ho controllato
  dall'esterno, senza login: il server offre per primo `mlkem768x25519-sha256`. Gli algoritmi di prima restano, e le
  chiavi dell'host sono le stesse (review dello Sprint 17, §8.5).
- **Dependabot**: nessun avviso aperto, nessuna PR aperta.

### Difetti

| Difetto | Dove | Come è emerso | Correzione |
|---|---|---|---|
| La #209 era impilata sulla #208 ed è finita nel branch di US-514 invece che su `main` | Processo | Merge delle due PR una dopo l'altra (26/11) | Stessi commit nella #210, verso `main`; PR sempre su `main` (A45) |
| Il ritorno all'elenco in fondo alle schede si vedeva male | Schede | Tuo collaudo di US-514 | Intestazione fissa in cima (US-1312) |
| Le prove chiedevano F12 per il tema scuro, che non funziona | Collaudo | Tuo collaudo di US-514 | Pulsante 🌙/☀️ e prove riscritte |
| Su uno smartphone stretto (320 px) menu e campanella si sovrapponevano | US-1312 | Controllo in Chrome prima della PR | Logo senza la scritta sotto i 375 px (#210) |
| Il Tutor non vedeva le aziende, anche se il planning lo dava per fatto | Aziende | Tu (26/11) | US-511 anticipata (#212) |
| Le categorie merceologiche rispondevano 403 al Tutor | US-511 | Test prima della PR | Catalogo leggibile dal Tutor (#212) |
| La nota della valutazione compariva solo con voti bassi; il testo della sezione era forzato | US-1104 | Tuo collaudo T-38 | Nota sempre presente, testo neutro (#214) |
| Il simbolo della nave scuola era troppo poco evidente | US-515 | Tuo collaudo T-39 | Bollino oro con la nave verde, più grande e staccato dal nome (#216) |
| Nell'elenco delle aziende la colonna «Ragione sociale» era la più stretta | Aziende | Tuo collaudo T-39 | Tolte Telefono ed Email; colonna del nome per il 95% dei nomi veri (#216) |
| La #215 e la #216 sarebbero andate in conflitto su `collaudo.md` | Processo | Controllo prima di dirti di unirle | La riga in fondo l'aggiorna una sola PR (A46) |
| Maven Central non trovava un plugin dei test | CI della #217 | CI | Rilanciato il solo controllo; problema del servizio, non nostro |

## 5. Decisioni prese durante lo sprint

| # | Decisione | Presa da | Dove |
|---|---|---|---|
| D1 | Stelle e pollici dai simboli del database attuale: `$` = una stella (cinque `$` = 4 stelle), `£` = un pollice; `&` e `^` iniziali sono refusi. Subito, anche nel collaudo | PO (25 – 26/11) | US-514 |
| D2 | Tre voti con le faccine, una sola nota facoltativa, media delle medie, elenco a pagine; anche il Tutor vede le valutazioni, con il nome del collega | PO (25 – 26/11) | US-1104 |
| D3 | Nuova storia: l'azienda «nave scuola», proposta dal Tutor e segnata dall'operatore | PO (25/11) | US-515 |
| D4 | Tema scelto con il pulsante 🌙/☀️; intestazione delle schede fissa, titolo compreso; nelle finestre basse scorre con la pagina (scelta A) | PO (26/11) | US-1312 |
| D5 | US-511 anticipata, US-1311 allo Sprint 19 | PO (26/11) | Planning |
| D6 | «Azzera i filtri» negli elenchi delle aziende | PO (26/11) | US-511 |
| D7 | La nota della valutazione c'è sempre ed è facoltativa; nella sezione un testo neutro | PO (26/11) | US-1104 |
| D8 | Nave scuola: un bollino ben visibile dopo il nome, in tutti gli elenchi | PO (26/11) | US-515 |
| D9 | Nell'elenco delle aziende niente Telefono ed Email; la ragione sociale va a capo solo nei casi eccezionali | PO (26/11) | US-515 |
| D10 | Lista del Tutor con i soli dati essenziali | Team (26/11) | US-511 |
| D11 | L'avviso della nave scuola porta alla scheda dell'azienda; nell'email solo il numero della segnalazione | Team (26/11) | US-515 |
| D12 | Colonna della ragione sociale larga per i nomi fino a 42 caratteri, il 95% di quelli veri | Team (26/11) | US-515 |
| D13 | `DEFAULT:PQ` sul server | PO (26/11) | Review dello Sprint 17, §8.5 |

## 6. Stato del prodotto rispetto alla roadmap

**R1** — sviluppo completo; resta **US-1301** (PWA su smartphone, a cura tua: prova T-10.1).

**R2, R3** — ✅ complete.

**R4** — *produzione*: **237 punti su 248**. Restano:

| Storia | Punti | Quando |
|---|---|---|
| US-1311 Avvisi affidabili su sito e backup | 3 | Sprint 19, dopo la scelta del servizio (§8.2) |
| US-1305 Revisione UX/UI | 8 (da rivedere) | Dopo le prime settimane d'uso reale (review dello Sprint 15, §8.7) |

**Senza release**: US-507 e US-512, 10 punti, il resto della richiesta di contatto (Ready).

**Product backlog:** 111 elementi, 450 punti; completati 104 (428 punti). Restano 5 storie, per 22 punti, più 2 assorbite
da altre.

## 7. Retrospettiva

**Cosa ha funzionato**
- **Il collaudo prima del merge è diventato il modo di lavorare.** Sei tue richieste sono nate provando le storie:
  - l'intestazione fissa;
  - «Azzera i filtri»;
  - la nota sempre presente;
  - il testo neutro;
  - il bollino;
  - la colonna del nome.

  Sono entrate nella stessa PR prima del merge, e in produzione è arrivata solo la versione già corretta.
- **Cinque storie, quattro migrazioni e quattro deploy in un giorno**, con la CI verde e senza ritorni indietro.
- **I dati veri sono stati usati solo come conteggi**: la conversione dei simboli e la larghezza della colonna del nome
  sono state decise su quei numeri, senza mai mostrare un nome.
- **L'avviso SSH è stato risolto senza che io entrassi nel server**: ti ho dato i comandi con la sessione di riserva, e
  il risultato l'ho controllato da fuori.

**Cosa non ha funzionato**
- **Tre scelte visive le hai dovute correggere tu**: il ritorno all'elenco in fondo, il simbolo della nave scuola
  troppo piccolo e la colonna del nome troppo stretta. Le schermate le controllavo, ma con i nomi corti dei dati finti
  e senza chiedermi se un elemento si vede a colpo d'occhio.
- **Una PR impilata è finita nel branch sbagliato** (#209). È servita una PR in più, e GitHub non lo segnala.
- **Il planning dava per fatto che il Tutor vedesse le aziende**, ma la lista arrivava solo con US-511, prevista nello
  Sprint 19. Te ne sei accorto tu.
- **La ricerca dei dati che non si correggono dall'app (A42) non è partita**, perché le storie sulle aziende hanno
  avuto la precedenza.
- **Tutto lo sprint in un giorno** vuol dire, per te, collaudi e merge a raffica fino a tarda sera. Non è un problema
  finché lo scegli tu, ma conviene saperlo quando si decide il carico.

**Azioni**

| # | Azione | Chi |
|---|---|---|
| A45 | PR sempre con base `main`, mai impilate; se una dipende da un'altra, lo scrivo nella PR | Team |
| A46 | Prima di dirti di unire più PR, controllo che si uniscano senza conflitti in qualunque ordine | Team |
| A47 | Ogni elemento nuovo negli elenchi lo provo con testi lunghi come quelli veri, misurati sul database attuale solo come lunghezze, e controllo che si veda a colpo d'occhio, in chiaro e in scuro | Team |
| A48 | Al planning controllo che quello che una decisione dà per fatto esista già, o sia nello stesso sprint | Team |
| A42 | (riportata) La ricerca dei dati che non si correggono dall'app, nello Sprint 19 | Team |

## 8. Da decidere

### 8.1 Chiusura dello Sprint 18
*Proposta:* Sprint 18 chiuso con velocity **23**: le cinque storie sono fatte, collaudate e in produzione. Questa PR
aggiorna il file dello sprint.

### 8.2 Il servizio degli avvisi (US-1311)
US-1311 non è Ready finché non scegli il servizio. Riassumo il confronto del 26/11, fatto sulle pagine ufficiali di
prezzi, condizioni e privacy:

| | Better Stack | Healthchecks.io | UptimeRobot |
|---|---|---|---|
| **Gratis** | 10 controlli del sito e 10 segnali, ogni 3 minuti | 20 segnali; non controlla il sito | 50 controlli del sito, ogni 5 minuti |
| **Controllo dell'API (risposta 401)** | Probabile, ma non scritto per il piano gratuito | No | No, solo a pagamento |
| **Segnale del backup entro le 9** | Sì, ma approssimato (24 ore dall'ultimo segnale più un margine) | Sì, preciso (ogni giorno alle 3, ora di Roma, con un margine) | No, solo a pagamento |
| **Uso da parte della cooperativa** | Ambiguo: «gratis per progetti personali» | «Tollerato»; per le non profit anche il piano Business, su richiesta | Sì, scritto |
| **Sede e dati** | Società USA, dati in UE, DPA pubblicato | Lettonia, server in Germania, nessun DPA | Slovacchia, DPA su richiesta |

| | Cosa | A favore | Contro |
|---|---|---|---|
| **A** | Better Stack da solo | Un solo account | Due dubbi: «progetti personali» e il 401 nel piano gratuito |
| **B** | UptimeRobot per il sito, Healthchecks.io per il backup | Condizioni chiare, scadenza delle 9 precisa | Due account. Senza il controllo del 401 serve un indirizzo di NEXUS che risponda solo «attivo», senza dati, per dire che il backend è su: si fa dentro US-1311 |

*Proposta:* **B**. Se la cooperativa è una non profit, conviene chiedere a Healthchecks.io il piano gratuito per le
non profit. In tutti e due i casi gli account li crei tu, e gli indirizzi dei segnali vanno solo nel `.env` del server.

### 8.3 Azzeramento della pre-produzione
Hai deciso che portalenexus.it, per ora, è una pre-produzione da azzerare più avanti (review dello Sprint 17, §8.3).
L'azzeramento tocca tre cose:
- il database: tutti i dati, compresi quelli nuovi di questo sprint (valutazioni, zone, nave scuola);
- gli utenti di Keycloak;
- una nuova importazione dal file Access. Applica già le regole di US-514 sui simboli dei nomi.

*Proposta:* una storia **US-1313 «Azzeramento della pre-produzione»**, circa 5 punti, R4: uno script provato nella CI
come quello del passaggio, con il backup prima e i conteggi dopo. La metto nel backlog subito, e la pianifichiamo
quando fissi la data. Ti chiedo anche se, oltre a te, qualcuno usa già la pre-produzione: in quel caso l'azzeramento
va concordato con loro.

### 8.4 Contenuto dello Sprint 19
*Proposta:* il resto della richiesta di contatto, US-507 e US-512, e gli avvisi, US-1311, se scegli il servizio:
**13 punti** in tutto (§9). Sotto la media, per lasciare spazio a quello che arriverà dall'uso, come negli ultimi due
sprint. Nello sprint anche la ricerca A42, senza punti.

## 9. Proposta per lo Sprint 19

**Date:** tre giorni dalla conferma del planning, indicativamente 27 – 29 novembre 2026. **Obiettivo proposto:** *il
Tutor chiede al Call Center di contattare un'azienda e segue la richiesta fino all'esito; gli avvisi su sito e backup
arrivano in tempo.*

| Ordine | ID | Storia | Punti |
|---|---|---|---|
| 1 | US-507 | Richiesta di contatto di un'azienda dal Tutor | 5 |
| 2 | US-512 | Tentativi di contatto ed esito della richiesta | 5 |
| 3 | US-1311 | Avvisi affidabili su sito e backup (§8.2) | 3 |
| | | **Totale** | **13** |

Prossima migrazione: **V56**; probabilmente una per ciascuna delle prime due storie. Ogni storia passa dal collaudo sul
tuo PC prima del merge (EN-11), e i deploy li decidi tu.

## 10. Azioni a carico del PO

- [x] Attivare il sottocriterio `PQ` di SSH sul server (26/11).
- [ ] Decidere i punti §8.1 … §8.4.
- [ ] Scegliere il servizio degli avvisi e creare gli account (§8.2).
- [ ] Unire questa PR dopo le decisioni.
- [ ] Gli account degli utenti, dopo l'azzeramento (§8.3).
- [ ] Il blocco automatico dello schermo dei PC del Call Center, dopo pochi minuti, con chi gestisce i PC (US-1310).
- [ ] I 30 giorni dei backup nel registro dei trattamenti, con il DPO (dallo Sprint 16).
- [ ] US-1301: la prova T-10.1 della PWA sullo smartphone.
