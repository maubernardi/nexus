## Decisions

### D1 — Due fasce per tirocinio, giorni con le fasce scelte e le ore
Il PO (29/10) vuole fasce da scegliere giorno per giorno, con le ore del giorno se diverse dalle fasce intere. Le fasce
sono due per tirocinio, precompilate con 9:00–13:00 e 14:00–18:00 e modificabili nella scheda. Questa è la "fascia di
default" che si può cambiare.

Ogni giorno della settimana (da lunedì a domenica) può usare la prima fascia, la seconda o entrambe. Le ore del giorno
sono facoltative:
- vuote, valgono le fasce intere;
- indicate, non superano la durata delle fasce scelte. Così si esprime il "9–13 e 14–18, ma al massimo 3 ore al
  giorno" dell'esempio del PO.

Un default delle fasce comune a tutti i tirocini, deciso dall'ADMIN, non c'è: le fasce precompilate sono fisse nel
database. Si potrà aggiungere se servirà.

### D2 — Ore come minuti
Le ore del giorno si salvano in minuti (`minutes`), così le fasce restano libere al minuto (es. 8:30–12:45). Nel modulo
si scrivono in ore, a quarti d'ora (es. 3 oppure 2,5).

### D3 — Monte ore calcolato, non salvato
Il monte ore si calcola a ogni lettura, quindi segue sempre griglia, date e sospensioni ("si ricalcola quando
cambiano"). Il calcolo:
- per ogni giorno dall'inizio alla fine prevista, estremi compresi, conta le ore del giorno della settimana;
- salta i giorni dei periodi di sospensione;
- se la casella è attiva, salta le festività nazionali (`ItalianHolidays`, Pasquetta compresa).

Il patrono non si esclude, perché dipende dalla sede dell'azienda: se serve si inserisce come sospensione. Senza date
o senza griglia, il monte ore non c'è e la pagina spiega che cosa manca. Il calcolo vive in `InternshipHours`, una
classe pura testata con test unitari.

### D4 — Il campo "Ore settimanali" diventa il totale della griglia
Il numero inserito a mano in US-1001 sarebbe in contraddizione con la griglia, quindi V24 toglie `weekly_hours`. Lo
fa insieme al vincolo `ck_internship_weekly_hours`, perché le schede esistono solo in locale: la demo è ancora allo
Sprint 6. Il limite di 40 ore, ancora un segnaposto da confermare con i moduli (Q5), vale per il totale settimanale.

### D5 — Vincoli
- **Nel database**:
  - fasce in ordine (`ck_internship_slots`);
  - un giorno una sola volta (`uq_internship_schedule_day`);
  - giorno da 1 a 7;
  - almeno una fascia;
  - minuti positivi.
- **Nell'applicazione**, perché incrociano le tabelle:
  - le ore non oltre le fasce scelte;
  - il totale settimanale.

Le righe dei giorni si sostituiscono in blocco, come tutor e sospensioni, con il `flush` prima degli inserimenti:
l'indice unico non deve vedere le righe vecchie.

### D6 — Appuntamento fallito
La griglia riguardava quell'azienda: con l'appuntamento fallito si azzerano giorni e fasce (di nuovo quelle
precompilate) e la casella delle festività torna attiva, come gli altri dati del tirocinio (US-1001, D7).

### D7 — Orari delle fasce in minuti, non `time`
L'applicazione ha `hibernate.jdbc.time_zone: UTC`, che serve per gli istanti. Applicato a una colonna `time`, quel
fuso spostava le fasce: 9:00 salvate dal default del database tornavano come 10:00, e con l'ora legale lo spostamento
cambierebbe tra estate e inverno. La prova sull'app reale l'ha mostrato. Le fasce si salvano quindi come minuti dalla
mezzanotte (`smallint`, 9:00 = 540) con `MinuteOfDayConverter`; nel codice e nell'API restano orari. Un test
d'integrazione rilegge la scheda dal database e controlla il valore salvato.
