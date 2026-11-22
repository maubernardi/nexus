## Context

Vedi proposal.md, «Why». Il tirocinio (`internship`) ha la fine prevista della convenzione e le proroghe; la fine
effettiva prevista è `Internship.currentEndDate()`, cioè quella dell'ultima proroga o, senza proroghe, quella iniziale
(US-1012). La segnalazione è `IN_TIROCINIO` mentre il tirocinio è in corso, e diventa `FORM_RESTITUZIONE` con il report
di restituzione (US-1101).

Le notifiche in app hanno il tipo e la segnalazione; l'email parte da sola con lo stesso testo (US-1202). Il promemoria
del timer (US-704) segna l'invio sulla segnalazione, così non si ripete.

## Goals / Non-Goals

**Goals:** avvisi puntuali, una volta sola, che seguono le proroghe. Un blocco chiaro, che dice cosa chiudere e porta
alla segnalazione.

**Non-Goals:** vedi proposal.md.

## Decisions

### D1. Quattro fasi per ogni fine prevista

- Fasi: 1 = 7 giorni prima, 2 = 3 giorni prima, 3 = 3 giorni dopo, 4 = 7 giorni dopo. Giorni di calendario, perché le
  date della convenzione sono di calendario (PO, 22/11).
- Ogni mattina la fase dovuta è la più alta già raggiunta oggi. Se è più alta dell'ultima inviata, parte il suo avviso,
  uno solo, anche se nel mezzo ne è saltato qualcuno: niente raffiche dopo un fermo.
- **Sulla scheda**: `end_reminder_for`, la fine a cui si riferisce l'ultima fase, e `end_reminder_stage`. Se la fine
  di oggi è diversa, per una proroga, si riparte da 0.
- **Due tipi di notifica**:
  - `INTERNSHIP_ENDING` per le fasi 1 e 2;
  - `INTERNSHIP_OVERDUE` per le fasi 3 e 4.

  Il testo non ha i giorni: la data è nella scheda, e la notifica in app ha solo tipo e numero della segnalazione.

### D2. Aggiornamento senza la versione della scheda

- La scheda ha il controllo delle modifiche concorrenti (`@Version`).
- Le due colonne sono mappate in sola lettura (`insertable = false, updatable = false`) e si scrivono con una query
  nativa (`markEndReminder`), che non incrementa la versione.
- Così il controllo delle 8 non fa fallire il salvataggio di un Tutor che ha la scheda aperta, e il suo salvataggio non
  riscrive le colonne con valori vecchi.

### D3. Controllo pianificato

- `InternshipEndReminderJob`, `@Scheduled` alle 8 di Roma (`nexus.internship-end-reminders.cron`, `-` nei test).
- Prima della posta delle 9 dei Tutor, e dopo il backup.
- Una sola transazione per esecuzione. Ogni avviso scrive l'audit `INTERNSHIP_END_REMINDER` sulla segnalazione, con la
  fase e la fine prevista.
- Il controllo è spento nell'istanza dell'importazione, come le altre attività pianificate.

### D4. Blocco delle nuove segnalazioni

- `InternshipEndReminderService.toClose(tutor)`: le segnalazioni `IN_TIROCINIO` del Tutor con `currentEndDate()`
  prima di oggi (Roma), ordinate per numero.
- `submit` e `submitSpecial` lo chiamano per primo e, se non è vuoto, rispondono con `ConflictException` (`409`). Il
  messaggio elenca le segnalazioni.
- `GET /api/v1/tickets/internships-to-close`, solo `TUTOR`: le pagine lo leggono prima di mostrare il modulo.
- Il giorno della fine non blocca: il blocco parte dal giorno dopo (PO, 22/11).

### D5. Interfaccia

- `InternshipsToCloseNotice`:
  - regione con titolo «Prima chiudi i tirocini finiti»;
  - una frase che spiega cosa fare;
  - l'elenco «Segnalazione n. N, fine prevista il gg/mm/aaaa», con i collegamenti.
- In «Nuova segnalazione» e nella candidatura dalla bacheca prende il posto del modulo, finché l'elenco non è vuoto.
- Durante il caricamento c'è lo stato «Caricamento…». Se la lettura fallisce si mostra il modulo, perché il backend
  blocca comunque.

## Risks / Trade-offs

- **[Server spento alle 8]**: l'avviso parte al primo controllo utile. → Al massimo un giorno di ritardo; niente
  doppioni.
- **[Fine prevista cambiata a mano senza proroga]**: gli avvisi ripartono dalla nuova data. → È quello che il Tutor
  vuole, se la convenzione è cambiata.
- **[Molti tirocini in corso]**: la lettura è di tutti i tirocini in corso, con le proroghe, una volta al giorno. → Sono
  decine, non migliaia.

## Migration Plan

- **V50** solo additiva; la release precedente ignora le colonne (A16).
- Al primo controllo dopo il deploy, i tirocini già oltre una fase ricevono un solo avviso, il più recente.
- Ritorno indietro: il deploy precedente non ha il controllo, e le colonne restano.

## Open Questions

Nessuna: destinatari, giorni e blocco sono decisi dal PO il 22/11.
