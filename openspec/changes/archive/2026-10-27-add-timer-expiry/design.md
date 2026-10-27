## Decisions

### D1 — Transizioni del sistema separate
La scadenza non è un'azione di un ruolo: una transizione `bySystem` non ha ruoli, `allowedFor` la nega a ogni utente e
`applyBySystem` la accetta solo senza utente collegato (attività pianificata). Nessun endpoint può far scadere un timer.

### D2 — Nessuna verifica di versione
Il sistema parte dallo stato attuale: se nello stesso istante il Tutor registra l'appuntamento, il conflitto emerge al
salvataggio e il controllo dell'ora successiva trova il timer già fermo.

### D3 — Ordine nel controllo
Prima le scadenze, poi i promemoria (US-704, design D4): la segnalazione scaduta esce dalle proposte accettate e non
riceve il promemoria.

### D4 — Nessuna esclusione dell'azienda
La scadenza dipende dall'inattività, non dall'azienda: l'azienda resta proponibile (come per l'appuntamento fallito).
