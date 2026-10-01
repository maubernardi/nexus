## Context

Motivazione: proposal.md (US-501). Tabella `company` esistente (V4) con P.IVA univoca e `active`; audit EN-2 disponibile.

## Decisions

### D1 — Blocco ottimistico
`V12` aggiunge `version`; il modulo invia la versione vista. Versione diversa → `409` prima di toccare i dati; il
`@Version` copre le gare tra transazioni. Stessa scelta di ticket e mansioni.

### D2 — Unicità della P.IVA
Controllo nel service con errore sul campo `vatCode` (anche in modifica, escludendo l'azienda stessa); il vincolo
`uq_company_vat_code` resta la garanzia contro le gare (`409` dal gestore globale).

### D3 — Audit
Valori prima/dopo completi, come deciso nel planning per le aziende; in modifica solo i campi effettivamente cambiati
(nessun evento se non cambia nulla). Attivazione e disattivazione sono azioni distinte.

### D4 — Interfaccia
- Ricerca in un `form role="search"` con pulsante esplicito; la casella "disattivate" si applica subito. Parametri
  nell'indirizzo (`?q=&disattivate=1`).
- Dopo la creazione si apre la scheda dell'azienda con l'esito focalizzato; nella scheda gli esiti (salvataggio,
  attivazione, conflitto) ricevono il focus. Il modulo si ricarica con la nuova versione dopo ogni salvataggio.
- Disattivazione senza conferma modale: è reversibile e il testo spiega l'effetto.
