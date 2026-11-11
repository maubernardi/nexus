## ADDED Requirements

### Requirement: Comune scelto dall'elenco nei moduli
Dove un modulo chiede il comune di un indirizzo, l'utente SHALL sceglierlo dall'elenco dei comuni validi oggi, con una
casella che propone i comuni dalla seconda lettera con la sigla della provincia (es. «Sesto Fiorentino (FI)»). La
casella MUST potersi usare solo da tastiera e MUST annunciare allo screen reader i suggerimenti, quello attivo e il
numero dei risultati. Il sistema SHALL salvare la versione del comune scelta: un caricamento successivo dell'elenco che
cambia nome o provincia del comune MUST NOT cambiare i dati già salvati. Un comune già salvato SHALL restare valido
anche se la sua versione è stata chiusa; un comune nuovo MUST essere tra quelli validi oggi, altrimenti il salvataggio
è rifiutato con un errore sul campo.

#### Scenario: Scelta da tastiera
- **WHEN** l'utente scrive «sesto f» nella casella del comune, scende con la freccia e preme Invio
- **THEN** il campo mostra «Sesto Fiorentino (FI)» e il comune è quello che verrà salvato

#### Scenario: Testo senza scelta
- **WHEN** l'utente scrive un nome nella casella ma non sceglie un comune dall'elenco e salva
- **THEN** il campo segnala che il comune va scelto dall'elenco e nulla è salvato

#### Scenario: Comune cambiato dopo il salvataggio
- **WHEN** un comune già salvato in un indirizzo cambia provincia con un nuovo caricamento dell'elenco
- **THEN** l'indirizzo salvato mantiene la provincia di prima e, risalvando il modulo senza toccarlo, nulla cambia

#### Scenario: Comune non più valido scelto di nuovo
- **WHEN** un modulo invia, come nuova scelta, una versione di comune già chiusa
- **THEN** il salvataggio è rifiutato con un errore sul campo del comune
