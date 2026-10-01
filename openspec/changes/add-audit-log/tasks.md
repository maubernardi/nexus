## 1. Backend

- [x] 1.1 `V10__create_audit_event.sql` (vincoli con nome, indici, trigger di immutabilità)
- [x] 1.2 `AuditEvent`, `AuditEntityType`, `AuditChanges`, `AuditEventRepository`, `AuditService` (+impl, `MANDATORY`)
- [x] 1.3 Collegamenti: `TicketStateMachine` (create/apply), registrazione del beneficiario
- [x] 1.4 Test: vincoli e immutabilità per nome, evento all'invio della segnalazione, nessun evento se l'operazione fallisce, beneficiario senza valori personali, `MANDATORY` senza transazione

## 2. Consegna

- [ ] 2.1 Backlog e sprint aggiornati (dopo il merge della PR #20); PR con CI verde
