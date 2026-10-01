## Context

Motivazione: proposal.md. Ticket e beneficiari esistono (`V5`, `V6`); il deploy demo usa il profilo `seed`.

## Decisions

### D1 — Rinomina completa con una migrazione
`V8` rinomina tabelle e colonne e, con un blocco `DO`, ogni vincolo e indice che contiene `candidate` (compresi i NOT
NULL con nome generato da PostgreSQL 18). Le migrazioni già applicate non si toccano. Lo script ripetibile del seed
cambia nome (`R__seed_04_beneficiaries`): nel solo profilo `seed`, `ignore-migration-patterns: repeatable:missing`
evita che la vecchia voce della cronologia blocchi l'avvio.

### D2 — Definizione di "aperta"
Ogni stato tranne `FORM_RESTITUZIONE`, l'unico conclusivo della FSM (`TicketStatus.CLOSED`). `RIAPERTO` è aperto: il
percorso continua sullo stesso ticket.

### D3 — Doppio controllo
Il service controlla prima di creare e risponde `400` sul campo `beneficiaryId` ("ha già una segnalazione aperta (n.
N)"), così il Tutor capisce cosa succede. L'indice unico parziale nel database è l'ultima garanzia per le richieste
concorrenti: la violazione diventa `409` dal gestore globale (`DataIntegrityViolationException`).

### D4 — Dati demo
Nel seed due beneficiari avevano due ticket aperti (t1+t7, t3+t6). `V9` (versionata, quindi eseguita prima degli
script ripetibili) crea due beneficiari fittizi e vi sposta t7 e t6 **solo se quei ticket demo esistono**; su un
database di test o produzione non trova righe. Il seed è aggiornato con gli stessi valori (inserimenti idempotenti).

### D5 — Interfaccia
L'opzione di un beneficiario con segnalazione aperta è `disabled` e ne riporta il numero nel testo (il motivo è
leggibile, non solo dedotto dall'assenza); il testo di aiuto del campo spiega la regola.
