## MODIFIED Requirements

### Requirement: Dati personali nell'audit
Il sistema SHALL NOT registrare nell'audit i valori dei dati personali: per i beneficiari registra solo l'azione e,
nelle modifiche, i nomi dei campi interessati; per le aziende registra referente, telefono ed email solo come campi
impostati o modificati, senza valori, mentre gli altri dati dell'azienda restano con i valori prima e dopo.

#### Scenario: Registrazione di un beneficiario
- **WHEN** il Tutor registra un beneficiario
- **THEN** l'audit contiene un evento `CREATE` sul beneficiario senza nome, cognome né altri valori personali

#### Scenario: Cambio del referente aziendale
- **WHEN** un operatore cambia il referente di un'azienda
- **THEN** l'audit registra un `UPDATE` con il solo campo `contactPerson` segnato come modificato, senza il vecchio né il
  nuovo nome
