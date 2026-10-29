## Decisions

### D1 — Tabelle a parte, non colonne dell'azienda
Il legale rappresentante ha la sua tabella, con la sua versione. Così non entra in conflitto con il modulo dei dati
dell'azienda (che ha la propria versione) e i dati personali stanno separati: si leggono solo dove servono. Lo
stesso vale per l'anonimizzazione (US-1303). I codici ATECO sono righe che si aggiungono e si tolgono, mai modificate.

### D2 — Obbligatori solo nome e cognome
Nome e cognome identificano il legale rappresentante. Gli altri dati si inseriscono quando si conoscono: saranno i
documenti (US-1004) a segnalare ciò che manca. Un modulo tutto obbligatorio impedirebbe di salvare quel poco che si sa.

### D3 — Codice fiscale controllato nell'applicazione
Il database controlla il formato (16 maiuscole e cifre); l'applicazione controlla struttura, omocodia e carattere di
controllo, con un messaggio sul campo. Il controllo vive in `FiscalCodes`, riusabile per il codice fiscale del
beneficiario nella scheda del tirocinio (US-1001).

### D4 — Il Tutor arriva con US-1001
Gli endpoint di questa storia sono del Call Center e dell'ADMIN. Il Tutor userà gli stessi dati dalla scheda del
tirocinio, solo per i propri tirocini. Lì potrà aggiungere un codice ATECO e inserire il legale rappresentante se
manca, ma non correggerlo (decisione del PO del 29/10).
