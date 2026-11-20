-- US-605: una mansione per più segnalazioni, una per tipo di tirocinio (PO, 15/11). Il tipo di tirocinio passa sulla
-- segnalazione (lo indica il Tutor); la segnalazione è "assegnata" alla sua mansione (job_slot_held) e il database
-- ammette al più un'assegnazione per mansione e tipo. job_slot.status e blocked_by_ticket_id restano come riassunto
-- (BLOCCATA = almeno un'assegnazione; la segnalazione con il numero più basso): la release precedente li legge come
-- oggi (A16). Solo additiva.
ALTER TABLE ticket ADD COLUMN internship_type_id bigint;
ALTER TABLE ticket ADD CONSTRAINT fk_ticket_internship_type FOREIGN KEY (internship_type_id)
    REFERENCES internship_type (id);

ALTER TABLE ticket ADD COLUMN job_slot_held boolean NOT NULL DEFAULT false;
ALTER TABLE ticket ADD CONSTRAINT ck_ticket_job_slot_held CHECK (NOT job_slot_held OR job_slot_id IS NOT NULL);

-- le segnalazioni senza tipo contano come un tipo a sé: due sulla stessa mansione non possono stare insieme
CREATE UNIQUE INDEX uq_ticket_job_slot_internship_type ON ticket (job_slot_id, coalesce(internship_type_id, 0))
    WHERE job_slot_held;

-- il tipo dalla scheda del tirocinio, dove c'è
UPDATE ticket t
SET internship_type_id = i.internship_type_id
FROM internship i
WHERE i.ticket_id = t.id
  AND i.internship_type_id IS NOT NULL;

-- le assegnazioni di oggi: le segnalazioni che bloccano una mansione
UPDATE ticket t
SET job_slot_held = true
FROM job_slot j
WHERE j.blocked_by_ticket_id = t.id
  AND t.job_slot_id = j.id;
