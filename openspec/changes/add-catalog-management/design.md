## Decisions

### D1 — Un servizio per due cataloghi
Zone e tipologie hanno la stessa forma (`AbstractReferenceEntity`: codice, nome, attiva) e le stesse regole. Il
servizio è uno solo e sceglie il repository dal catalogo (`CatalogKind`, che dà anche il tratto dell'indirizzo e il
tipo nell'audit). Un terzo catalogo, per esempio i progetti con US-102, si aggiunge come voce dell'enum.

### D2 — Il codice non cambia
Il codice è un identificativo stabile: lo usano dati demo, migrazioni e integrazioni. Si ricava dal nome alla
creazione (maiuscole, senza accenti, underscore, suffisso se già usato) e un nuovo nome non lo tocca.

### D3 — Nome unico tra le voci attive
Due voci attive con lo stesso nome confonderebbero chi sceglie da un menu. Una voce disattivata può avere il nome di
una nuova voce, così si può sostituire una voce senza perdere la storia. Il vincolo sta nel database (indice parziale
su `lower(name) WHERE active`) e il servizio risponde prima, sul campo, con il motivo.

### D4 — Disattivata resta sui dati
Non si cancella nulla. I moduli che mostrano una voce già scelta la tengono anche se disattivata: beneficiario (zona)
e mansione (zona e tipologia) lo facevano già. I menu per le scelte nuove mostrano solo le attive.

### D5 — Nessun blocco ottimistico
Le tabelle dei cataloghi non hanno la colonna `version` e li modifica il solo ADMIN: se due ADMIN rinominano la
stessa voce insieme, vale l'ultimo nome, e l'audit li registra entrambi. Aggiungere la versione costerebbe una
migrazione su tabelle usate ovunque, senza un caso reale.
