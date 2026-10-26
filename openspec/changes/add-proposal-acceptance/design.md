## Decisions

### D1 — Solo il Tutor della segnalazione
È il Tutor che segue il beneficiario a decidere se accettare: i colleghi di progetto vedono la proposta ma non la
accettano, e nemmeno il Call Center o l'ADMIN (proposta del team, da confermare dal PO: in alternativa anche i colleghi
di progetto, come per la lavorazione condivisa del Call Center).

### D2 — Scadenza come istante, mostrata come giorno
`timer_deadline_at` è la mezzanotte di Roma alla fine del 7° giorno lavorativo: lo scheduler del timeout (US-705)
confronterà istanti; l'interfaccia mostra l'ultimo giorno utile. Il promemoria del 4° giorno (US-704) si ricava dalla
data di inizio con lo stesso calcolo.

### D3 — Orologio iniettato
Un `Clock` con il fuso di Roma rende "oggi" indipendente dal fuso del server.

### D4 — Conferma prima dell'accettazione
Come per la proposta del Call Center, l'accettazione avvia un effetto concreto (il timer): serve una conferma esplicita.
