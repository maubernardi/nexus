# Sprint 16 — 21 – 23 novembre 2026

**Stato:** 📋 proposta di planning, 13 punti: da confermare le scelte del §«Da confermare». **Planning:** 20/11, dalla
[review dello Sprint 15](sprint-15-review.md) (§8, §9).

## Obiettivo

> NEXUS va in produzione sul server di oggi, con un backup giornaliero cifrato fuori dal server e provato con un
> ripristino. L'installazione è pulita, con la sola utenza dell'ADMIN, e ha dentro i dati del database attuale.

## Impegno (13 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| US-1302 | Backup del database | 5 | ✅ Ready | `add-database-backup` | |
| US-1306 | Passaggio in produzione | 8 (era 5) | ✅ Ready | `prepare-production` | |

Alla review avevamo detto 10 punti. Scrivendo i criteri, US-1306 è salita a 8: il suo criterio unico nascondeva il
realm di produzione, la creazione dell'ADMIN, la procedura, l'importazione sul server e il cambio di nome. Resta sotto
la velocity media (23,1), e il margine serve per correggere quello che emerge dal collaudo.

**Ordine:**
1. **US-1302, provata sulla demo.** Nessun dato reale in gioco. Senza un ripristino riuscito non si va in produzione.
2. **Collaudo T-22 … T-25 sulla demo**, a cura tua, con la data che decidiamo in corsa (review, §8.5). Dopo il
   passaggio la demo non c'è più.
3. **US-1306 in due tempi:**
   - prima la preparazione (realm di produzione, creazione dell'ADMIN, procedura, nomi), provata in locale e unita come
     ogni PR;
   - poi il passaggio vero sul server, nel giorno che scegli, seguendo la procedura insieme.

   Se il collaudo non finisce in tre giorni, il passaggio slitta e la storia si chiude quando è fatto.

Prossima migrazione: **V50**; per queste storie non dovrebbe servirne una.

Ogni storia con interfaccia nuova si controlla in Chrome **prima** della PR (A26). Se l'estensione si blocca, il
controllo si fa con Chrome senza finestra e un profilo pulito (A34). Per attendere la CI uso solo
`gh pr checks N --watch` (A36).

## Come li faccio

**Backup (US-1302):**
- un servizio nel compose del server, quindi arriva con il deploy: sul server non si installa nulla a mano;
- ogni notte salva i due database (NEXUS e Keycloak), li cifra con la chiave pubblica e li carica su IONOS;
- tiene le ultime 30 copie e scrive l'esito, che il controllo del mattino legge;
- l'ultimo esito si legge anche da «Stato del server».

**Passaggio in produzione (US-1306):**
- il realm di produzione è quello di oggi senza gli utenti di prova;
- il backend parte senza il profilo `seed`;
- al primo avvio crea l'ADMIN dall'email nel `.env`, con l'invito di US-103;
- la procedura parte da un backup della demo, per poter tornare indietro, e finisce con l'importazione del database
  attuale (analisi, §9.2) e il controllo di sicurezza.

## Da confermare

1. **Installazione pulita** (*proposta*), invece di togliere i dati dimostrativi dalla demo e tenere il resto.
   - Pulita vuol dire che dopo il passaggio, da ADMIN, reinserisci tutta la configurazione: comuni, progetti, tipologie
     di mansione, sedi, tipi di tirocinio per progetto, enti, promotore e variabili di GOL, modelli. Ti preparo una
     lista di controllo.
   - Rientrano anche i dati di GOL e degli enti inseriti il 19/11 e i modelli: un'ora circa di lavoro.
   - In cambio non resta niente della demo: niente aziende o utenti di prova, niente voci segnaposto, niente storia
     nell'audit.
   - *Alternativa:* pulire la demo. È più rischioso, perché l'audit non si modifica e le voci segnaposto andrebbero
     cercate a mano.
2. **Avviso del backup**: un controllo ogni mattina alle 9. Se manca un backup riuscito nelle ultime 26 ore, GitHub ti
   manda un'email, come fa oggi il controllo della demo.
3. **Protezione dei backup**: se IONOS offre il blocco degli oggetti senza costi in più, nessuno può cancellare un
   backup per 30 giorni, nemmeno chi entrasse nel server. Lo verifico all'inizio di US-1302.
4. **Stima**: 13 punti invece di 10, per US-1306 portata a 8.

## Per il PO

**Per partire con US-1302** (ti guido passo per passo quando ci arriviamo):
- in IONOS crei il contenitore, con i dati in Germania, e una chiave di accesso;
- sul tuo PC generi la coppia di chiavi di cifratura con un comando che ti do. La chiave privata va nel password
  manager e in una copia offline, mai sul server;
- metti nel `.env` del server le chiavi di IONOS e la chiave pubblica: mai in chat.

**Prima del passaggio:**
- il collaudo T-22 … T-25;
- il controllo in Access delle 7 chiamate con nome e cognome e delle 20 con possibili nomi;
- l'email dell'ADMIN nel `.env`;
- la copia del file Access sul server (analisi, §9.2).

**Dopo il passaggio:**
- la configurazione con la lista di controllo e gli account;
- i 30 giorni dei backup nel registro dei trattamenti, con il DPO.

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (dopo la conferma del planning) | 13 |
