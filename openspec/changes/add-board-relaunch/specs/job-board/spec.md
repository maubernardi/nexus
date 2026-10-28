## ADDED Requirements

### Requirement: Rilancio in bacheca dopo un appuntamento fallito
Registrando un appuntamento fallito, il Tutor della segnalazione SHALL poter chiedere il rilancio della mansione in
bacheca. Il sistema SHALL creare nella stessa operazione una bozza di annuncio con la mansione appena liberata,
riservata al progetto della segnalazione e collegata a essa, e SHALL notificare l'operatore che ha in carico la
segnalazione. Pubblicarla resta una scelta del Call Center. La bozza MUST NOT essere visibile ai Tutor finché non è
pubblicata.

#### Scenario: Rilancio chiesto
- **WHEN** il Tutor registra l'appuntamento fallito spuntando "Chiedi il rilancio della mansione in bacheca"
- **THEN** nasce la bozza #N riservata al progetto della segnalazione, l'operatore riceve la notifica e la
  cronologia riporta "rilancio in bacheca richiesto"

#### Scenario: Link alla bozza
- **WHEN** l'operatore apre la segnalazione
- **THEN** la sezione "Rilancio in bacheca" porta alla scheda dell'annuncio #N; il Tutor vede solo numero e stato

#### Scenario: Senza rilancio
- **WHEN** il Tutor registra l'appuntamento fallito senza spuntare la casella
- **THEN** non nasce nessun annuncio
