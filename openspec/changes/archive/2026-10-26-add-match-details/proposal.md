## Why

Storia **US-604** (Sprint 4), dalla nota operativa 5 del PO: durante l'abbinamento l'operatore deve vedere, in un
popup, la scheda dell'azienda, la mansione, le note e i pregressi, per scegliere la proposta con tutte le informazioni.
Con questa storia si chiude il rilascio R1 (resta US-1301, a cura del PO).

## What Changes

- Nella tabella delle mansioni compatibili, pulsante **"Dettagli"** accanto a "Proponi" (nome accessibile con mansione
  e azienda).
- **Dialogo modale** con: scheda dell'azienda (ragione sociale, partita IVA, sede, referente e contatti, stato), scheda
  della mansione (titolo, tipologia, zona, descrizione), **note recenti** (US-505, le ultime 3, con link alla scheda
  completa) e **storico e pregressi** con riepilogo (US-503, gli ultimi 5 eventi).
- Da qui **"Proponi"** apre la stessa conferma della tabella (nessuna proposta senza conferma).
- Accessibilità: focus nel dialogo e intrappolato, chiusura con Esc o "Chiudi", ritorno del focus su "Dettagli"; a
  schermo intero sotto i 640 px.
- Componente `ui/dialog` (shadcn su `radix-ui`, già tra le dipendenze); la sezione dello storico diventa riusabile
  (livello del titolo, numero di eventi) e dispone il riepilogo secondo la larghezza della sezione.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `job-matching`: dettagli di azienda e mansione durante l'abbinamento.
