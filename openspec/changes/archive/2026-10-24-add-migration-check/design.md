## Decisions

### D1 — Confronto per nome di file, non solo per numero
Una PR rimasta indietro può avere una `V4__x.sql` mentre su `main` è già arrivata una `V4__y.sql` diversa: le nuove
migrazioni sono i **file** assenti sul branch di destinazione; ciascuna deve avere numero maggiore del massimo. I
duplicati di numero sono segnalati a parte (caso del merge commit che GitHub usa per la CI).

### D2 — Nel job Backend
Un passo in più nel job già richiesto dalla branch protection (niente nuovo controllo obbligatorio da configurare).
`fetch-depth: 0` per avere il branch di destinazione e il merge-base; il nome del branch passa da variabile d'ambiente
(niente interpolazione nello script, contro l'iniezione di comandi).

### D3 — Immutabilità
Una migrazione già unita è già applicata su almeno un database: modificarla rompe il checksum di Flyway. Lo script
rifiuta modifiche, rinomine e cancellazioni rispetto al merge-base.
