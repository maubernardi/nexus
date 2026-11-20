# Sprint 16 — 21 – 23 novembre 2026

**Stato:** 📋 proposta di planning, 13 punti: da confermare le scelte del §«Da confermare». **Planning:** 20/11, dalla
[review dello Sprint 15](sprint-15-review.md) (§8, §9).

## Obiettivo

> NEXUS va in produzione sul server di oggi, con un backup giornaliero cifrato fuori dal server e provato con un
> ripristino. La produzione tiene la configurazione della demo e prende le aziende dal database attuale; parte senza
> segnalazioni, beneficiari e utenti, con la sola utenza dell'ADMIN.

## Impegno (13 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| US-1302 | Backup del database | 5 | ✅ Ready | `add-database-backup` | |
| US-1306 | Passaggio in produzione | 8 (era 5) | ✅ Ready | `prepare-production` | |

Alla review avevamo detto 10 punti. Scrivendo i criteri, US-1306 è salita a 8: il suo criterio unico nascondeva la
pulizia della demo, il realm senza utenti, la creazione dell'ADMIN, la procedura, l'importazione sul server e il cambio
di nome. Resta sotto la velocity media (23,1), e il margine serve per correggere quello che emerge dal collaudo.

**Ordine:**
1. **US-1302, provata sulla demo.** Nessun dato reale in gioco. Senza un ripristino riuscito non si va in produzione.
2. **Collaudo T-22 … T-25 sulla demo**, a cura tua, con la data che decidiamo in corsa (review, §8.5). Dopo il
   passaggio la demo non c'è più.
3. **US-1306 in due tempi:**
   - prima la preparazione (script di pulizia, realm senza utenti, creazione dell'ADMIN, procedura, nomi), provata in
     locale su una copia del backup della demo e unita come ogni PR;
   - poi il passaggio vero sul server, nel giorno che scegli, seguendo la procedura insieme.

   Se il collaudo non finisce in tre giorni, il passaggio slitta e la storia si chiude quando è fatto.

Prossima migrazione: **V50**; per queste storie non dovrebbe servirne una.

Ogni storia con interfaccia nuova si controlla in Chrome **prima** della PR (A26). Se l'estensione si blocca, il
controllo si fa con Chrome senza finestra e un profilo pulito (A34). Per attendere la CI uso solo
`gh pr checks N --watch` (A36).

## Decisioni del planning (20/11)

- **Dati della produzione** (PO): si riporta tutto dalla demo, tranne segnalazioni, utenti e beneficiari. Le aziende
  vengono dal database attuale (US-1309): quelle della demo si tolgono. Sostituisce la proposta di un'installazione
  pulita: la configurazione non si rifà.

## Come li faccio

**Backup (US-1302):**
- un servizio nel compose del server, quindi arriva con il deploy: sul server non si installa nulla a mano;
- ogni notte salva i due database (NEXUS e Keycloak), li cifra con la chiave pubblica e li carica su IONOS;
- tiene le ultime 30 copie e scrive l'esito, che il controllo del mattino legge;
- l'ultimo esito si legge anche da «Stato del server».

**Passaggio in produzione (US-1306):**
- resta tutta la configurazione della demo, con le impostazioni di Keycloak;
- uno script, una volta sola e in una transazione, toglie segnalazioni (con bacheca, schede, report, storico,
  notifiche), beneficiari, utenti di NEXUS e di Keycloak e aziende della demo (con referenti, diario, storico e
  mansioni);
- storico dell'azienda, diario, report di chiusura e audit rifiutano le cancellazioni (EN-2): lo script sospende queste
  regole solo dentro la sua transazione. Se qualcosa va storto, la transazione si annulla e non cambia nulla;
- lo provo in locale su una copia del backup della demo: è il primo uso di US-1302, e nel backup ci sono solo dati
  dimostrativi;
- dopo la pulizia, l'importazione del database attuale (analisi, §9.2);
- il backend parte senza il profilo `seed`: dalla cronologia di Flyway tolgo le voci degli script dimostrativi,
  altrimenti non partirebbe;
- Keycloak importa un realm senza utenti di prova;
- al primo avvio NEXUS crea l'ADMIN dall'email nel `.env`, con l'invito di US-103;
- la procedura parte da un backup della demo, per poter tornare indietro, e finisce con il controllo di sicurezza.

## Da confermare

1. **Audit della demo**: lo script toglie anche le voci dell'audit su segnalazioni, beneficiari, utenti e aziende tolte;
   restano quelle sulla configurazione che teniamo. È l'unica volta che l'audit si tocca, prima che ci siano dati veri.
   *Alternativa:* tenerlo tutto, con la storia delle prove fatte sulla demo.
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
- il collaudo T-22 … T-25. Segnalazioni, beneficiari, utenti e aziende che crei per le prove li toglie la pulizia; la
  configurazione invece passa in produzione così com'è, quindi le voci create solo per prova vanno tolte a mano;
- i comuni completi sulla demo (file ISTAT, US-114), se non li hai già caricati: l'importazione li usa per le sedi;
- il controllo in Access delle 7 chiamate con nome e cognome e delle 20 con possibili nomi;
- l'email dell'ADMIN nel `.env`;
- la copia del file Access sul server (analisi, §9.2).

**Dopo il passaggio:**
- gli account degli utenti;
- i 30 giorni dei backup nel registro dei trattamenti, con il DPO.

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (dopo la conferma del planning) | 13 |
