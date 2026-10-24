## Decisions

### D1 — Una sola API con viste
Le viste sono un enum con i ruoli ammessi (`TicketListView`); il servizio sceglie la vista predefinita per ruolo e
compone Specification (`fetchListData` + filtri + vincoli della vista) con l'ordinamento della vista. Così i filtri si
combinano senza moltiplicare query JPQL; il fetch join evita N+1.

### D2 — Visibilità
`PROJECT` usa i progetti assegnati (`user_project`): un Tutor senza progetti ottiene un elenco vuoto, mai segnalazioni di
altri progetti. Le segnalazioni dei colleghi sono complete (nome e cognome, Q11) ma senza azioni.

### D3 — Indirizzi italiani e retrocompatibili
Parametri leggibili (`vista=da-prendere`, `stato=NUOVA`, `progetto=…`); i vecchi `/coda` e `/lavorazioni` reindirizzano.

### D4 — Nessuna richiesta prima del ruolo
Finché il profilo non è caricato la pagina non chiede né elenco né progetti: altrimenti un Tutor chiederebbe per un
istante la vista del Call Center (400) e `/reference/projects` (403).
