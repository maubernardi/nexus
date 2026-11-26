# legacy-import Specification

## Purpose
Porta in NEXUS le aziende e le chiamate del database Access attuale con un'importazione una tantum: ripetibile senza
duplicati, con un resoconto delle righe da rivedere e senza dati personali nei log.
## Requirements
### Requirement: Importazione delle aziende dal database attuale
Con il profilo `legacy-import` e il percorso del file Access, il backend SHALL importare tutte le aziende del database
attuale e poi chiudersi. Le aziende entrano:
- con il nome, oppure «Azienda senza nome n. ‹numero di Access›»;
- con la partita IVA «00000000000»;
- con l'indirizzo e il comune della sede legale e della sede operativa;
- con telefono, cellulare, fax, email e seconda email;
- con i referenti e il loro ruolo, la categoria merceologica e l'attività.

Il comune SHALL essere abbinato all'elenco ufficiale dei comuni vigenti, anche quando il testo ha la sigla della
provincia in coda, un quartiere dopo il nome, è tagliato a 20 caratteri o è un alias noto (abbreviazione, frazione,
comune fuso). Le email SHALL essere ripulite dal formato dei collegamenti di Access. I recapiti che non rispettano il
formato dei campi MUST NOT andare persi: finiscono in una nota del diario. La nota dell'azienda SHALL entrare nel
diario con la data originale. L'audit di ogni azienda SHALL registrare l'importazione con il numero di Access. Senza
l'elenco dei comuni l'importazione MUST NOT partire.

#### Scenario: Azienda completa
- **WHEN** si importa un'azienda con nome, «SCANDICCI FI» come comune, due referenti con il ruolo, la categoria e una
  nota
- **THEN** l'azienda ha la partita IVA da completare, il comune Scandicci, i referenti con il ruolo, la categoria e la
  nota nel diario con la data di Access

#### Scenario: Azienda senza nome
- **WHEN** si importa l'azienda n. 1234 senza nome
- **THEN** si chiama «Azienda senza nome n. 1234» ed è tra le aziende da completare

#### Scenario: Email nel formato dei collegamenti
- **WHEN** l'email in Access è `info@ditta.it#mailto:info@ditta.it#`
- **THEN** l'email dell'azienda è `info@ditta.it`

#### Scenario: Recapito fuori formato
- **WHEN** il cellulare in Access è «333 1234567 (sera)»
- **THEN** il cellulare dell'azienda è «333 1234567» e il testo originale è in una nota del diario

#### Scenario: Elenco dei comuni vuoto
- **WHEN** si avvia l'importazione senza l'elenco dei comuni
- **THEN** l'importazione si ferma con un messaggio e non scrive nulla

### Requirement: Archivio delle chiamate
L'importazione SHALL portare tutte le chiamate del database attuale in una tabella di archivio, con tutti i loro dati:
- data e ora;
- oggetto, esito, data dell'esito e note;
- se è stata evasa e le disponibilità dell'azienda;
- progetto e operatore.

Ogni chiamata SHALL essere collegata all'azienda importata quando esiste. Le chiamate MUST NOT comparire
nell'interfaccia.

#### Scenario: Chiamata di un'azienda importata
- **WHEN** si importa una chiamata dell'azienda n. 12 con l'esito 2 e il progetto 26
- **THEN** l'archivio ha la chiamata collegata all'azienda, con l'esito «Richiamare» e il progetto «GOL»

#### Scenario: Chiamata senza azienda
- **WHEN** si importa una chiamata di un'azienda che non esiste più
- **THEN** la chiamata è nell'archivio, senza azienda, con il numero dell'azienda di Access

### Requirement: Importazione ripetibile e resoconto
Ogni riga importata SHALL avere un id ricavato dal numero di Access. Una seconda importazione MUST NOT duplicare righe
né cambiare quelle già presenti. Alla fine l'importazione SHALL scrivere un resoconto accanto al file. Il resoconto
contiene:
- le righe lette e quelle entrate;
- quelle da rivedere: duplicati per nome, comuni non trovati, aziende senza nome, chiamate con possibili nomi di
  beneficiari;
- per le aziende il numero di Access e il collegamento alla scheda.

Il resoconto e il log MUST NOT contenere dati personali: niente nomi di aziende o di persone, recapiti o note.

#### Scenario: Seconda esecuzione
- **WHEN** si ripete l'importazione dopo aver corretto a mano un'azienda importata
- **THEN** non entra nessuna riga nuova e l'azienda corretta resta com'è

#### Scenario: Resoconto
- **WHEN** l'importazione finisce
- **THEN** il resoconto elenca i duplicati e i comuni non trovati con i numeri di Access, senza nomi, recapiti o note

### Requirement: Simboli del database attuale nel nome
L'importazione SHALL applicare al nome di ogni azienda la stessa regola della conversione delle aziende già in NEXUS:
- ogni `$` all'inizio del nome vale una stella di affidabilità, al massimo 4;
- ogni `£` all'inizio del nome vale un pollice di interesse al progetto, al massimo 2;
- `&` e `^` all'inizio del nome si tolgono;
- i simboli si contano in qualunque ordine, anche separati da spazi.

La `&` dentro il nome MUST restare. Un nome fatto solo di simboli SHALL diventare «Azienda senza nome n. ‹numero di
Access›». Il resoconto SHALL dire quante aziende hanno ricevuto stelle, pollici o un nome ripulito, senza nomi.

#### Scenario: Nome con i simboli
- **WHEN** si importa l'azienda «$$£ Officina Rossi»
- **THEN** l'azienda si chiama «Officina Rossi», con 2 stelle e un pollice

#### Scenario: Nome fatto solo di simboli
- **WHEN** si importa l'azienda n. 1234 chiamata «$$»
- **THEN** si chiama «Azienda senza nome n. 1234», ha 2 stelle ed è tra le aziende da completare

#### Scenario: Conteggi nel resoconto
- **WHEN** l'importazione finisce
- **THEN** il resoconto dice quante aziende hanno ricevuto stelle e pollici e quante avevano un refuso all'inizio del
  nome, senza i nomi

