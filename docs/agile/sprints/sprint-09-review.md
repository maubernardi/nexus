# Sprint 9 — Review, retrospettiva e planning

**Sprint:** 6 – 8 novembre 2026 (iniziato il 05/11 sera dopo il planning, chiuso il 06/11, in anticipo) ·
**Review:** 06/11/2026 · **Partecipanti:** Mauro Bernardi (Product Owner), team di sviluppo (Claude Code) ·
**Demo:** https://portalenexus.it, versione `e30269f` (tutto lo Sprint 9) ·
**Collaudo:** [docs/test/collaudo.md](../../test/collaudo.md), sezione 20 (T-20.1–T-20.8).

> Documento unico e asincrono: review (§1–§6), retrospettiva (§7), decisioni da prendere (§8) e proposta di planning
> dello Sprint 10 (§9). La PR resta in **bozza** finché non abbiamo finito di discuterne (A12): le decisioni le aggiungo
> qui, poi la segno come pronta per il merge.

---

## 1. Sintesi

| | |
|---|---|
| **Obiettivo dello sprint** | L'ADMIN gestisce progetti e utenti da NEXUS, senza la console di Keycloak: crea gli utenti, li assegna ai progetti, sposta le segnalazioni di un Tutor su un collega, li disattiva o cambia il ruolo; i Coordinatori riorganizzano le segnalazioni dei loro progetti. |
| **Esito** | ✅ **Raggiunto.** Tutte le storie sono pubblicate sulla demo; il primo utente creato da NEXUS ha impostato la password ed è entrato (T-20.1, 06/11). |
| **Impegno / completato** | 25 punti (19 al planning, ripianificati il 06/11) / **25 punti** (6 storie su 6) |
| **Extra** | Nessuno: US-1302 e US-205 non iniziate |
| **Fuori piano** | Nessuno. Nuova storia nel backlog: US-113 *Registro degli accessi* (§8.4) |
| **Velocity** | Sprint 1: 21 · 2: 25 · 3: 21 · 4: 24 · 5: 21 · 6: 20 · 7: 31 · 8: 23 · 9: **25** · media 23,4 |
| **PR unite** | 10 (#99–#108): 6 di funzionalità, 1 sul deploy (A16), 3 di documentazione (review dello Sprint 8, ripianificazione, chiusura) |
| **Migrazioni del database** | 2: V28 (catalogo dei progetti), V29 (Coordinatore di progetto) |
| **Deploy della demo** | 06/11: 08:52 `6bd882c` (US-102, US-103) · 14:24 `c10a62a` (US-104) · 16:26 `aebf44b` (US-111) · 18:12 `e30269f` (US-105, US-112). Tutti riusciti |

## 2. Cosa è stato consegnato

| ID | Storia | Punti | PR | In una frase |
|---|---|---|---|---|
| A16 | Comandi di sola lettura sul server (azione, in parte) | — | [#100](https://github.com/maubernardi/nexus/pull/100) | Il workflow «Stato del server» legge `status` e i log del backend e del database, senza poter cambiare nulla. |
| US-102 | Gestione progetti | 3 | [#101](https://github.com/maubernardi/nexus/pull/101) | I progetti (GOL, POLIS…) si aggiungono, rinominano e disattivano nella pagina «Cataloghi». |
| US-103 | Creazione utente | 8 | [#102](https://github.com/maubernardi/nexus/pull/102) | L'ADMIN crea l'utente in NEXUS e in Keycloak; l'utente riceve l'email per impostare la password, valida 3 giorni. |
| US-104 | Assegnazione utenti ai progetti | 3 | [#103](https://github.com/maubernardi/nexus/pull/103) | L'ADMIN sceglie i progetti di un Tutor: ne vede le segnalazioni e segnala solo su quelli. |
| US-111 | Spostamento delle segnalazioni tra Tutor | 5 | [#105](https://github.com/maubernardi/nexus/pull/105) | L'ADMIN passa le segnalazioni aperte di un Tutor a un collega dello stesso progetto, con il beneficiario. |
| US-105 | Disattivazione e cambio ruolo | 3 | [#106](https://github.com/maubernardi/nexus/pull/106) | «Accesso e ruolo»: disattiva, riattiva o cambia ruolo in NEXUS e in Keycloak; i ticket in carico tornano in coda. |
| US-112 | Coordinatore di progetto | 3 | [#107](https://github.com/maubernardi/nexus/pull/107) | Un Tutor segnato come Coordinatore di un progetto ne sposta le segnalazioni dalla pagina «Coordinamento». |

### La gestione degli utenti dopo lo Sprint 9

```
ADMIN ──► Nuovo utente (NEXUS + Keycloak) ──► email «imposta la password» ──► primo accesso
  │
  ├──► Progetti del Tutor (e Coordinatore per progetto)
  │
  ├──► Segnalazioni del Tutor ──► spostate su un collega dello stesso progetto (con il beneficiario)
  │                                       ▲
  │                     Coordinatore ─────┘ (solo nei progetti che coordina)
  │
  └──► Accesso e ruolo: disattiva / riattiva / cambia ruolo
         · Tutor con segnalazioni aperte: prima si spostano
         · operatore con ticket in carico: tornano nella coda «Da prendere in carico»
```

## 3. Demo guidata

Le prove passo per passo sono nella **sezione 20 del collaudo**, sulla versione `e30269f`:
- **T-20.1:** nuovo utente (✅ 06/11: password impostata, accesso riuscito);
- **T-20.2–T-20.4:** dati già usati, nuovo invito, accesso riservato all'ADMIN;
- **T-20.5:** progetti di un Tutor (provati in parte: GOL assegnato e segnalazioni visibili);
- **T-20.6:** spostamento delle segnalazioni;
- **T-20.7:** disattivazione, riattivazione e cambio di ruolo;
- **T-20.8:** Coordinatore di progetto.

Per T-20.6–T-20.8 servono segnalazioni aperte di `tutor1` e `tutor2` su GOL, come nei dati della demo.

## 4. Qualità

### Test automatici

| | Fine Sprint 8 (con US-110) | Fine Sprint 9 |
|---|---|---|
| Backend – unit | 106 | 108 |
| Backend – integrazione (PostgreSQL reale) | 326 | 362 |
| Frontend – componenti (tutti con controllo axe) | 179 | 204 |

Da segnalare:
- **Keycloak vero nei test**: creazione, disattivazione, cambio di ruolo e chiusura delle sessioni si provano contro un
  Keycloak della stessa versione della produzione, non solo con un simulatore.
- **Tutto o niente**: se Keycloak non risponde, in NEXUS non cambia nulla (utente, ruolo, ticket in coda); un test lo
  verifica per ogni operazione.
- **Spostamenti e coordinamento** hanno test sui rifiuti: Tutor disattivato o di un altro progetto, segnalazione
  chiusa, progetto non coordinato (`403`).

### Accessibilità (WCAG 2.2 AA)

Nuove pagine (Utenti, Nuovo utente, Progetti del Tutor, Segnalazioni del Tutor, Accesso e ruolo, Coordinamento) con il
controllo axe nei test: **0 violazioni**. La casella «Coordinatore» ha il nome accessibile «Coordinatore di GOL», così
chi usa un lettore di schermo sa di quale progetto si parla.

### Difetti

| Difetto | Dove | Come è emerso | Correzione |
|---|---|---|---|
| **Il ruolo cambiato dall'ADMIN sarebbe tornato indietro da solo**: NEXUS copia il ruolo dal token di accesso, che resta valido fino a 5 minuti | US-105 | Analisi, prima della PR | Prima della PR (#106): un token emesso prima dell'ultima modifica dell'utente non riallinea più il ruolo; il cambio chiude anche le sessioni |
| **Ticket bloccati**: un ticket già avanti (es. ad appuntamento fissato) tolto a un operatore disattivato non sarebbe comparso in nessuna coda | US-105 | Analisi, prima della PR | Prima della PR (#106): la coda comprende i ticket liberi in qualunque fase; chi li prende li segue dalla fase in cui erano |
| **Un dato di test irrealistico** (ticket avanzato senza operatore) faceva fallire i test dopo la coda allargata | US-105 | CI | Prima del merge (#106) |
| **Il ripristino automatico del 05/11 non è ripartito** | Deploy | Sprint 8 | **Aperto**: i comandi di sola lettura ci sono (#100), la causa no. A16, §8.3 |

## 5. Decisioni prese durante lo sprint

| # | Decisione | Presa da | Dove |
|---|---|---|---|
| D1 | Username scritto dall'ADMIN con il suggerimento `nome.cognome`; email «imposta la password» di Keycloak, valida 3 giorni | PO (§8.7–§8.8 review Sprint 8, 06/11) | US-103 |
| D2 | Un Tutor con segnalazioni aperte non si disattiva né cambia ruolo: prima l'ADMIN le sposta su un Tutor attivo dello stesso progetto | PO (06/11) | US-105, US-111 |
| D3 | Con la segnalazione passa al nuovo Tutor anche il beneficiario, con tirocinio e documenti; le chiuse restano nello storico | PO (06/11) | US-111 |
| D4 | Operatore del Call Center con ticket in carico: si disattiva dopo un avviso; i ticket tornano in coda | PO (06/11) | US-105 |
| D5 | Coordinatore definito per progetto, senza un nuovo ruolo in Keycloak; più Coordinatori per progetto | PO (06/11) | US-112 |
| D6 | Ripianificazione: +US-111, +US-112, US-1308 allo Sprint 10 (da 19 a 25 punti) | PO (06/11) | [PR #104](https://github.com/maubernardi/nexus/pull/104) |
| D7 | L'ADMIN non disattiva né cambia ruolo a se stesso: NEXUS non resta mai senza ADMIN | Team | US-105 |
| D8 | Chi diventa Tutor lascia in coda i ticket in carico; chi passa da Call Center ad ADMIN li tiene | Team | US-105 |
| D9 | Il Coordinatore sposta anche verso se stesso; nella sua pagina nessuna segnalazione è già selezionata; il coordinamento decade togliendo il progetto o cambiando ruolo | Team, **da confermare** (§8.1) | US-112 |
| D10 | Registro degli accessi: anche i tentativi falliti, visibile all'ADMIN e all'utente, conservazione 12 mesi | PO (06/11) | US-113 (§8.4) |

## 6. Stato del prodotto rispetto alla roadmap

**R1** — sviluppo completo; resta **US-1301** (PWA su smartphone, a cura del PO: prova T-10.1).

**R2, R3** — ✅ complete.

**R4** — *produzione*: con lo Sprint 9 fatta tutta la gestione di utenti e progetti: **36 punti su 71**. Restano:

| Gruppo | Storie | Punti |
|---|---|---|
| Accesso | US-1308 recupero password (2), US-113 registro degli accessi (5) | 7 |
| Dati | US-1302 backup (5), US-1303 anonimizzazione a scadenza (5) | 10 |
| Produzione | US-1305 revisione UX/UI (8), US-1306 passaggio in produzione (5), US-1307 monitoraggio e sicurezza (5) | 18 |

Con la velocity media (23) R4 si chiude in **due sprint**: lo Sprint 10 per accesso, dati e monitoraggio, lo Sprint 11
per la revisione UX/UI e il passaggio in produzione.

**Product backlog:** 73 elementi attivi, 263 punti; completati 64 elementi (222 punti) negli Sprint 1–9.

## 7. Retrospettiva

**Cosa ha funzionato**
- **Impegno completato in un giorno e mezzo**, anche dopo la ripianificazione da 19 a 25 punti.
- **Ripianificazione rapida**: la tua decisione su US-105 ha fatto nascere due storie; le abbiamo definite, stimate e
  messe nello sprint in un'ora, con una PR dedicata.
- **Deploy dopo ogni storia, tutti riusciti**: quattro deploy in un giorno, ognuno verificato sul bundle online. Il test
  di avvio con il profilo di produzione (A17) ha fatto il suo lavoro.
- **Due difetti di logica trovati prima della PR** (ruolo che torna indietro, ticket bloccati): ragionare sui casi
  limite prima di scrivere il codice ha evitato di scoprirli sulla demo.
- **A14 applicata**: le scelte che potevano limitare gli utenti (Coordinatore verso se stesso, disattivazione dell'ADMIN)
  sono segnate e portate qui.

**Cosa non ha funzionato**
- **Il collaudo resta indietro**: della sezione 20 è spuntata solo T-20.1; le prove degli sprint precedenti (T-13.4,
  sezioni 16–17, T-19.2) sono ancora aperte. Con sprint così veloci il collaudo rischia di accumularsi (§8.5).
- **A16 non è chiusa**: ora posso leggere i log, ma la causa del ripristino fallito del 05/11 non l'ho cercata. Va
  messa nello sprint con un tempo dedicato, non lasciata in fondo.
- **Ho lanciato un deploy senza una tua richiesta esplicita**, dopo il merge di #106: il controllo automatico l'ha
  bloccato. La regola è chiara: il deploy solo su tua richiesta.
- **Un'archiviazione OpenSpec è fallita** perché la change di US-105 riscriveva uno scenario della coda con un nome
  diverso: l'ho corretta prima della chiusura, ma va controllata quando si scrive la change, non quando si archivia.

**Azioni**

| # | Azione | Chi |
|---|---|---|
| A16 | (riportata) Trovare la causa del ripristino fallito, correggerla e provarla di proposito (§8.3) | Team, con il PO |
| A18 | Una change che modifica un requisito esistente ne riporta tutti gli scenari, con gli stessi nomi; si verifica con `openspec archive` in prova già nella PR della storia | Team |
| A19 | Deploy solo su richiesta esplicita del PO, anche quando è il passo successivo atteso | Team |
| A14 | (confermata) Le scelte che possono limitare gli utenti si segnano e si portano nella review | Team |

## 8. Da decidere

### 8.1 Scelte del team su US-112
Le ho segnate da confermare:
- il **Coordinatore può spostare le segnalazioni anche verso se stesso** (per prendersi il lavoro di un collega);
- nella pagina «Coordinamento» **nessuna segnalazione è già selezionata**, perché sono di Tutor diversi (per l'ADMIN,
  che sposta quelle di un solo Tutor, partono tutte selezionate);
- il **coordinamento decade** quando l'ADMIN toglie il progetto al Tutor o gli cambia il ruolo.

*Proposta:* confermarle così come sono.

### 8.2 Chiusura dello Sprint 9
*Proposta:* Sprint 9 chiuso con velocity **25**, US-111 e US-112 comprese (entrate con la ripianificazione).

### 8.3 Il ripristino automatico (A16)
I log del 05/11 potrebbero non esserci più. *Proposta:* riprodurre il problema **di proposito**, in un orario che
scegli tu, con una release che non parte; i comandi `status` e `logs` mostrano che cosa fa lo script di ripristino.
La demo resterebbe giù al massimo qualche minuto; se il ripristino non riparte, lo rilancio con un deploy della
versione buona.

*Alternativa:* cercare la causa solo leggendo lo script, senza provarlo sul server.

### 8.4 Registro degli accessi (US-113)
Decisioni già prese (06/11): data, ora, IP ed esito, anche i tentativi falliti; lo vedono l'ADMIN e l'utente;
conservazione 12 mesi. *Proposta tecnica:*
- registra **Keycloak**, che già conosce gli accessi riusciti e falliti con l'IP, e li cancella da solo dopo 12 mesi;
  NEXUS li legge e li mostra;
- per attivarlo serve un passo sul server, con uno script come per le email di Keycloak (lista completa nella PR);
- i tentativi con uno **username inesistente** non appartengono a nessun utente: non compaiono nei registri dei
  singoli. Se vuoi vederli, una pagina dell'ADMIN con tutti gli accessi falliti costerebbe circa 2 punti in più.

### 8.5 Il collaudo arretrato
Sono aperte: T-13.4, sezioni 16–17, T-19.2, T-20.2–T-20.8, US-1301. *Proposta:* nello Sprint 10 dai la precedenza
alla sezione 20 (la gestione degli utenti serve per andare in produzione), poi alle 16–17. Se qualche prova è già
fatta, basta l'elenco e la spunto.

### 8.6 Backup fuori dal server (US-1302)
Il backup deve stare **fuori dal server**. Serve una destinazione:
- **spazio di archiviazione IONOS** (S3 compatibile), con lo stesso fornitore del server;
- **un altro server o un NAS della cooperativa**, raggiunto via SSH;
- **un servizio esterno** (es. Backblaze B2), a pagamento ma economico.

*Proposta:* IONOS, per avere un solo fornitore e un solo contratto. Le credenziali le metti tu nel `.env` del server,
mai nel repository. Conservazione proposta: 30 backup giornalieri, cifrati.

### 8.7 Monitoraggio (US-1307)
Per l'avviso quando la demo non risponde:
- **un controllo dalla CI di GitHub** ogni 10 minuti, con un'email a te se fallisce: gratuito, nessun nuovo account;
- **un servizio di monitoraggio esterno** (es. UptimeRobot), più preciso, con un account in più.

*Proposta:* il controllo dalla CI di GitHub. La revisione di sicurezza (OWASP ASVS livello 1) la faccio io, con un
documento di esito.

## 9. Proposta per lo Sprint 10 (3 giorni, ~20 punti)

**Obiettivo proposto:** *NEXUS è pronto per i dati reali: chi dimentica la password la reimposta da solo, gli accessi
sono registrati, i dati hanno un backup fuori dal server e si anonimizzano alla scadenza, e un avviso dice se l'app non
risponde.*

| Ordine | ID | Storia | Punti | Release |
|---|---|---|---|---|
| 1 | A16 | Ripristino del deploy (azione, §8.3) | — | — |
| 2 | US-1308 | Recupero password | 2 | R4 |
| 3 | US-113 | Registro degli accessi (§8.4) | 5 | R4 |
| 4 | US-1302 | Backup del database (§8.6) | 5 | R4 |
| 5 | US-1303 | Anonimizzazione a scadenza | 5 | R4 |
| 6 | US-1307 | Monitoraggio e revisione di sicurezza (§8.7) | 5 | R4 |
| | | **Totale** | **22** | |

**Extra**, nell'ordine: US-205 *Curriculum del beneficiario* (5).

US-1308 è poco più di una configurazione di Keycloak, che già manda le email con l'SMTP di IONOS. US-1302 e US-113
richiedono passi sul server: te li do come lista completa (A15). Restano per lo Sprint 11 US-1305 (revisione UX/UI, da
raffinare insieme in una sessione dedicata) e US-1306 (passaggio in produzione).

## 10. Azioni a carico del PO

- [ ] Decidere i punti §8.1–§8.7 e confermare il planning (§9).
- [ ] Collaudo sulla versione `e30269f`: sezione 20 (T-20.2–T-20.8), poi T-13.4, sezioni 16–17, T-19.2.
- [ ] US-1301: PWA su Android e iOS (collaudo T-10.1).
- [ ] Credenziali della destinazione dei backup nel `.env` del server, quando arriva US-1302.
- [x] Prova del primo utente creato da NEXUS (T-20.1, 06/11).
