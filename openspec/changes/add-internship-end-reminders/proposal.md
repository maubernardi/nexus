## Why

Storia **US-1203** (Sprint 16, PO 22/11), da avere prima del passaggio in produzione. Oggi nessuno avvisa il Tutor
che la convenzione di un tirocinio sta per scadere, né che un tirocinio finito va chiuso. E un Tutor con tirocini
finiti e non chiusi può continuare ad aprire segnalazioni: per la cooperativa non deve poterlo fare.

## What Changes

- **Avvisi al Tutor**, notifica in app ed email, alle 8 del mattino, in giorni di calendario (PO, 22/11):
  - 7 e 3 giorni prima della fine prevista: «la convenzione è in chiusura: se serve prorogalo, altrimenti ricordati di
    chiuderlo»;
  - 3 e 7 giorni dopo la fine prevista: «chiudilo con il report di restituzione o prorogalo; finché non chiudi i
    tirocini finiti non puoi aprire nuove segnalazioni».

  La fine prevista tiene conto delle proroghe: dopo una proroga gli avvisi ripartono dalla nuova fine. Ogni avviso
  parte una volta sola. Se un giorno il controllo non gira, al successivo parte solo il più recente.
- **Blocco delle nuove segnalazioni** (PO, 22/11): dal giorno dopo la fine prevista, finché il tirocinio non è chiuso o
  prorogato, il Tutor non apre segnalazioni, né normali né dalla bacheca. Le pagine mostrano l'elenco dei tirocini da
  chiudere, con il collegamento alla segnalazione; il backend rifiuta comunque con `409` e lo stesso elenco.
- **Cronologia**: ogni avviso entra nell'audit della segnalazione.

## Non-goals

- **Avvisi al Call Center o all'ADMIN**: solo al Tutor (PO, 22/11).
- **Altri avvisi dopo i 7 giorni**: resta il blocco, che il Tutor vede appena prova a segnalare.
- **Chiusura automatica** del tirocinio: la chiusura resta il report di restituzione del Tutor (US-1101).

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `notifications`: nuovo requisito per gli avvisi di fine tirocinio.
- `ticket-submission`: nuovo requisito per il blocco con tirocini finiti da chiudere.

## Impact

- **Migrazione V50**, solo additiva: `internship.end_reminder_for` e `end_reminder_stage`, con i vincoli con nome
  testati in `DomainConstraintsIT`. Si scrivono con un aggiornamento che non tocca la versione della scheda, così il controllo
  delle 8 non fa fallire il salvataggio di un Tutor che la sta modificando.
- **Backend**:
  - `InternshipEndReminderService` e `InternshipEndReminderJob`, ogni giorno alle 8
    (`nexus.internship-end-reminders.cron`);
  - tipi di notifica `INTERNSHIP_ENDING` e `INTERNSHIP_OVERDUE`, con i testi delle email;
  - blocco in `submit` e `submitSpecial`;
  - `GET /api/v1/tickets/internships-to-close`, solo Tutor.
- **Frontend**:
  - testi delle due notifiche e della voce di cronologia;
  - `InternshipsToCloseNotice` in «Nuova segnalazione» e nella candidatura dalla bacheca.
- **Accessibilità**: l'avviso è una regione con titolo, testo ed elenco di collegamenti; test con axe.
- **PWA**: nessun impatto.
- **Passaggio in produzione**: le colonne nuove stanno nella tabella dei tirocini, che la pulizia della demo svuota già.
- **Dati personali**: notifiche, email e audit hanno solo il numero della segnalazione e le date.
