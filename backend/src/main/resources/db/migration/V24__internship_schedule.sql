-- Griglia oraria e monte ore del tirocinio (US-1002, PO 29/10). Due fasce per tirocinio, precompilate e modificabili;
-- ogni giorno della settimana usa una o entrambe, con le ore del giorno se diverse dalle fasce intere. Il monte ore si
-- calcola nell'applicazione da griglia, date e sospensioni, saltando le festività nazionali se la scheda lo chiede.
-- Le ore settimanali scritte a mano (V23) diventano il totale della griglia: la colonna si toglie (schede solo in
-- locale, la demo non ha ancora V23).
-- Orari delle fasce in minuti dalla mezzanotte (9:00 = 540): una colonna time subirebbe la conversione di fuso che
-- l'applicazione applica agli istanti (hibernate.jdbc.time_zone), con l'ora legale in mezzo.
ALTER TABLE internship
    DROP COLUMN weekly_hours,
    ADD COLUMN first_slot_start  smallint NOT NULL DEFAULT 540,
    ADD COLUMN first_slot_end    smallint NOT NULL DEFAULT 780,
    ADD COLUMN second_slot_start smallint NOT NULL DEFAULT 840,
    ADD COLUMN second_slot_end   smallint NOT NULL DEFAULT 1080,
    ADD COLUMN exclude_holidays  boolean  NOT NULL DEFAULT true,
    ADD CONSTRAINT ck_internship_slots CHECK (first_slot_start >= 0 AND first_slot_start < first_slot_end
        AND first_slot_end <= second_slot_start AND second_slot_start < second_slot_end AND second_slot_end <= 1440);

-- Un giorno della settimana (1 = lunedì … 7 = domenica, ISO) con le fasce scelte; minuti nulli = fasce intere. Righe
-- sostituite in blocco a ogni salvataggio della scheda.
CREATE TABLE internship_schedule_day (
    id             bigint       NOT NULL,
    internship_id  bigint       NOT NULL,
    day_of_week    smallint     NOT NULL,
    first_slot     boolean      NOT NULL,
    second_slot    boolean      NOT NULL,
    minutes        smallint,
    created_at     timestamptz  NOT NULL,
    created_by     varchar(100) NOT NULL,
    CONSTRAINT pk_internship_schedule_day PRIMARY KEY (id),
    CONSTRAINT fk_internship_schedule_day_internship FOREIGN KEY (internship_id) REFERENCES internship (id),
    CONSTRAINT uq_internship_schedule_day UNIQUE (internship_id, day_of_week),
    CONSTRAINT ck_internship_schedule_day_dow CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT ck_internship_schedule_day_slot CHECK (first_slot OR second_slot),
    CONSTRAINT ck_internship_schedule_day_minutes CHECK (minutes IS NULL OR minutes > 0)
);
