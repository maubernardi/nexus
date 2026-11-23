# Sprint 17 — 24 – 26 novembre 2026

**Stato:** 🏃 in corso: planning confermato dal PO il 23/11, 13 punti.
**Planning:** 23/11, dalla [review dello Sprint 16](sprint-16-review.md) (§8, §9).

## Obiettivo

> NEXUS è in produzione sul server di oggi, con i dati veri, il backup notturno e PostgreSQL 19.

## Impegno (13 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| US-1306 | Passaggio in produzione | 8 | 🔨 In corso | `prepare-production` | [#177](https://github.com/maubernardi/nexus/pull/177) (preparazione) |
| EN-10 | Aggiornamento a PostgreSQL 19 | 5 | ✅ Ready | `upgrade-postgresql-19` | |

**Ordine** (PO, 23/11):
1. **EN-10 sulla demo, il 24/11**:
   - la PR, provata in locale sul backup vero della demo;
   - il deploy e lo script dell'aggiornamento sul server.
2. **Collaudo T-24, T-25, T-28 e T-29** sulla demo già aggiornata, a cura tua (24–25/11).
3. **US-1306 il 25 o il 26/11**:
   - il passaggio sul server, già su PostgreSQL 19;
   - l'invito all'ADMIN;
   - poi la seconda PR: profilo predefinito, realm e password demo, nomi «produzione», controllo ASVS.
4. **Analisi di US-507**, in una sessione dopo il passaggio (review, §8.5).

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

## Come li faccio

**Aggiornamento a PostgreSQL 19 (EN-10):**
- una PR porta la 19 in tutti i punti:
  - compose del server e di sviluppo;
  - immagine del backup;
  - test;
  - ripristino di prova.
- uno script per il server, `aggiorna-postgresql.sh`, lanciato da te con sudo, in due tempi:
  - **prima del deploy**: backup su IONOS, copia dei due database sul server, fermo del sito;
  - **dopo il deploy**: ripristino dei due database nella 19, con la procedura già provata per il backup
    (`ricrea-database.sql`, poi `pg_restore`), e riavvio;
- l'immagine di PostgreSQL tiene i dati in una cartella per versione: la 19 parte su una cartella nuova e quella della
  18 resta, ed è il ritorno indietro. Lo verifico in locale prima della PR;
- tra il deploy e il ripristino la demo resta ferma per qualche minuto.

**Passaggio in produzione (US-1306):** lo script e la procedura sono pronti e provati sul backup vero della demo
(Sprint 16). Restano:
- il passaggio, il giorno che scegli, seguendolo insieme;
- l'invito all'ADMIN e i controlli del README;
- la seconda PR.

## Per il PO

**Per EN-10 (24/11):**
- due comandi con sudo sul server, uno prima e uno dopo il deploy: te li do nella PR;
- la demo resta ferma qualche minuto.

**Prima del passaggio (25–26/11):**
- il collaudo T-24, T-25, T-28 e T-29;
- i dati dell'ADMIN nel `.env`;
- il file Access sul server;
- `nexus-deploy` reinstallato;
- il controllo in Access delle 7 chiamate con nome e cognome e delle 20 con possibili nomi.

**Dopo il passaggio:**
- gli account degli utenti;
- i 30 giorni dei backup nel registro dei trattamenti, con il DPO.

**Da tenere d'occhio:** il controllo del backup delle 9:07. Il 22/11 GitHub l'ha fatto partire alle 17:06; il 23/11
alle 10:25 non era ancora partito. Se continua, propongo un avviso mandato dal server stesso, con la posta di IONOS:
una storia piccola.

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (23/11, dopo la conferma del planning) | 13 |
