# Sprint 17 — Review, retrospettiva e planning

**Sprint:** 24 – 26 novembre 2026, concluso il 25/11 · **Review:** 25/11/2026 · **Partecipanti:** Mauro Bernardi
(Product Owner), team di sviluppo (Claude Code) · **Produzione:** https://portalenexus.it dal 24/11, versione `8139e84`
· **Collaudo:** [docs/test/collaudo.md](../../test/collaudo.md): tutte le prove dello sprint riuscite; resta T-26.3
(§8.2).

> Documento unico e asincrono: review (§1–§6), retrospettiva (§7), decisioni da prendere (§8) e proposta di planning
> dello Sprint 18 (§9). La PR resta in **bozza** finché non abbiamo finito di discuterne (A12). Le storie sono già
> chiuse e le change archiviate nelle PR di chiusura (#199, #200, #201, #205, #206); questa PR chiude lo sprint nel suo
> file e nel backlog.

---

## 1. Sintesi

| | |
|---|---|
| **Obiettivo dello sprint** | NEXUS è in produzione sul server di oggi, con i dati veri, il backup notturno e PostgreSQL 19. |
| **Esito** | ✅ **Raggiunto.** NEXUS è in produzione dal 24/11, un giorno prima del previsto, con il sito fermo circa 2 minuti. Le aziende e le chiamate vengono dal database attuale. PostgreSQL 19 c'è dal 23/11, e il primo backup notturno della produzione è riuscito il 25/11 alle 3:30. |
| **Impegno / completato** | 13 punti al planning, **29** con le quattro storie aggiunte il 24/11; completati **29** (6 storie su 6) |
| **Extra** | Analisi di US-507 (tre storie, 15 punti); controllo ASVS sulla produzione; script una tantum per il profilo utente di Keycloak |
| **Velocity** | Sprint 1: 21 · 2: 25 · 3: 21 · 4: 24 · 5: 21 · 6: 20 · 7: 31 · 8: 23 · 9: 25 · 10: 22 · 11: 15 · 12: 21 · 13: 22 · 14: 29 · 15: 27 · 16: 18 · 17: **29** · media 23,2 |
| **PR unite** | 18: #189, #190 (EN-10), #194, #206 (US-1306), #197, #200 (US-1310), #198, #199 (US-513), #201 (EN-11), #204, #205 (US-116), #191 (analisi di US-507), #192, #193, #195, #196 (documenti e CI), #202, #203 (Dependabot). Chiuse senza unirle: #182–#184 (PostgreSQL 19 di Dependabot, sostituite da EN-10). Aperte: nessuna |
| **Migrazioni del database** | Nessuna; la prossima resta **V52** |
| **Deploy** | 6: `47fe414` (23/11, PostgreSQL 19), `1890dd9` (23/11, dati dell'ADMIN), `433f234` (24/11, sessione attiva e release del passaggio), `db4a4c5` (24/11, aziende a pagine), `388e8c2` (24/11, produzione senza dati demo), `8139e84` (24/11, correzione dei dati di un utente) |
| **CI** | Verde in tutte le PR unite; un falso errore nella #205, corretto nella stessa PR (§4) |

## 2. Cosa è stato consegnato

| ID | Storia | Punti | PR | In una frase |
|---|---|---|---|---|
| EN-10 | Aggiornamento a PostgreSQL 19 | 5 | [#189](https://github.com/maubernardi/nexus/pull/189), [#190](https://github.com/maubernardi/nexus/pull/190) | La produzione è nata sulla 19. Lo script `aggiorna-postgresql.sh` ha copiato i dati su un volume nuovo e ha confrontato i numeri; la 18 è rimasta come ritorno indietro fino al `pulisci` del 24/11. |
| US-1306 | Passaggio in produzione | 8 | [#177](https://github.com/maubernardi/nexus/pull/177) (Sprint 16), [#194](https://github.com/maubernardi/nexus/pull/194), [#206](https://github.com/maubernardi/nexus/pull/206) | Il 24/11 alle 11:07: backup, pulizia della demo, importazione del database attuale, primo ADMIN. Poi la produzione senza dati e utenti dimostrativi, con la console di Keycloak raggiungibile solo dal tunnel. |
| US-1310 | Sessione attiva finché NEXUS è aperto | 3 | [#197](https://github.com/maubernardi/nexus/pull/197), [#200](https://github.com/maubernardi/nexus/pull/200) | Il token si rinnova ogni minuto, anche con la pagina nascosta. 5 minuti prima del limite delle 10 ore compare un avviso con «Accedi di nuovo adesso». |
| US-513 | Elenco delle aziende a pagine | 3 | [#198](https://github.com/maubernardi/nexus/pull/198), [#199](https://github.com/maubernardi/nexus/pull/199) | 10, 25, 50 o 100 aziende per pagina, scelte da un menu, invece di 13.799 tutte insieme. |
| EN-11 | Ambiente di collaudo sul PC del PO | 5 | [#201](https://github.com/maubernardi/nexus/pull/201) | `collaudo.sh avvia <PR>` avvia sul tuo PC il NEXUS del branch, con dati finti e le email in una casella finta. Il collaudo si fa prima del merge. |
| US-116 | Correzione dei dati di un utente | 5 | [#204](https://github.com/maubernardi/nexus/pull/204), [#205](https://github.com/maubernardi/nexus/pull/205) | L'ADMIN corregge nome, cognome, email, telefono e, finché l'utente non è mai entrato, lo username: in NEXUS e in Keycloak, con lo storico. Se cambia l'email di chi non è ancora entrato, parte un nuovo invito. |

## 3. Cosa vedi in produzione

Dal passaggio il sito ha solo dati veri: le storie nuove le hai collaudate sul PC con dati finti, e in produzione hai
fatto solo un controllo veloce.

- **T-30** PostgreSQL 19: sulla demo il 23/11; la T-30.4, il backup della notte sulla 19, il 24/11. ✅
- **T-24, T-25, T-28, T-29**: il collaudo prima del passaggio, finito il 23/11. ✅
- **T-27.1 … T-27.4** produzione: primo accesso dell'ADMIN, Cataloghi, backup della prima notte (25/11), console di
  Keycloak dal tunnel. ✅
- **T-31** sessione attiva, in produzione. ✅
- **T-32** aziende a pagine, in produzione. ✅
- **T-33** collaudo sul PC. ✅
- **T-34.1 … T-34.4** correzione dei dati, sul PC prima del merge; **T-34.5** in produzione, con l'utente sbagliato
  corretto. ✅
- **T-26.3** controllo del backup del mattino: ⏳ non parte al mattino (§8.2).

Delle prove più vecchie, sezioni 1–23, ne restano 103 non spuntate (§8.3).

## 4. Qualità

### Test automatici

| | Fine Sprint 16 | Fine Sprint 17 |
|---|---|---|
| Backend – unit | 246 | 246 |
| Backend – integrazione (PostgreSQL e Keycloak veri) | 551 | 575 |
| Frontend – componenti (tutti con controllo axe) | 299 | 324 |
| Prove da capo a fondo nella CI, a ogni PR | backup e ripristino | in più: aggiornamento di PostgreSQL, ambiente di collaudo, script del profilo utente di Keycloak |

Da segnalare:
- **L'aggiornamento di PostgreSQL si prova a ogni PR**: lo script gira su un server finto nella CI, compreso il ritorno
  alla versione precedente se un passo fallisce.
- **Anche l'ambiente di collaudo si prova a ogni PR**: avvio dal commit, accesso dalla pagina di Keycloak, email nella
  casella finta, nessun container o volume rimasto alla fine.
- **US-116 è provata con Keycloak vero**:
  - dopo il cambio dell'email il vecchio invito non vale più;
  - lo username di chi è già entrato si rifiuta;
  - dalla pagina «il mio account» di Keycloak i dati non si cambiano più.
- **Il passaggio ha dato gli stessi numeri della prova generale**: 13.799 aziende, 7.786 referenti, 6.980 note,
  38.789 chiamate.

### Sicurezza e privacy

- **La produzione non ha dati né utenti dimostrativi**: il realm di produzione non ha utenti di prova e la CI lo
  controlla. Le variabili `NEXUS_DEMO_PASSWORD_*` sono state tolte dal `.env`.
- **Dati veri solo dove servono**:
  - il file Access è stato cancellato dal server dopo l'importazione;
  - il volume della 18 è stato tolto con `pulisci`;
  - il collaudo sul PC usa solo dati finti, e le sue email non escono dal PC.
- **Console degli amministratori di Keycloak**: il controllo ASVS del 24/11 l'ha trovata raggiungibile da Internet. Ora
  Caddy risponde `403` e la console si apre solo dal tunnel SSH (T-27.4).
- **Profilo utente di Keycloak**: un utente poteva cambiare nome ed email dalla pagina «il mio account», senza passare
  da NEXUS. Ora username, email, nome e cognome li cambia solo l'ADMIN, da NEXUS. In produzione lo script ha trovato 4
  utenti, nessuno con dati mancanti.
- **Sessione** (US-1310): Keycloak tiene gli stessi limiti di prima, 30 minuti di inattività e 10 ore al massimo.
  NEXUS ora avvisa prima della fine (WCAG 2.2.1). Con la sessione sempre attiva conta di più il blocco dello schermo
  dei PC del Call Center (§10).
- **Dependabot**: nessun avviso aperto. Unite #202 e #203; chiuse #182–#184.
- **Avviso SSH «post-quantum»**: compare dal 04/11 a ogni accesso al server (§8.5).

### Difetti

| Difetto | Dove | Come è emerso | Correzione |
|---|---|---|---|
| La 19 non parte sui dati della 18: le PR di Dependabot avrebbero fatto fallire il deploy | EN-10 | Prova in locale, prima della PR (23/11) | Volume nuovo e script dell'aggiornamento (#189); #182–#184 chiuse |
| T-30.3 chiedeva al Tutor la pagina «Aziende», che non ha | Collaudo | Tuo collaudo (23/11) | Prova corretta: si fa come operatore (#190) |
| Comandi del README con il percorso relativo del `.env` | README | Tu, sul server (23/11) | Percorso assoluto (#192) |
| Il file Access non si poteva copiare in `/opt/nexus`, riservata all'utente `deploy` | README del passaggio | Passaggio (24/11) | Passa dalla tua home; README corretto |
| Console degli amministratori di Keycloak raggiungibile da Internet | Produzione | Controllo ASVS (24/11) | `403` da Caddy e tunnel (#194) |
| La sessione scadeva dopo 30 minuti con la pagina in secondo piano, senza avviso | Frontend | Tu, primo giorno d'uso (24/11) | US-1310 (#197) |
| Elenco delle aziende lento: 13.799 righe tutte insieme | Aziende | Tuo collaudo T-27.2 (24/11) | US-513 (#198) |
| Dopo «Successiva» la vista non tornava in cima alla tabella | US-513 | Prova prima della PR | Corretto nella #198 |
| Keycloak, già dichiarato sano, rispondeva `503` per qualche secondo | EN-11 | Prova nella CI | I controlli riprovano per un minuto (#201) |
| Un utente creato con lo username sbagliato non si poteva correggere | Utenti (US-103) | Tu, in produzione (24/11) | US-116 (#204) |
| Dalla pagina «il mio account» di Keycloak si cambiavano nome ed email senza NEXUS | Keycloak | Prove di US-116 | Profilo in sola lettura (#204 e script) |
| Nella pagina «Utenti» la colonna dell'email era schiacciata a 58 pixel | US-116 | Controllo in Chrome prima della PR | I pulsanti vanno a capo (#204) |
| Il controllo finale dello script del profilo falliva a caso (`pipefail` con `grep -q`) | US-116 | CI della #205 | Prima l'output, poi il controllo (#205) |
| I controlli pianificati di GitHub partono con ore di ritardo, o non partono | US-1307, US-1302 | Conteggio dei run per questa review | §8.2 |

## 5. Decisioni prese durante lo sprint

| # | Decisione | Presa da | Dove |
|---|---|---|---|
| D1 | EN-10 prima del passaggio, sulla demo: la produzione nasce sulla 19 | PO (23/11) | Planning |
| D2 | La 19 su un volume nuovo; lo script lo lanci tu con sudo prima del deploy, e la 18 resta come ritorno indietro fino a `pulisci` | Team (23/11) | EN-10 |
| D3 | US-507 diventa tre storie, US-511, US-507 e US-512, per 15 punti, senza release | PO (23/11) | Analisi di US-507 |
| D4 | Passaggio anticipato al 24/11, con il file Access del 4/11 | PO (24/11) | US-1306 |
| D5 | Console degli amministratori di Keycloak solo dal tunnel SSH | PO (24/11) | US-1306, ASVS |
| D6 | Sessione rinnovata finché la pagina è aperta, con l'avviso prima delle 10 ore (scelta fra tre strade) | PO (24/11) | US-1310 |
| D7 | Aziende a pagine da 10, 25, 50 o 100, scelte da un menu | PO (24/11) | US-513 |
| D8 | Collaudo sul tuo PC, prima del merge (scelta fra quattro strade); in produzione solo un controllo veloce, senza dati finti | PO (24/11) | EN-11, Definition of Done |
| D9 | L'ADMIN corregge i dati degli utenti; lo username solo finché l'utente non è mai entrato | PO (24/11) | US-116 |
| D10 | Username, email, nome e cognome si cambiano solo dall'amministrazione, anche in Keycloak | Team (24/11) | US-116 |
| D11 | Cambiata l'email di chi non è ancora entrato, NEXUS rimanda l'invito; il vecchio non vale più | Team (24/11) | US-116 |

## 6. Stato del prodotto rispetto alla roadmap

**R1** — sviluppo completo; resta **US-1301** (PWA su smartphone, a cura tua: prova T-10.1).

**R2, R3** — ✅ complete.

**R4** — *produzione*: **214 punti su 222** fatti, **NEXUS è in produzione**. Resta:

| Storia | Punti | Quando |
|---|---|---|
| US-1305 Revisione UX/UI | 8 (da rivedere) | Dopo le prime settimane d'uso reale (review dello Sprint 15, §8.7) |

**Senza release**: US-511, US-507 e US-512 (15 punti), dall'analisi del 23/11 (§8.6). Da aggiungere, se accetti:
**US-1311** Avvisi affidabili su sito e backup (§8.2).

**Product backlog:** 104 elementi, 429 punti; completati 99 (405 punti), ne restano 5 (24 punti).

## 7. Retrospettiva

**Cosa ha funzionato**
- **Il passaggio è andato come la prova generale**: stessi numeri, sito fermo circa 2 minuti, un giorno prima del
  previsto. La preparazione dello Sprint 16, con lo script provato sul backup vero, ha ripagato.
- **PostgreSQL 19 prima del passaggio.** La prova in locale ha scoperto che la 19 non parte sui dati della 18. I dati
  veri non sono mai passati da una versione all'altra.
- **Il primo giorno d'uso ha portato quattro storie**: US-1310, US-513, EN-11 e US-116. Tutte decise con te, poi fatte,
  collaudate e in produzione nello stesso giorno.
- **Il collaudo prima del merge ha funzionato dalla prima storia**: US-116 è passata dal tuo PC, con dati finti, e in
  produzione è bastato un controllo veloce.
- **Le prove hanno trovato due buchi di sicurezza prima che venissero sfruttati**: la console degli amministratori di
  Keycloak aperta su Internet e la pagina «il mio account» che scavalcava NEXUS.

**Cosa non ha funzionato**
- **I controlli pianificati di GitHub non girano come pensavamo.** Ce ne accorgiamo solo ora:
  - il controllo del sito dovrebbe girare 72 volte al giorno. Dall'8/11 GitHub lo fa partire 2 o 3 volte al giorno, e
    spesso una di queste cade fuori dalla fascia 8–20;
  - il controllo del backup delle 9:07 parte tra le 16 e le 21.

  Lo Sprint 16 aveva visto i ritardi, ma non aveva contato i run, e il minuto 7 (A38) non è bastato.
- **Lo sprint è passato da 13 a 29 punti in un giorno**: deploy in produzione alle 15:14, alle 15:39 e alle 21:14, e
  una correzione alle 22:49. È andata bene perché in produzione c'eri quasi solo tu. Con il Call Center al lavoro
  serve una regola sugli orari (§8.4).
- **L'utente sbagliato in produzione.** NEXUS permetteva di creare un utente, ma non di correggerlo: US-103 non lo
  prevedeva, e in otto sprint non ce ne siamo accorti. Altri dati potrebbero avere lo stesso problema (A42).
- **Errori nelle prove e nel README**, trovati da te mentre li usavi:
  - T-30.3 chiedeva al Tutor una pagina che non ha;
  - il README dava un percorso relativo per il `.env` e mandava il file Access in una cartella dove non potevi scrivere.
- **Un falso errore nella CI** (#205): il controllo finale dello script falliva a caso, per un dettaglio di bash.
  Nessun danno: sul server lo script era riuscito.

**Azioni**

| # | Azione | Chi |
|---|---|---|
| A41 | Un workflow pianificato si verifica contando i run dei primi giorni, non guardandone uno | Team |
| A42 | Prima che arrivino gli utenti, cerco i dati che si inseriscono ma non si correggono dall'app, e te ne porto l'elenco | Team |
| A43 | Nessun orario fisso per i deploy: li decidi tu volta per volta (§8.4). Quando ne propongo uno, ricordo il breve fermo | Team e PO |
| A44 | Negli script con `pipefail`, mai `comando \| grep -q`: prima l'output in una variabile, poi il controllo | Team |
| A36, A37 | (confermate) Attesa della CI solo con `--watch`; container di prova con dati spenti con `docker rm -f -v` | Team |

## 8. Da decidere

### 8.1 Chiusura dello Sprint 17
*Proposta:* Sprint 17 chiuso con velocity **29**: tutte le sei storie sono fatte, collaudate e in produzione. Questa PR
aggiorna il file dello sprint e il backlog.

> ✅ **Deciso (PO, 25/11):** Sprint 17 chiuso con velocity 29.

### 8.2 Avvisi su sito e backup
Dalle esecuzioni dei workflow (Actions):
- **Controllo del sito** (US-1307), previsto ogni 10 minuti dalle 8 alle 20, cioè 72 volte al giorno. Dall'8/11 GitHub
  lo fa partire 2 o 3 volte al giorno, con ore di ritardo: il 25/11 alle 2:14 (fuori fascia, quindi nessun controllo) e
  alle 17:41. In pratica **un controllo al giorno**.
- **Controllo del backup delle 9:07** (US-1302). Nei quattro giorni dal 22/11 è partito tra le 15:58 e le 21:05. Il
  backup notturno funziona: è l'avviso che arriva tardi.

GitHub avverte che i workflow pianificati si possono ritardare o saltare quando è carico; da parte nostra non si può
correggere. Le strade:

| | Cosa | A favore | Contro |
|---|---|---|---|
| **A** | Un servizio esterno (per esempio UptimeRobot o Healthchecks.io) controlla il sito ogni pochi minuti e aspetta un «segnale» dal server dopo ogni backup; se il segnale non arriva entro le 9, ti scrive | Affidabile e indipendente dal server: avvisa anche se il server è spento | Uno o due account nuovi a tuo nome (piani gratuiti e sede dei dati da verificare prima di scegliere); ricevono solo l'indirizzo pubblico del sito e un segnale senza dati |
| **B** | Controlli dal server stesso, con un timer di sistema e la posta di IONOS che NEXUS usa già | Nessun account nuovo; orari precisi | Se il server o la sua rete sono giù, nessun avviso: proprio il caso più grave |
| **C** | Restare su GitHub | Nessun lavoro | Un controllo del sito al giorno: un guasto del mattino lo scopre chi usa NEXUS |

*Proposta:* **A**, con una storia **US-1311 «Avvisi affidabili su sito e backup»**, 3 punti, R4, nello Sprint 18. Il
servizio lo scegliamo insieme quando raffiniamo la storia, dopo aver letto piani e privacy. I controlli di GitHub
restano come seconda rete, e T-26.3 si riscrive con la soluzione scelta.

> ✅ **Deciso (PO, 25/11):** **A**. US-1311 entra nel backlog: 3 punti, R4. Il servizio si sceglie al planning tra i
> due qui sotto. Con entrambi, al servizio arrivano solo gli indirizzi pubblici del sito e un segnale senza dati;
> l'account lo crea il PO, e l'indirizzo segreto del segnale va solo nel `.env` del server.
>
> | | Servizio | Cosa fa | A favore | Contro |
> |---|---|---|---|---|
> | **1** | **Better Stack**, piano gratuito: 10 controlli e 10 segnali, ogni 3 minuti | Controlla da fuori le tre pagine di oggi (sito, Keycloak, API che risponde 401) e aspetta il segnale del backup | Un solo account fa tutto; controlli veri da fuori, come oggi da GitHub | Servizio commerciale: sede dei dati e condizioni da leggere prima |
> | **2** | **Healthchecks.io**, piano gratuito: 20 segnali | Aspetta solo segnali: il server controlla le proprie pagine ogni 5 minuti e manda il segnale se rispondono, e uno dopo il backup | Europeo, a codice aperto; nessun controllo da configurare sul sito del servizio | I controlli partono dal server: se il server è spento o isolato il segnale manca e l'avviso arriva, ma un blocco che ferma solo chi arriva da fuori non si vede |
>
> *Proposta:* **1**, perché sostituisce il controllo di oggi senza cambiarne la natura.

### 8.3 Prove vecchie prima che arrivino gli utenti
Le prove sono 158 e ne sono spuntate 54. Nelle sezioni 1–23 (Sprint 1–15) ne restano 103: il flusso dal beneficiario
alla proposta, l'appuntamento, la bacheca, il tirocinio, i documenti e il report. In produzione non si possono più
fare, ma sul tuo PC sì (EN-11).

| | Cosa | Tempo | Rischio |
|---|---|---|---|
| **A** | Una **prova del flusso completo** sul PC, prima di dare gli account a Tutor e Call Center: circa 15 prove scelte, dalla segnalazione al report. Le altre restano come storico | Un paio d'ore | Basso: le funzioni sono anche coperte dai test automatici |
| **B** | Tutte le 103 prove | Diversi pomeriggi | Il più basso |
| **C** | Nessuna: i problemi emergono dall'uso | — | Il primo errore lo trova un Tutor o un operatore |

*Proposta:* **A**. Preparo la sezione con le 15 prove e avvio il collaudo dall'ultima versione di `main`
(`collaudo.sh avvia main`).

> ✅ **Deciso (PO, 25/11):** **C**, nessuna prova vecchia. Per ora la produzione è una **pre-produzione**: più avanti
> si azzera tutto. Le 103 prove restano come storico.

### 8.4 Orari dei deploy e arrivo degli utenti
Durante un deploy i servizi ripartono, e per un breve momento NEXUS non risponde: chi sta salvando può ricevere un
errore.

*Proposta:* in produzione i deploy si fanno **prima delle 8:30 o dopo le 18**, salvo le correzioni urgenti, che
concordiamo prima (A43). Ti chiedo:
- gli orari del Call Center, se diversi;
- quando pensi di dare gli account a Tutor e Call Center. Da questa data dipendono la prova del §8.3 e la revisione
  UX/UI (US-1305).

> ✅ **Deciso (PO, 25/11):** nessun orario fisso: il PO decide volta per volta quando fare un deploy. Per ora la
> produzione è una pre-produzione (§8.3). A43 cambia di conseguenza.

### 8.5 Avviso SSH «post-quantum»
Dal 04/11, a ogni accesso al server, il programma SSH del tuo PC avvisa che lo scambio delle chiavi non è
«post-quantum». Il rischio è chiamato *store now, decrypt later*: chi registra oggi il traffico potrebbe decifrarlo fra
anni con un computer quantistico. Nelle sessioni SSH passano i comandi, i loro risultati e, quando modifichi il `.env`,
i segreti.

| | Cosa | A favore | Contro |
|---|---|---|---|
| **A** | Controllare che cosa offre l'OpenSSH di Rocky 9 e, se c'è un algoritmo post-quantum, attivarlo con i criteri di cifratura del sistema | L'avviso sparisce e il traffico è protetto | Un errore nella configurazione di SSH può chiudere l'accesso: si lavora con una seconda sessione aperta e si prova la configurazione prima di applicarla |
| **B** | Lasciare così fino a un aggiornamento del sistema operativo del server | Nessun lavoro | Il traffico registrato oggi resta esposto in futuro; conviene cambiare i segreti del `.env` di tanto in tanto |
| **C** | Nascondere l'avviso sul PC | Nessun lavoro | Nessuna protezione: solo silenzio. Sconsigliata |

*Proposta:* **A**, partendo da un controllo in sola lettura. Io non posso farlo: l'accesso SSH al server non mi è
consentito. Lancia sul PC questo comando e incollami il risultato:

```bash
ssh portalenexus 'rpm -q openssh-server; update-crypto-policies --show; ssh -Q kex | grep -iE "sntrup|mlkem" || echo "nessun algoritmo post-quantum"'
```

Se l'algoritmo c'è, ti preparo i comandi con sudo, senza punti di sprint.

> ✅ **Deciso (PO, 25/11):** **A**. Il risultato del controllo:
> - sul server c'è OpenSSH 9.9 di Rocky 9.8, con i criteri di cifratura `DEFAULT`;
> - OpenSSH ha due algoritmi post-quantum, `mlkem768x25519-sha256` e `sntrup761x25519-sha512`;
> - OpenSSH 10.2, sul PC, li chiede per primi.
>
> L'avviso viene quindi dai criteri `DEFAULT` di Rocky 9, che non li attivano. Da Rocky 9.7 esiste il sottocriterio
> `PQ`, che li aggiunge senza togliere nulla. Il PO ha ricevuto i comandi il 25/11. Esito: ⏳.

### 8.6 US-511, US-507 e US-512: due proposte da confermare
Dall'analisi del 23/11 restano due proposte mie, che bloccano la *Definition of Ready*:
- prima di inviare una richiesta per un'azienda nuova, il Tutor vede i **nomi simili** già presenti, contro i doppioni;
- alla chiusura della richiesta, l'operatore **collega l'azienda** a quella del database, se esiste.

*Proposta:* confermarle entrambe: le tre storie diventano Ready.

> ✅ **Deciso (PO, 25/11):** confermate entrambe. US-511, US-507 e US-512 sono ✅ Ready.

### 8.7 Contenuto dello Sprint 18
*Proposta:* prima gli avvisi (US-1311), poi il gruppo della richiesta di contatto: US-511, US-507 e US-512, 18 punti
in tutto (§9). Sotto la media, per lasciare spazio a quello che arriverà dal primo uso degli utenti, come in questo
sprint. Nello sprint anche la ricerca dei dati non correggibili (A42) e la prova del flusso completo (§8.3), senza
punti.

> ✅ **Deciso (PO, 25 – 26/11):** prima di partire, il PO chiede tre storie nuove sulle aziende, già nel backlog:
> - **US-514**: affidabilità (1–4 stelle) e interesse al progetto (1–2 pollici), presi dai simboli `$` e `£` all'inizio
>   dei nomi importati, che si ripuliscono senza perdere il dato;
> - **US-1104**: tre voti del Tutor alla fine del tirocinio, mostrati come faccina, con la media nella scheda
>   dell'azienda;
> - **US-515**: l'azienda «nave scuola», proposta dal Tutor e segnata dall'operatore, con il simbolo 🚢 accanto al nome.
>
> Hanno la precedenza sul gruppo della richiesta di contatto: lo Sprint 18 è quello del §9, US-514 subito, anche nel
> collaudo ([sprint-18](sprint-18.md)). Il servizio di US-1311 si sceglie dopo un approfondimento. La prova del flusso
> completo non c'è più (§8.3).

## 9. Proposta per lo Sprint 18

**Date:** tre giorni dalla conferma del planning, indicativamente 26 – 28 novembre 2026. **Obiettivo proposto:** *la
scheda dell'azienda dice quanto è affidabile e interessata, come si sono trovati i tirocinanti e se è una nave scuola;
gli avvisi su sito e backup arrivano in tempo.*

| Ordine | ID | Storia | Punti |
|---|---|---|---|
| 1 | US-514 | Affidabilità e interesse al progetto dell'azienda | 5 |
| 2 | US-1104 | Valutazione dell'azienda alla fine del tirocinio | 5 |
| 3 | US-515 | Azienda «nave scuola» | 5 |
| 4 | US-1311 | Avvisi affidabili su sito e backup (§8.2) | 3 |
| | | **Totale** | **18** |

Nello Sprint 19 il gruppo della richiesta di contatto: US-511, che porta al Tutor anche le valutazioni, US-507 e
US-512.

Prossima migrazione: **V52**; probabilmente ne servirà una per ciascuna delle prime tre storie. Ogni storia passa dal
collaudo sul tuo PC prima del merge (EN-11), e i deploy li decidi tu (§8.4).

## 10. Azioni a carico del PO

- [x] Decidere i punti §8.1 … §8.7 (25 – 26/11).
- [x] Lanciare il comando del §8.5 e incollarmi il risultato (25/11).
- [ ] Attivare il sottocriterio `PQ` di SSH sul server (§8.5) e incollarmi il risultato.
- [ ] Unire questa PR dopo le decisioni.
- [ ] Gli account degli utenti.
- [ ] Il blocco automatico dello schermo dei PC del Call Center, dopo pochi minuti, con chi gestisce i PC (US-1310).
- [ ] I 30 giorni dei backup nel registro dei trattamenti, con il DPO (dallo Sprint 16).
- [ ] US-1301: la prova T-10.1 della PWA sullo smartphone.
