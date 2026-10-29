## Decisions

### D1 — Una voce in più di `CatalogKind`
Il catalogo riusa tutto quanto fatto per US-106: entità di riferimento, servizio, risorsa e componente del frontend.
Servono solo la tabella, l'entità, il repository e una voce dell'enum (`INTERNSHIP_TYPES`, indirizzo
`internship-types`).

### D2 — Voci iniziali nella migrazione
Le quattro voci sono dati reali di partenza, come le zone (EN-3): stanno nella migrazione, quindi presenti in ogni
ambiente, con un inserimento che non tocca un codice già presente.

### D3 — I parametri del tipo arriveranno dopo
Il PO ha detto che il tipo cambia i documenti e che "ci saranno diversi altri parametri". Per ora il tipo è solo una
voce di catalogo. Quando i parametri saranno noti, diventeranno colonne o una tabella collegata, senza cambiare il
modo in cui il Tutor lo sceglie.
