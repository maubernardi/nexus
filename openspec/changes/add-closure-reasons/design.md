## Context

- I cataloghi dell'ADMIN (US-106, US-109) sono tabelle con codice, nome e attivo. Il servizio dei cataloghi li gestisce
  in modo generico (`CatalogKind`), e la pagina dei Cataloghi li mostra con `CatalogSection`.
- Il report di restituzione (US-1101) non si modifica dopo l'invio. Dallo Sprint 13 ha la fine effettiva (US-1009),
  facoltativa nel database per i report di prima e per A16.

## Goals / Non-Goals

**Goals:** motivo di chiusura in ogni report nuovo, con le voci gestite dall'ADMIN.

**Non-Goals:** segnaposto della relazione finale (US-1011).

## Decisions

### D1 — Catalogo `closure_reason`, la chiusura regolare riconosciuta dal codice
Tabella come `no_hire_reason`, con le tre voci iniziali in V41. La chiusura regolare ha il codice fisso
`CHIUSURA_REGOLARE`: il servizio dei cataloghi rifiuta di disattivarla (`409`), mentre il nome si può correggere.
Le interruzioni sono tutte le altre voci attive (`GET /reference/interruption-reasons`).
*Alternativa scartata:* tre valori fissi nel codice. Il PO ha chiesto un catalogo (15/11).

### D2 — Colonna facoltativa nel report
`closure_report.closure_reason_id` è senza `NOT NULL`: i report già inviati non hanno il motivo, e la release
precedente inserisce report senza la colonna (A16). L'obbligo sta nel servizio.

### D3 — La regola nel servizio
La fine prevista è quella della scheda del tirocinio. Con US-1012 diventerà quella dell'ultima convenzione.
- **Fine effettiva prima della fine prevista:** serve `closureReasonId`, e deve indicare una voce attiva diversa dalla
  chiusura regolare. Gli errori vanno sul campo `closureReasonId`.
- **Altrimenti**, o se la scheda non ha la fine prevista: il motivo è la chiusura regolare e il valore del modulo si
  ignora.

### D4 — Cronologia e audit
Il dettaglio della chiusura nella cronologia inizia con il motivo: «Chiusura regolare; Assunto: …; mansione …».
L'audit registra `closureReason` con il codice.

### D5 — Interfaccia
- Il modulo del report conosce già la fine prevista (US-1009). Quando la fine effettiva è precedente compare la tendina
  obbligatoria «Motivo dell'interruzione anticipata», con un aiuto che ricorda la fine prevista.
- Se il server segnala l'errore sul motivo, la tendina compare comunque.
- Il report inviato mostra la riga «Motivo di chiusura».

## Risks / Trade-offs

- [La regola confronta la scheda, che si modifica sempre] → il confronto vale al momento dell'invio; il report poi non
  cambia.
