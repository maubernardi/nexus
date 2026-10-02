## Context

Motivazione: proposal.md (US-502). `job_slot` esiste (V4) con stato, blocco e `@Version`; manca un modo per ritirare una
mansione che l'azienda non offre più senza cancellarne lo storico.

## Decisions

### D1 — Ritiro invece di cancellazione
`active` sulla mansione (V13). Una mansione ritirata resta nello storico dei ticket e non sarà proposta negli
abbinamenti. Vincolo nel database: ritirata ⇒ LIBERA. Il servizio spiega il rifiuto citando la segnalazione che blocca.

### D2 — Stato non modificabile
Il DTO del modulo non contiene lo stato; un eventuale campo `status` inviato è ignorato. Solo l'abbinamento (US-602) e
le chiusure cambiano LIBERA/BLOCCATA.

### D3 — Voci di riferimento disattivate
In creazione tipologia e zona devono essere attive; in modifica è accettata anche la voce già assegnata se nel frattempo
disattivata, così si possono correggere altri campi senza forzare un cambio.

### D4 — Interfaccia
Sezione nella scheda azienda (niente pagine nuove): pannello di modifica in linea con titolo focalizzato all'apertura;
dopo il salvataggio, il ritiro o un conflitto il focus va al messaggio di esito. I pulsanti di riga hanno nome
accessibile univoco ("Modifica Magazziniere", "Ritira Magazziniere").
