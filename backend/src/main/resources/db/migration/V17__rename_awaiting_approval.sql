-- US-302: la nuova tipologia proposta dal Tutor la approva un operatore del Call Center (o l'ADMIN), non solo l'ADMIN.
-- Lo stato IN_ATTESA_APPROVAZIONE_ADMIN diventa IN_ATTESA_APPROVAZIONE, nei dati e nei vincoli.
-- In più il segno "tipologia cambiata dal Call Center, non ancora vista dal Tutor" (notifica provvisoria, review §8.2).

ALTER TABLE ticket DROP CONSTRAINT ck_ticket_status;
ALTER TABLE ticket_status_history DROP CONSTRAINT ck_ticket_status_history_from;
ALTER TABLE ticket_status_history DROP CONSTRAINT ck_ticket_status_history_to;

UPDATE ticket SET status = 'IN_ATTESA_APPROVAZIONE' WHERE status = 'IN_ATTESA_APPROVAZIONE_ADMIN';
UPDATE ticket_status_history SET from_status = 'IN_ATTESA_APPROVAZIONE' WHERE from_status = 'IN_ATTESA_APPROVAZIONE_ADMIN';
UPDATE ticket_status_history SET to_status = 'IN_ATTESA_APPROVAZIONE' WHERE to_status = 'IN_ATTESA_APPROVAZIONE_ADMIN';

ALTER TABLE ticket ADD CONSTRAINT ck_ticket_status CHECK (status IN (
    'NUOVA', 'IN_ATTESA_APPROVAZIONE', 'IN_LAVORAZIONE', 'PROPOSTA_AZIENDA', 'PROPOSTA_ACCOLTA',
    'APPUNTAMENTO', 'IN_TIROCINIO', 'FORM_RESTITUZIONE', 'RIAPERTO'));
ALTER TABLE ticket_status_history ADD CONSTRAINT ck_ticket_status_history_from CHECK (from_status IN (
    'NUOVA', 'IN_ATTESA_APPROVAZIONE', 'IN_LAVORAZIONE', 'PROPOSTA_AZIENDA', 'PROPOSTA_ACCOLTA',
    'APPUNTAMENTO', 'IN_TIROCINIO', 'FORM_RESTITUZIONE', 'RIAPERTO'));
ALTER TABLE ticket_status_history ADD CONSTRAINT ck_ticket_status_history_to CHECK (to_status IN (
    'NUOVA', 'IN_ATTESA_APPROVAZIONE', 'IN_LAVORAZIONE', 'PROPOSTA_AZIENDA', 'PROPOSTA_ACCOLTA',
    'APPUNTAMENTO', 'IN_TIROCINIO', 'FORM_RESTITUZIONE', 'RIAPERTO'));

ALTER TABLE ticket ADD COLUMN category_change_unseen boolean NOT NULL DEFAULT false;
