## ADDED Requirements

### Requirement: Convenzione del tirocinio
La scheda del tirocinio SHALL avere una sola convenzione, inserita dal Tutor: numero (testo, al massimo 50 caratteri),
data e data di stipula, tutti facoltativi come gli altri dati della scheda. La durata della convenzione MUST coincidere
con le date del tirocinio e la convenzione vale per un solo tirocinio: la scheda li mostra senza chiederli.

#### Scenario: Convenzione salvata
- **WHEN** il Tutor scrive il numero «2026/145», la data 20/11/2026 e la data di stipula 25/11/2026 e salva
- **THEN** riaprendo la scheda la convenzione ha quei dati e la durata è quella del tirocinio

#### Scenario: Numero di soli spazi
- **WHEN** il Tutor salva un numero di soli spazi
- **THEN** la convenzione resta senza numero

### Requirement: Progetti formativi
La convenzione SHALL avere uno o più progetti formativi, al massimo 10, numerati 1, 2, 3… nell'ordine delle date.
Ogni progetto SHALL avere data di inizio, data di fine e mansione (al massimo 200 caratteri), obbligatorie, e il
dettaglio della mansione, testo facoltativo di al massimo 2000 caratteri. Tutti gli altri dati, orario compreso, sono
quelli della convenzione e del tirocinio. Un nuovo progetto SHALL proporre come mansione quella del progetto precedente
o, per il primo, la mansione della segnalazione.

#### Scenario: Riformulazione
- **WHEN** in un tirocinio dal 1/12/2026 al 31/5/2027 il Tutor ha il progetto 1 dal 1/12/2026 al 28/2/2027 e aggiunge il
  progetto 2 dal 1/3/2027 al 31/5/2027 con un'altra mansione
- **THEN** la scheda salvata ha i due progetti numerati 1 e 2, ciascuno con le sue date e la sua mansione

#### Scenario: Progetto tolto
- **WHEN** il Tutor toglie l'ultimo progetto e salva
- **THEN** la scheda ha i progetti rimasti, con la stessa numerazione

### Requirement: Controlli sui progetti formativi
Le date di un progetto formativo MUST stare dentro quelle del tirocinio: dall'inizio alla fine prevista, estremi
compresi. Con almeno un progetto la scheda MUST avere la data di inizio e la fine prevista. Ogni progetto MUST
iniziare dopo la fine del precedente: i progetti della stessa convenzione non si sovrappongono, neanche di un giorno.
Altrimenti il salvataggio è rifiutato con l'errore sul campo e la spiegazione. Il database MUST impedire le
sovrapposizioni.

#### Scenario: Progetto fuori dal tirocinio
- **WHEN** il progetto finisce dopo la fine prevista del tirocinio
- **THEN** il salvataggio è rifiutato e la data di fine del progetto lo spiega

#### Scenario: Stesso giorno in due progetti
- **WHEN** il progetto 1 finisce il 28/2/2027 e il progetto 2 inizia il 28/2/2027
- **THEN** il salvataggio è rifiutato e la data di inizio del progetto 2 indica che deve iniziare dopo il 28/02/2027

#### Scenario: Tirocinio accorciato
- **WHEN** il Tutor anticipa la fine prevista del tirocinio a una data in cui un progetto è ancora in corso
- **THEN** il salvataggio è rifiutato e la data di fine di quel progetto lo spiega

#### Scenario: Progetti senza le date del tirocinio
- **WHEN** la scheda ha un progetto formativo ma non la data di inizio del tirocinio
- **THEN** il salvataggio è rifiutato e la data di inizio del tirocinio lo spiega

### Requirement: Durata e monte ore dei progetti formativi
Per ogni progetto salvato la scheda SHALL mostrare la durata in mesi e il monte ore, calcolati sulle date del progetto.
I mesi MUST essere arrotondati al mese più vicino, con le sospensioni comprese: 15 giorni o più contano come un mese
in più. Il monte ore SHALL seguire le regole del monte ore del tirocinio: griglia oraria, sospensioni e, se escluse,
festività nazionali. Senza griglia il monte ore del progetto MUST NOT essere indicato.

#### Scenario: Mesi arrotondati
- **WHEN** un progetto va dal 1/12/2026 al 15/1/2027 e un altro dal 16/1/2027 al 28/2/2027
- **THEN** il primo dura 2 mesi (1 mese e 15 giorni) e il secondo 1 mese (1 mese e 13 giorni)

#### Scenario: Monte ore del progetto
- **WHEN** la griglia ha 4 ore il lunedì e il martedì, le festività sono escluse e un progetto va da lunedì 7 a
  domenica 13 dicembre 2026
- **THEN** il monte ore del progetto è di 4 ore, perché martedì 8 dicembre è festivo

### Requirement: Audit della convenzione e dei progetti formativi
Ogni salvataggio che cambia la convenzione o i progetti formativi SHALL essere registrato nell'audit della
segnalazione, con autore e istante:
- la convenzione con i valori;
- i progetti con numero, date e mansione;
- il dettaglio della mansione, testo libero, solo come «cambiato».

Il motivo di una riformulazione MUST NOT essere chiesto né registrato.

#### Scenario: Nuovo progetto nell'audit
- **WHEN** il Tutor aggiunge il progetto 2 con il dettaglio della mansione e salva
- **THEN** l'audit registra l'elenco dei progetti prima e dopo, con date e mansione, e il dettaglio solo come cambiato

### Requirement: Convenzione e progetti con l'appuntamento fallito
Se l'appuntamento con l'azienda non va a buon fine, la convenzione e i progetti formativi MUST essere azzerati
insieme agli altri dati del tirocinio, perché riguardavano quell'azienda.

#### Scenario: Appuntamento fallito
- **WHEN** il Tutor registra l'appuntamento non andato a buon fine di una segnalazione con convenzione e progetti
- **THEN** la scheda non ha più numero e date della convenzione né progetti formativi
