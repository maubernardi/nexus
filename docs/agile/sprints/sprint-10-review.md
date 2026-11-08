# Sprint 10 — Review, retrospettiva e planning

**Sprint:** 7 – 9 novembre 2026 (iniziato il 06/11 sera dopo il planning, chiuso l'08/11) · **Review:** 08/11/2026 ·
**Partecipanti:** Mauro Bernardi (Product Owner), team di sviluppo (Claude Code) · **Demo:** https://portalenexus.it,
versione `ed3332b` · **Collaudo:** [docs/test/collaudo.md](../../test/collaudo.md), sezione 21 (T-21.1–T-21.5, tutte
superate).

> Documento unico e asincrono: review (§1–§6), retrospettiva (§7), decisioni da prendere (§8) e proposta di planning
> dello Sprint 11 (§9). La PR resta in **bozza** finché non abbiamo finito di discuterne (A12): le decisioni le aggiungo
> qui, poi la segno come pronta per il merge.

---

## 1. Sintesi

| | |
|---|---|
| **Obiettivo dello sprint** | NEXUS è più sicuro per i dati reali: il deploy si ripristina da solo se una release non parte, chi dimentica la password la reimposta da solo, gli accessi sono registrati, i dati si anonimizzano alla scadenza e un avviso dice se l'app non risponde. |
| **Esito** | ✅ **Raggiunto.** Tutto pubblicato e collaudato dal PO (T-21.1–T-21.5). |
| **Impegno / completato** | 19 punti al planning, 22 dopo la nuova stima di US-1303 / **22 punti** (4 storie su 4, più A16) |
| **Extra** | Nessuno: US-205 non iniziata |
| **Fuori piano** | Sottomenu «Amministrazione» dell'ADMIN (correzione, segnalata dal PO, senza punti) |
| **Velocity** | Sprint 1: 21 · 2: 25 · 3: 21 · 4: 24 · 5: 21 · 6: 20 · 7: 31 · 8: 23 · 9: 25 · 10: **22** · media 23,3 |
| **PR unite** | 13 (#110–#122): 1 correzione del deploy + 1 migrazione della prova (A16), 4 storie, 5 correzioni nate dal collaudo di US-1308 (#113–#117), 1 correzione del menu, 1 di chiusura |
| **Migrazioni del database** | 2: V30 (vuota, della prova del ripristino), V31 (conservazione dei dati dei beneficiari) |
| **Deploy della demo** | 7, tutti riusciti; più 1 fallito **di proposito** (06/11, prova di A16: il ripristino è ripartito da solo) |

## 2. Cosa è stato consegnato

| ID | Storia | Punti | PR | In una frase |
|---|---|---|---|---|
| A16 | Ripristino del deploy | — | [#110](https://github.com/maubernardi/nexus/pull/110), [#111](https://github.com/maubernardi/nexus/pull/111) | La release precedente riparte anche se quella nuova ha già applicato le sue migrazioni; provato sul server con una release fatta apposta per non partire. |
| US-1308 | Recupero password | 2 | [#112](https://github.com/maubernardi/nexus/pull/112)–[#117](https://github.com/maubernardi/nexus/pull/117) | «Password dimenticata?» con un collegamento valido 30 minuti; email di sicurezza a ogni cambio di password; tutte le email con «PORTALE NEXUS - …» e la firma. |
| US-113 | Registro degli accessi | 7 | [#118](https://github.com/maubernardi/nexus/pull/118) | Ogni accesso, riuscito o no, con data, ora e IP: per ogni utente (ADMIN), nel proprio profilo e nella pagina «Accessi non riusciti»; 12 mesi. |
| US-1303 | Anonimizzazione a scadenza | 8 | [#119](https://github.com/maubernardi/nexus/pull/119) | Ogni notte si segnano i beneficiari fermi da 2 anni; il Tutor o l'ADMIN li anonimizzano (restano le iniziali) o ne tengono i dati con un motivo. |
| US-1307 | Monitoraggio e revisione di sicurezza | 5 | [#120](https://github.com/maubernardi/nexus/pull/120) | Controllo della demo ogni 10 minuti dalle 8 alle 20 con un'email se non risponde; revisione OWASP ASVS livello 1. |
| — | Menu dell'ADMIN | — | [#122](https://github.com/maubernardi/nexus/pull/122) | Utenti, Cataloghi e Calendario nel sottomenu «Amministrazione»; menu ☰ sotto i 1024 px. |

## 3. Demo guidata

Prove nella **sezione 21 del collaudo**, tutte superate sulla demo:
- **T-21.1** password dimenticata, email di sicurezza, oggetto e firma;
- **T-21.2** registro degli accessi;
- **T-21.3** anonimizzazione («Demo Otto»);
- **T-21.4** controllo della demo, email delle Actions, avvisi di Dependabot;
- **T-21.5** menu dell'ADMIN.

## 4. Qualità

### Test automatici

| | Fine Sprint 9 | Fine Sprint 10 |
|---|---|---|
| Backend – unit | 108 | 108 |
| Backend – integrazione (PostgreSQL reale) | 362 | 378 |
| Frontend – componenti (tutti con controllo axe) | 204 | 213 |

Da segnalare:
- **Prove con Keycloak vero in CI**: il recupero della password con un server di posta di prova (testo, ora italiana,
  firma, oggetto delle email; nessuna email per una password sbagliata) e il registro degli accessi (password sbagliata,
  username inesistente, accesso riuscito, con l'IP dietro il proxy).
- **Il ripristino del deploy è provato**: un test riproduce il guasto del 05/11 e la prova sul server l'ha confermato.

### Sicurezza

Revisione **OWASP ASVS livello 1** in [`docs/security/asvs-l1.md`](../../security/asvs-l1.md): nessuna lacuna bloccante
per la demo. **L2** (avvisi sulle dipendenze) chiusa l'08/11; **L1** (password comuni) da fare (§9). Appena attivato,
**Dependabot ha segnalato 32 vulnerabilità**, tutte nel frontend: 31 in strumenti di sviluppo (server di Vite, Vitest,
jsdom, esbuild, `sharp`) che non finiscono nell'app pubblicata, 1 in una dipendenza dell'app (`follow-redirects`, via
axios, usata solo fuori dal browser). Decisione del PO: aggiornarle **tutte** come prima cosa dello Sprint 11 (§8.2).

### Difetti

| Difetto | Dove | Come è emerso | Correzione |
|---|---|---|---|
| Lo script per attivare il recupero si fermava con la posta impostata | US-1308 | PO, sul server | [#113](https://github.com/maubernardi/nexus/pull/113) |
| Campo password con «mostra»: contorno ed errore sbordavano | Tema di Keycloak | PO, screenshot | [#114](https://github.com/maubernardi/nexus/pull/114) |
| Avviso di cambio password generico, ora UTC, senza firma | Email di Keycloak | PO, email ricevuta | [#116](https://github.com/maubernardi/nexus/pull/116), [#117](https://github.com/maubernardi/nexus/pull/117) |
| Menu dell'ADMIN sovrapposto a campanella, nome ed Esci | PWA | PO, screenshot | [#122](https://github.com/maubernardi/nexus/pull/122) |
| Il ripristino automatico del 05/11 non ripartiva | Deploy | Sprint 8 | **Chiuso** con [#110](https://github.com/maubernardi/nexus/pull/110) |

## 5. Decisioni prese durante lo sprint

| # | Decisione | Presa da | Dove |
|---|---|---|---|
| D1 | Collegamento per reimpostare la password valido 30 minuti | Team | US-1308 |
| D2 | Email di sicurezza a ogni cambio di password, con username, data e ora italiane, IP; nessuna email per le password sbagliate | PO (07/11) | US-1308 |
| D3 | **Tutte** le email: oggetto «PORTALE NEXUS - …», firma «Portale Nexus» | PO (07/11) | Tutte le email |
| D4 | Registro degli accessi tenuto da Keycloak (12 mesi), letto da NEXUS; pagina dei tentativi falliti per l'ADMIN | PO (06/11) | US-113 |
| D5 | Anonimizzazione: 2 anni dall'ultima attività, iniziali, job che segna e persona che decide, motivo per tenere i dati | PO (07/11) | US-1303 (5 → 8 punti) |
| D6 | Monitoraggio dalla CI di GitHub dalle 8 alle 20 | PO (review Sprint 9) | US-1307 |
| D7 | Sottomenu «Amministrazione»; menu ☰ sotto i 1024 px per tutti | PO (08/11) | Menu |
| D8 | **Versioni sempre il più aggiornate possibile**, non più congelate; tutte le vulnerabilità aggiornate come prima cosa dello Sprint 11 | PO (08/11) | Regola del progetto (§8.2) |

## 6. Stato del prodotto rispetto alla roadmap

**R1** — sviluppo completo; resta **US-1301** (PWA su smartphone, a cura del PO: prova T-10.1).

**R2, R3** — ✅ complete.

**R4** — *produzione*: **58 punti su 76** fatti. Restano US-1302 *Backup* (5), US-1305 *Revisione UX/UI* (8), US-1306
*Passaggio in produzione* (5); con questa review entrano **EN-6** *Dipendenze aggiornate* (5) e **EN-7** *Password
comuni bloccate* (2), che portano R4 a 83 punti.

**Product backlog:** 73 elementi attivi, 268 punti; completati 68 (244 punti) negli Sprint 1–10. Con EN-6 ed EN-7:
75 elementi, 275 punti.

## 7. Retrospettiva

**Cosa ha funzionato**
- **Impegno completato**, anche con US-1303 cresciuta da 5 a 8 punti per le decisioni del PO.
- **A16 chiusa per davvero**: un test che riproduce il guasto e una prova sul server fatta apposta, invece di supporre.
- **Prove end-to-end con Keycloak vero in CI**: hanno trovato prima del PO problemi di cookie, a capo e fuso orario.
- **Decisioni rapide**: anonimizzazione e registro degli accessi definiti con poche domande, senza rifacimenti.

**Cosa non ha funzionato**
- **US-1308 ha avuto 5 correzioni dopo il merge** (#113–#117): uno script con un controllo sbagliato, un difetto
  grafico, testi delle email non adatti. Le ho dichiarate pronte senza aver visto la pagina né l'email vera.
- **Non posso vedere l'interfaccia**: Chrome non è collegato e in locale non ho un browser, così il menu sovrapposto e
  il campo che sbordava li hai trovati tu (§8.6).
- **Ho aggiunto una voce di menu senza controllare lo spazio**: «Beneficiari» per l'ADMIN ha fatto sbordare il menu.
- **Due PR risultavano unite ma erano ancora aperte** (#120, #123): la verifica del merge le ha fermate, ma è tempo perso
  da entrambe le parti.

**Azioni**

| # | Azione | Chi |
|---|---|---|
| A20 | Prima di dichiarare pronta una modifica visibile (pagina, tema, email), vederla: screenshot da un browser o email di prova; se non posso, dirlo nella PR | Team |
| A21 | Ogni voce di menu nuova: controllare la larghezza dell'intestazione per ogni ruolo | Team |
| A22 | Versioni aggiornate a ogni sprint (§8.2) | Team |
| A18 | (confermata) Scenari completi nelle change che modificano un requisito: l'archiviazione dello Sprint 10 è andata liscia | Team |

## 8. Da decidere

### 8.1 Chiusura dello Sprint 10
*Proposta:* Sprint 10 chiuso con velocity **22** (US-1303 a 8 punti).

### 8.2 Versioni sempre aggiornate
Hai deciso (08/11) che le versioni devono essere sempre il più aggiornate possibile. Da precisare:
- **quali versioni**: *proposta*, l'ultima **stabile** (mai beta o RC) e, per Java e Node, l'ultima **LTS**, come avevi
  chiesto il 27/09; *alternativa*, l'ultima in assoluto anche dove esiste una LTS (oggi Java 27 invece di 25, Node 26
  invece di 24);
- **come restare aggiornati**: *proposta*, all'inizio di ogni sprint un passo di aggiornamento (EN-6 il primo) più gli
  avvisi di Dependabot; *alternativa*, le PR automatiche settimanali di Dependabot, una per gruppo di dipendenze, da
  unire tu;
- **anche le versioni principali** (es. una nuova major di React o di Spring Boot): *proposta*, sì, ciascuna in una PR a
  sé con la CI verde.

La regola va in `CLAUDE.md` al posto di «Versioni congelate»; in questa PR c'è già, da confermare.

### 8.3 Password comuni (L1)
*Proposta:* EN-7 — Keycloak rifiuta le password comuni con la sua policy `passwordBlacklist` e un elenco pubblico delle
100 000 password più usate, inserito nell'immagine di Keycloak; uno script la attiva sulla demo.

### 8.4 Backup (US-1302)
Nella review dello Sprint 9 è rimasta nel backlog. Serve per la produzione (US-1306): *proposta*, nello Sprint 11 con lo
spazio di archiviazione **IONOS** (30 backup giornalieri, cifrati); le credenziali le metti tu nel `.env` del server.

### 8.5 Revisione UX/UI (US-1305)
Va raffinata insieme prima di stimarla davvero. *Proposta:* una sessione in cui mi dici, pagina per pagina, cosa rallenta
Tutor e Call Center; ne escono storie piccole per lo Sprint 12.

### 8.6 Vedere l'interfaccia (A20)
Per controllare pagine ed email prima di consegnarle mi serve un browser. *Proposta:* collegare l'estensione di Chrome
a Claude Code quando lavoriamo (la scelta è tua), oppure installare in locale un browser senza finestra (Chromium di
Playwright, ~150 MB) solo per gli screenshot.

## 9. Proposta per lo Sprint 11 (3 giorni, ~20 punti)

**Obiettivo proposto:** *NEXUS è aggiornato e senza vulnerabilità note, le password comuni sono rifiutate e i dati hanno
un backup fuori dal server.*

| Ordine | ID | Storia | Punti | Release |
|---|---|---|---|---|
| 1 | EN-6 | Dipendenze aggiornate: tutte le vulnerabilità e tutte le versioni all'ultima stabile (§8.2) | 5 | R4 |
| 2 | EN-7 | Password comuni rifiutate (L1, §8.3) | 2 | R4 |
| 3 | US-1302 | Backup del database (§8.4) | 5 | R4 |
| 4 | US-205 | Curriculum del beneficiario | 5 | — |
| | | **Totale** | **17** | |

**Extra:** la sessione di raffinamento di US-1305 (§8.5).

EN-6 per primo, come hai chiesto; gli aggiornamenti delle versioni principali in PR separate. US-205 introduce il
caricamento di file: la revisione ASVS (V12) va aggiornata con quella storia. Dopo lo Sprint 11 restano per R4 US-1305 e
US-1306 (passaggio in produzione).

## 10. Azioni a carico del PO

- [ ] Decidere i punti §8.1–§8.6 e confermare il planning (§9).
- [ ] Credenziali dello spazio di archiviazione dei backup nel `.env` del server, se confermi §8.4.
- [ ] US-1301: PWA su Android e iOS (collaudo T-10.1).
- [x] Collaudo dello Sprint 10 (T-21.1–T-21.5, 08/11).
- [x] Avvisi di Dependabot e notifiche email delle Actions (08/11).
