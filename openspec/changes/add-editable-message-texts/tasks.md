## 1. Backend

- [x] 1.1 V51 `message_text` con i vincoli; test per nome in `DomainConstraintsIT`
- [x] 1.2 `MessageKey`, `MessagePart`, `MessageDefaults` (testi di oggi e parti distintive dell'oggetto),
  `MessageTexts`; `MessageDefaultsTest`
- [x] 1.3 `MessageTextService`, `MessageTextResource` (solo ADMIN), validazione dei segnaposto, ripristino, audit
- [x] 1.4 Email delle notifiche, email del cambio di indirizzo ed elenco delle notifiche con i testi in uso;
  `MessageTextIT`; test delle email aggiornati

## 2. Frontend

- [x] 2.1 `MessageTextsPage`, `MessageTextPartForm` con anteprima, salvataggio e ripristino; rotta `/testi` e voce di
  menu; test con axe
- [x] 2.2 Notifiche con il testo dell'API; via i testi delle notifiche da `it.ts`

## 3. Documentazione e verifica

- [x] 3.1 Backlog (US-1204), Sprint 16, collaudo, README
- [x] 3.2 `./mvnw verify` e controlli del frontend verdi; `openspec validate add-editable-message-texts --strict`;
  CI verde
