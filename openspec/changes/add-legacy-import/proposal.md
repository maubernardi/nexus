## Why

Storia **US-1309** (Sprint 15, PO 19/11). NEXUS sostituisce in tutto il database Access del Call Center
([analisi](../../../docs/analisi/database-attuale.md)). Il database contiene 13.799 aziende e 38.789 chiamate, che
devono entrare in NEXUS al passaggio in produzione (US-1306). Le storie US-508, US-509 e US-510 hanno preparato i
campi:
- partita IVA e nome da completare;
- categoria merceologica e attività;
- referenti con il ruolo, recapiti e sede operativa.

Manca l'importazione: va letta da un file che non entra mai nel repository, e va provata prima su una copia.

## What Changes

- **Importazione dal file Access** (`.mdb`), letto sul server in sola lettura. Parte con il profilo `legacy-import` e
  il percorso del file, fa il suo lavoro e chiude l'applicazione: niente interfaccia, niente attività pianificate né
  server web.
- **Aziende**: tutte e 13.799.
  - Nome, oppure «Azienda senza nome n. ‹numero›»; partita IVA «00000000000» (US-508).
  - Indirizzo e comune della sede legale e della sede operativa. Il comune si abbina all'elenco ufficiale anche con la
    sigla della provincia in coda, con un quartiere dopo il nome, con i nomi tagliati a 20 caratteri e con una
    tabella di alias per abbreviazioni, frazioni e comuni fusi.
  - Recapiti:
    - telefono, cellulare e fax;
    - email ripulite dal formato dei collegamenti di Access;
    - seconda email.
  - Quello che non sta nei campi (testi accanto ai numeri, indirizzi non validi) finisce in una nota del diario, così
    non si perde.
  - Referenti con il ruolo (US-510), categoria e attività (US-509).
  - La nota di Access nel diario dell'azienda, con la data originale.
  - Nell'audit di ogni azienda l'evento `IMPORT` con il numero di Access.
- **Chiamate**: tutte e 38.789, nella tabella di archivio `legacy_call` (V48), con tutti i loro dati.
  - I codici diventano testi: esito, progetto, operatore.
  - Ogni chiamata è collegata all'azienda quando esiste.
  - Non compaiono nell'interfaccia.
- **Ripetibile**: ogni riga ha un id fisso ricavato dal numero di Access. Una seconda esecuzione non duplica nulla e
  non cambia i dati già corretti a mano.
- **Resoconto** in un file accanto a quello di Access, senza dati personali:
  - righe lette ed entrate;
  - da rivedere: duplicati per nome, comuni non trovati, aziende senza nome, chiamate con possibili nomi di
    beneficiari;
  - per ogni azienda il numero di Access e il collegamento alla scheda in NEXUS.
  - Nel log vanno solo i conteggi.
- **Autore** delle note importate: «Database attuale».
- **Procedura** per la prova in locale e per il server, nell'analisi del database attuale.

## Non-goals

- **L'esecuzione in produzione**: si fa con il passaggio in produzione (US-1306). Sulla demo restano i dati
  dimostrativi.
- **Profili, candidature, operatori, progetti** del database attuale: restano fuori (analisi, §6.4).
- **Le chiamate nell'interfaccia** e le richiamate: l'archivio serve se servirà (PO, 19/11).
- **Correggere i dati**: duplicati e comuni non trovati si sistemano a mano dopo, con il resoconto.

## Capabilities

### New Capabilities
- `legacy-import`: importazione delle aziende e delle chiamate dal database attuale, con il resoconto.

### Modified Capabilities
<!-- nessuna: i campi dell'azienda sono di US-508, US-509 e US-510 -->

## Impact

- Migrazione **V48**, solo additiva: tabella `legacy_call`, con vincoli con nome testati in `DomainConstraintsIT`.
- Backend:
  - dipendenze nuove **Jackcess** e **jackcess-encrypt** (Apache 2.0), per leggere il file Access codificato senza
    strumenti esterni, con BouncyCastle all'ultima versione stabile;
  - profilo `legacy-import`;
  - le attività pianificate si spengono con `nexus.scheduling.enabled=false`.
- Frontend: nessun impatto, salvo l'autore «Database attuale» nel diario, che arriva dal backend.
- Dati personali:
  - il file e il resoconto restano sul server;
  - il resoconto contiene numeri, conteggi e nomi di comuni;
  - i test usano un file Access costruito al momento, con dati inventati.
