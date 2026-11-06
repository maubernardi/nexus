-- Coordinatore di progetto (US-112): un Tutor assegnato a un progetto può coordinarlo, cioè spostarne le segnalazioni
-- tra i Tutor dello stesso progetto. Il flag sta sull'assegnazione: lo stesso Tutor coordina un progetto e non un altro.
ALTER TABLE user_project ADD COLUMN coordinator boolean NOT NULL DEFAULT false;
