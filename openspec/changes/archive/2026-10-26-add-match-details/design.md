## Decisions

### D1 — Solo frontend
Le API esistono già (scheda azienda, note, storico): il dialogo le compone, senza nuovi endpoint.

### D2 — Ritorno del focus esplicito
Radix riporta il focus al pulsante di apertura, ma il comportamento non era affidabile con il dialogo controllato
dall'esterno: il dialogo riceve il pulsante di partenza e alla chiusura vi porta il focus; se si propone, il focus va
invece alla conferma. Il contenuto resta montato durante la chiusura.

### D3 — Estratti, non pagine intere
Nel dialogo le ultime 3 note e gli ultimi 5 eventi bastano a decidere; il link "Apri la scheda completa dell'azienda"
porta al resto.

### D4 — Riepilogo secondo la sezione
Le sei caselle del riepilogo usano container query: 2 colonne su schermi stretti, 3 nel dialogo, 6 nella scheda azienda
su schermo largo, senza testo che deborda.
