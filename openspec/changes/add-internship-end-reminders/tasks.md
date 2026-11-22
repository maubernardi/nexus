## 1. Backend

- [x] 1.1 V50: `end_reminder_for`, `end_reminder_stage` con i vincoli; test per nome in `DomainConstraintsIT`
- [x] 1.2 `InternshipEndReminderService`, `InternshipEndReminderJob` alle 8; tipi di notifica e testi delle email
- [x] 1.3 Blocco in `submit` e `submitSpecial`; `GET /api/v1/tickets/internships-to-close`
- [x] 1.4 `InternshipEndRemindersIT`: avvisi giorno per giorno, una volta sola, giorni saltati, proroga, chiuso,
  versione della scheda invariata; blocco via API, fine oggi, proroga, bacheca, solo Tutor

## 2. Frontend

- [x] 2.1 Testi delle notifiche e della cronologia
- [x] 2.2 `InternshipsToCloseNotice` in «Nuova segnalazione» e nella candidatura; test con axe

## 3. Documentazione e verifica

- [x] 3.1 Backlog (US-1203), Sprint 16, collaudo
- [x] 3.2 `./mvnw verify` e controlli del frontend verdi; `openspec validate add-internship-end-reminders --strict`;
  CI verde
