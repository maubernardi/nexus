-- US-1009: data di fine effettiva del tirocinio nel report di restituzione e funzione o ruolo dei tutor aziendali.
-- Solo additiva (A16): la release precedente ignora le colonne nuove e continua a inserire report e tutor senza.

-- Fine effettiva: obbligatoria nel servizio per i report nuovi; vuota per quelli inviati prima, che non si modificano
-- (il trigger di V26 rifiuta gli UPDATE). Il controllo «non prima dell'inizio» confronta la scheda del tirocinio e sta
-- nel servizio.
ALTER TABLE closure_report ADD COLUMN actual_end_date date;

-- Funzione o ruolo del tutor aziendale in azienda, testo libero facoltativo.
ALTER TABLE internship_company_tutor ADD COLUMN role varchar(200);
ALTER TABLE internship_company_tutor
    ADD CONSTRAINT ck_internship_company_tutor_role CHECK (role IS NULL OR btrim(role) <> '');
