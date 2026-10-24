## Decisions

### D1 — Tabella dedicata in sola aggiunta
Le note non sono eventi dello storico (US-503) ma testi liberi: tabella separata, con la stessa protezione di
`company_audit_event` (trigger che rifiutano UPDATE, DELETE e TRUNCATE; entità `@Immutable`, repository senza metodi di
modifica). Una correzione si fa aggiungendo una nuova nota.

### D2 — Audit senza testo
L'evento `NOTE_ADD` registra autore e istante; il testo resta solo nella nota, che sarà oggetto delle regole di
conservazione dei dati (US-1303) insieme agli altri dati dell'azienda.

### D3 — Note anche sulle aziende disattivate
Il diario resta consultabile e aggiornabile: una nota può spiegare proprio la disattivazione.
