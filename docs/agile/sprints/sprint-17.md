# Sprint 17 — 24 – 26 novembre 2026

**Stato:** 🏃 in corso: planning confermato dal PO il 23/11, 13 punti; 16 con US-1310 e 19 con US-513, aggiunte il
24/11.
**Planning:** 23/11, dalla [review dello Sprint 16](sprint-16-review.md) (§8, §9).

## Obiettivo

> NEXUS è in produzione sul server di oggi, con i dati veri, il backup notturno e PostgreSQL 19.

## Impegno (19 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| US-1306 | Passaggio in produzione | 8 | 🔨 In corso | `prepare-production` | [#177](https://github.com/maubernardi/nexus/pull/177) (preparazione) |
| EN-10 | Aggiornamento a PostgreSQL 19 | 5 | 🎉 Done | `upgrade-postgresql-19` (archiviata) | [#189](https://github.com/maubernardi/nexus/pull/189), [#190](https://github.com/maubernardi/nexus/pull/190) |
| US-1310 | Sessione attiva finché NEXUS è aperto | 3 | 🔨 In corso | `keep-session-alive` | [#197](https://github.com/maubernardi/nexus/pull/197) |
| US-513 | Elenco delle aziende a pagine | 3 | 🔨 In corso | `paginate-company-list` | in arrivo |

**Ordine** (PO, 23/11):
1. **EN-10 sulla demo**, anticipata al 23/11 sera (PO):
   - la PR, provata in locale sul backup vero della demo;
   - il deploy e lo script dell'aggiornamento sul server.
2. **Collaudo T-24, T-25, T-28 e T-29** sulla demo già aggiornata, a cura tua: finito il 23/11 sera, tutto riuscito.
3. **US-1306 il 25 o il 26/11**:
   - il passaggio sul server, già su PostgreSQL 19;
   - l'invito all'ADMIN;
   - poi la seconda PR: profilo predefinito, realm e password demo, nomi «produzione», controllo ASVS.
4. **Analisi di US-507**, in una sessione dopo il passaggio (review, §8.5). Anticipata e fatta il 23/11 sera: tre
   storie, 15 punti, da pianificare (sotto, «Analisi di US-507»).

**Perché EN-10 prima del passaggio:**
- il cambio di versione principale tocca solo i dati dimostrativi;
- la produzione nasce sulla 19: i dati veri non passano mai da una versione all'altra;
- il collaudo si fa sulla versione che userete.

Se l'aggiornamento non riuscisse, si torna alla 18 con il deploy del commit precedente, e il passaggio si fa comunque.

Prossima migrazione: **V52**; per queste storie non dovrebbe servirne una.

Ogni storia con interfaccia nuova si controlla in Chrome **prima** della PR (A26). Per attendere la CI uso solo
`gh pr checks N --watch` (A36). I container di prova con dati si spengono con `docker rm -f -v` (A37).

## Decisioni del planning (23/11)

Dalla review dello Sprint 16, tutte del PO:
- **Prima il collaudo**, poi il passaggio, indicativamente il **25–26/11** (§8.2).
- **PostgreSQL 19 come storia a sé**, EN-10, nello Sprint 17; le PR di Dependabot #182–#184 non si uniscono (§8.3,
  §8.4).
- **Analisi di US-507** nello Sprint 17, dopo il passaggio (§8.5).
- **Ordine**: EN-10 prima del passaggio, sulla demo; poi collaudo e passaggio, già su PostgreSQL 19 (planning, 23/11).
- **US-1310 nello sprint** (24/11): il PO segnala che dopo un po' di inattività NEXUS chiede di nuovo la password, un
  problema per il Call Center. Tra tre strade sceglie la sessione attiva finché la pagina è aperta, con l'avviso prima
  delle 10 ore. Lo sprint passa a 16 punti.
- **US-513 nello sprint** (24/11): nel collaudo dopo il passaggio (T-27.2) il PO trova lento l'elenco delle 13.799
  aziende, che arrivano tutte insieme. Chiede le pagine, con 10, 25, 50 o 100 aziende scelte da un menu. Lo sprint passa
  a 19 punti.

## Come li faccio

**Aggiornamento a PostgreSQL 19 (EN-10):**
- una PR porta la 19 in tutti i punti:
  - compose del server e di sviluppo;
  - immagine del backup;
  - test;
  - ripristino di prova.
- **scoperta del 23/11**: la 19 non parte sul volume della 18. Trova i dati della 18 e si ferma, e chiede
  `pg_upgrade`. Quindi la 19 va su un **volume nuovo**, `postgres19-data`, come proponeva la review (§8.3). La 18
  resta intatta nel suo volume, ed è il ritorno indietro;
- uno script per il server, `aggiorna-postgresql.sh`, lanciato da te con sudo **prima del deploy**:
  - fermo del sito;
  - backup su IONOS;
  - copia dei due database;
  - ripristino nella 19, sul volume nuovo;
  - confronto dei numeri.

  Il deploy poi trova la 19 già pronta: backend e Keycloak non partono mai su un database vuoto;
- dopo il deploy: `verifica`, che rifà il confronto e un backup; dopo il passaggio in produzione: `pulisci`, che toglie
  il volume della 18;
- la CI prova lo script a ogni PR, ritorno alla 18 compreso. In locale è già verde;
- il sito resta fermo dallo script alla fine del deploy: circa 10–15 minuti.

**Passaggio in produzione (US-1306):** lo script e la procedura sono pronti e provati sul backup vero della demo
(Sprint 16). Restano:
- il passaggio, il giorno che scegli, seguendolo insieme;
- l'invito all'ADMIN e i controlli del README;
- la seconda PR.

## Per il PO

**Per EN-10 (24/11):**
- il ripristino di prova dell'ultimo backup con la 19, sul PC;
- sul server, con sudo: `prepara` prima del deploy e `verifica` dopo. I comandi sono nella PR;
- la demo resta ferma per 10–15 minuti.

**Prima del passaggio (25–26/11):**
- ✅ il collaudo T-24, T-25, T-28 e T-29 (23/11);
- ✅ i dati dell'ADMIN nel `.env`, arrivati al backend con il deploy `1890dd9` (23/11);
- ✅ `nexus-deploy` reinstallato, con il realm di produzione (23/11);
- ✅ il controllo in Access delle 7 chiamate con nome e cognome e delle 20 con possibili nomi: nessun nome di
  beneficiario (23/11);
- ⏳ il file Access sul server: una copia fresca, il giorno del passaggio.

**Dopo il passaggio:**
- gli account degli utenti;
- con US-1310: il blocco automatico dello schermo dei PC del Call Center, dopo pochi minuti, con chi gestisce i PC;
- i 30 giorni dei backup nel registro dei trattamenti, con il DPO.

**Da tenere d'occhio:** il controllo del backup delle 9:07. Il 22/11 GitHub l'ha fatto partire alle 17:06; il 23/11
alle 10:25 non era ancora partito. Se continua, propongo un avviso mandato dal server stesso, con la posta di IONOS:
una storia piccola.

## Analisi di US-507 (23/11)

Oggi la richiesta passa a voce o per email, senza traccia. Decisioni del PO:
- la richiesta è **generale**, non per un beneficiario;
- **azienda nuova**: nome e zona del catalogo obbligatori, settore facoltativo. **Azienda già presente**: «Chiedi di
  contattarla» dalla sua scheda;
- la prende in carico **un operatore qualsiasi**; il Call Center vede le richieste nel suo elenco, senza email;
- **più tentativi**, ognuno con data, esito (elenco fisso) e nota; la richiesta la **chiude a mano** l'operatore;
- il Tutor vede **tutti i tentativi** e riceve l'esito nell'app e per email;
- il Tutor **vede le aziende** con pochi dati: nome, settore, zona, indirizzo;
- l'azienda **ha una zona** sua, del catalogo. Il collegamento tra zone e comuni verrà in un secondo momento.

Ne escono tre storie nel backlog, 15 punti, senza release, da pianificare:

| ID | Storia | Punti |
|---|---|---|
| US-511 | Zona dell'azienda e aziende per il Tutor | 5 |
| US-507 | Richiesta di contatto di un'azienda dal Tutor | 5 |
| US-512 | Tentativi di contatto ed esito della richiesta | 5 |

Le ho proposte io, da confermare:
- i nomi simili mostrati prima dell'invio, contro i doppioni;
- il collegamento all'azienda del database alla chiusura.

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (23/11, dopo la conferma del planning) | 13 |
| EN-10 sulla demo (23/11, sera: `prepara`, deploy `47fe414`, `verifica` con gli stessi numeri e il backup); Done dopo il controllo del mattino del 24/11 | 13 |
| US-1310 aggiunta dal PO (24/11, mattina) | 16 |
| EN-10 Done: backup delle 3:30 riuscito sulla 19 e controllo verde (24/11) | 11 |
| US-513 aggiunta dal PO dopo il passaggio (24/11) | 14 |
