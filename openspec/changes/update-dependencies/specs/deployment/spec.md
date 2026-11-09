## ADDED Requirements

### Requirement: Dipendenze aggiornate
Le dipendenze del frontend, del backend, delle immagini e della CI SHALL essere all'ultima versione stabile; Java e Node
MUST essere all'ultima LTS. Gli aggiornamenti SHALL arrivare con le PR di Dependabot, una a settimana per ecosistema per
le versioni minori e una per ogni versione principale, e passare dalla CI prima del merge del PO. Le vulnerabilità
segnalate SHALL essere chiuse aggiornando; un avviso senza correzione esistente MAY essere archiviato con il motivo e
l'ok del PO. Le immagini usate dai test MUST essere le stesse della produzione.

#### Scenario: Aggiornamento settimanale
- **WHEN** esce una versione minore di una dipendenza del frontend
- **THEN** il lunedì successivo Dependabot apre una PR con tutte le minori del frontend, che il PO unisce dopo la CI verde

#### Scenario: Versione principale
- **WHEN** esce una nuova versione principale di una dipendenza
- **THEN** Dependabot apre una PR solo per quella; per Java e per i tipi di Node non la apre, perché seguono la LTS

#### Scenario: Immagini dei test diverse dalla produzione
- **WHEN** l'immagine di PostgreSQL o di Keycloak della produzione cambia e quella dei test no
- **THEN** la CI fallisce indicando il file da aggiornare
