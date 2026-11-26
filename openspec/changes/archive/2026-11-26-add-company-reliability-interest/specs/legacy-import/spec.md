## ADDED Requirements

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
