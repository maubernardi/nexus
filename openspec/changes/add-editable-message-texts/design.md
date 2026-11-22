## Context

Vedi proposal.md, «Why». I testi stavano in tre posti:
- `NotificationEmail.java`, per l'email di ogni tipo di notifica;
- `it.ts` nel frontend, per la notifica nell'app, che aveva lo stesso testo;
- `EmailChangeMailer.java`, per il codice e l'avviso del cambio di indirizzo.

L'oggetto delle notifiche era «PORTALE NEXUS - Segnalazione n. N» per tutte.

## Goals / Non-Goals

**Goals:** l'ADMIN cambia i testi senza deploy; i testi predefiniti restano quelli di oggi, quindi dopo il deploy non
cambia nulla, tranne la parte distintiva dell'oggetto che è nuova. Le parti fisse e le regole del PO non si toccano.

**Non-Goals:** email di Keycloak, altre lingue, documenti (vedi proposal.md).

## Decisions

### D1. Predefiniti nel codice, nel database solo i testi cambiati

- `MessageDefaults` ha i testi di oggi, copiati da `it.ts` (app) e da `NotificationEmail` (email), con `{numero}` al
  posto dei vecchi segnaposto, più le parti distintive dell'oggetto.
- `message_text` (V51) ha una riga per ogni parte cambiata, con `UNIQUE (message, part)`. Il ripristino cancella la
  riga.
- **Perché**: i predefiniti seguono il codice, per esempio quando arriva un tipo di notifica nuovo. Una riga esiste
  solo per una scelta dell'ADMIN. Salvare un testo identico al predefinito equivale a ripristinarlo.

### D2. Messaggi e parti

- `MessageKey`: una voce per ogni `NotificationType`, con lo stesso nome (`MessageKey.of(type)`), più
  `EMAIL_CHANGE_CODE` ed `EMAIL_CHANGE_NOTICE`.
- `MessagePart`:
  - `APP`, `EMAIL_SUBJECT` ed `EMAIL_BODY` per le notifiche;
  - `EMAIL_SUBJECT` ed `EMAIL_BODY` per il cambio di indirizzo.
- **Segnaposto**:
  - `{numero}` per le notifiche;
  - `{codice}` e `{ora}` per il codice del cambio di indirizzo;
  - nessuno per l'avviso.
- `MessageDefaultsTest` controlla che ogni tipo di notifica abbia il suo messaggio, ogni parte il suo predefinito, e
  che i predefiniti usino solo segnaposto ammessi.

### D3. Composizione

- `MessageTexts`: i testi in un momento, letti una volta (`findAll`, poche righe), con `render(key, part, valori)`.
  Un segnaposto senza valore resta com'è.
- **Email della notifica**:
  - oggetto = `PORTALE NEXUS - Segnalazione n. N - ` + parte distintiva;
  - testo = testo dell'email + collegamento, avvertenza e firma fissi.
- **Email del cambio di indirizzo**:
  - oggetto = `PORTALE NEXUS - ` + testo;
  - testo = testo + avvertenza e firma fisse.
- **Notifiche nell'app**: `NotificationDTO.text` è il testo `APP` già composto; il frontend lo mostra, con il testo
  generico solo se manca.

### D4. Validazione e audit

- **Spazi**: tolti ai bordi; nell'oggetto e nel testo dell'app anche gli a capo diventano spazi.
- **Lunghezze**:
  - testo dell'app 500 caratteri;
  - oggetto 100;
  - testo dell'email 4000.
- **Segnaposto**: un `{nome}` non ammesso è rifiutato con l'elenco di quelli ammessi; una graffa fuori da un
  segnaposto è rifiutata.
- **Audit** `MESSAGE_TEXT`, sull'id della riga: `TEXT_CHANGED` e `TEXT_RESET`, con messaggio, parte e testo prima e
  dopo. I testi non hanno dati personali.

### D5. Interfaccia

- **Pagina `/testi`**, ADMIN, voce «Testi dei messaggi» nel menu di amministrazione, accanto a «Modelli dei
  documenti». I Cataloghi sono già lunghi.
- **Due gruppi** (`h2`): «Notifiche» ed «Email del cambio di indirizzo».
- **Ogni messaggio** è un `<details>`:
  - nel sommario titolo, «Testi cambiati» se ce ne sono, e a chi va e quando;
  - dentro, un `<fieldset>` con il titolo come `legend`, perché le etichette dei campi si ripetono da un messaggio
    all'altro.
- **Ogni parte** ha un suo modulo:
  - campo (una riga per l'oggetto);
  - suggerimento con i segnaposto;
  - anteprima con valori d'esempio e parti fisse;
  - «Salva» e, se cambiata, «Ripristina il testo predefinito»: i nomi accessibili contengono parte e messaggio;
  - esito in `role="status"`, e l'errore del server sul campo.

## Risks / Trade-offs

- **[Testo cambiato che perde il numero]**: l'ADMIN può togliere `{numero}`. → È una sua scelta; l'anteprima lo
  mostra, e nell'email il collegamento alla segnalazione resta fisso.
- **[Tipo di notifica nuovo]**: serve il suo predefinito. → `MessageDefaultsTest` fallisce finché non c'è.
- **[Email di Keycloak]**: restano nel tema. → Il README dice dove si cambiano.

## Migration Plan

- **V51** solo additiva; tabella vuota: dopo il deploy valgono i predefiniti.
- **Ritorno indietro**: la release precedente ignora la tabella e usa i suoi testi.

## Open Questions

Nessuna: testi distinti, parte distintiva dell'oggetto e sprint decisi dal PO il 22/11.
