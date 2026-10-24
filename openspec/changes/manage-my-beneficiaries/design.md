## Decisions

### D1 — Visibilità per progetto calcolata dalle segnalazioni
Un Tutor vede un beneficiario altrui se il beneficiario ha almeno una segnalazione (anche conclusa) in un progetto a cui
il Tutor è assegnato. Non serve un legame diretto beneficiario–progetto: è la segnalazione a portarlo nel progetto.
Il controllo è nell'API; per chi non ha diritto la risposta è `404`, uguale a "inesistente".

### D2 — "I miei beneficiari" resta del proprietario
L'elenco mostra solo i beneficiari del Tutor (US-202); quelli dei colleghi di progetto si raggiungono dalle segnalazioni.

### D3 — Audit con i soli nomi dei campi
Come per la registrazione, nell'audit non finiscono valori personali: l'evento `UPDATE` elenca i campi cambiati
(`redacted`). Autore e data li aggiunge l'audit (EN-2).

### D4 — Niente blocco ottimistico
A differenza delle aziende (modificate da più operatori), un beneficiario lo modifica solo il suo Tutor: il rischio di
modifiche concorrenti è trascurabile e non giustifica una migrazione per la colonna `version`.

### D5 — Lingue aggiornate sul posto
Togliere e reinserire la stessa lingua violerebbe `uq_candidate_language`, perché Hibernate esegue gli inserimenti prima
delle cancellazioni: le lingue esistenti si aggiornano, le mancanti si tolgono, le nuove si aggiungono.

### D6 — Zona disattivata
Se la zona del beneficiario è stata disattivata, in modifica resta selezionabile e valida; una zona disattivata diversa
dall'attuale è rifiutata.

### D7 — Ricerca nel browser
Un Tutor ha pochi beneficiari: l'elenco arriva intero e la ricerca filtra nel browser (parametro `q` nell'indirizzo, come
per le aziende).
