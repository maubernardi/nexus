-- Azienda «nave scuola» (US-515, PO 25/11): adatta a un primo tirocinio. La segnano operatori e ADMIN nella scheda;
-- il Tutor la può proporre nel report di restituzione, e l'operatore della segnalazione riceve un avviso che porta alla
-- scheda dell'azienda. Il report resta immodificabile (V26): qui si aggiungono solo colonne.

ALTER TABLE company ADD COLUMN training_ship boolean NOT NULL DEFAULT false;

ALTER TABLE closure_report ADD COLUMN training_ship_proposed boolean NOT NULL DEFAULT false;

-- un avviso può portare alla scheda di un'azienda invece che alla segnalazione
ALTER TABLE notification
    ADD COLUMN company_id bigint,
    ADD CONSTRAINT fk_notification_company FOREIGN KEY (company_id) REFERENCES company (id);
