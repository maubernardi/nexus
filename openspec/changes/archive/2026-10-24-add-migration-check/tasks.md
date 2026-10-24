## 1. Controllo

- [x] 1.1 `.github/scripts/check-migrations.sh` (numerazione per nome di file, duplicati, immutabilità)
- [x] 1.2 Passo nel job Backend della CI sulle PR (`fetch-depth: 0`, ref via variabile d'ambiente)
- [x] 1.3 Prove su un repository di prova: nessuna migrazione, nuova corretta, numero superato, branch rimasto indietro, merge commit con duplicato, rinumerazione, modifica di una migrazione esistente
- [x] 1.4 Modello di descrizione delle PR con la sezione "Migrazioni Flyway"

## 2. Consegna

- [x] 2.1 Backlog e sprint aggiornati; PR con CI verde
