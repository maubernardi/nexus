-- US-509: categorie merceologiche delle aziende, catalogo dell'ADMIN (PO, 19/11), con le 53 categorie del database
-- attuale (le righe 51 e 56 di Access sono vuote e mai usate). Due nomi corretti: «edelizia» e le spaziature di
-- «Telecomunicazioni, informatica, cinema e TV». L'id di ogni voce iniziale è 893700000000000400 + il numero della
-- categoria in Access: l'importazione (US-1309) la ritrova anche dopo una rinomina. Solo additiva (A16): la release
-- precedente ignora tabella e colonne.
CREATE TABLE business_category (
    id          bigint       NOT NULL,
    code        varchar(60)  NOT NULL,
    name        varchar(200) NOT NULL,
    active      boolean      NOT NULL DEFAULT true,
    created_at  timestamptz  NOT NULL,
    created_by  varchar(100) NOT NULL,
    updated_at  timestamptz  NOT NULL,
    updated_by  varchar(100) NOT NULL,
    CONSTRAINT pk_business_category PRIMARY KEY (id),
    CONSTRAINT uq_business_category_code UNIQUE (code),
    CONSTRAINT ck_business_category_code CHECK (code ~ '^[A-Z0-9_]+$'),
    CONSTRAINT ck_business_category_name CHECK (btrim(name) <> '')
);

CREATE UNIQUE INDEX uq_business_category_active_name ON business_category (lower(name)) WHERE active;

INSERT INTO business_category (id, code, name, active, created_at, created_by, updated_at, updated_by) VALUES
    (893700000000000401, 'ALBERGO', 'Albergo', true, now(), 'system', now(), 'system'),
    (893700000000000402, 'HOTEL', 'Hotel', true, now(), 'system', now(), 'system'),
    (893700000000000403, 'AGRITURISMO_CAMPEGGIO_VILL', 'Agriturismo / Campeggio / Villaggio vacanze / Agenzie viaggi', true, now(), 'system', now(), 'system'),
    (893700000000000404, 'BED_AND_BREAKFAST', 'Bed and Breakfast', true, now(), 'system', now(), 'system'),
    (893700000000000405, 'PENSIONE', 'Pensione', true, now(), 'system', now(), 'system'),
    (893700000000000406, 'PANETTERIA_FORNI', 'Panetteria / Forni', true, now(), 'system', now(), 'system'),
    (893700000000000407, 'ALIMENTARI_ENOTECHE_LAVORA', 'Alimentari / Enoteche / Lavorazione Carni / Prodotti ittici', true, now(), 'system', now(), 'system'),
    (893700000000000408, 'SUPERMERCATI_E_GRANDE_DIST', 'Supermercati e grande distribuzione', true, now(), 'system', now(), 'system'),
    (893700000000000409, 'ABBIGLIAMENTO', 'Abbigliamento', true, now(), 'system', now(), 'system'),
    (893700000000000410, 'ANTINFORTUNISTICA', 'Antinfortunistica', true, now(), 'system', now(), 'system'),
    (893700000000000411, 'RISTORANTE_TRATTORIA_BISTR', 'Ristorante / Trattoria / Bistrot', true, now(), 'system', now(), 'system'),
    (893700000000000412, 'PIZZERIA', 'Pizzeria', true, now(), 'system', now(), 'system'),
    (893700000000000413, 'MENSA', 'Mensa', true, now(), 'system', now(), 'system'),
    (893700000000000414, 'BAR_PUB', 'Bar / Pub', true, now(), 'system', now(), 'system'),
    (893700000000000415, 'CATERING_ORGANIZZAZIONE_EV', 'Catering / Organizzazione eventi / Marketing', true, now(), 'system', now(), 'system'),
    (893700000000000416, 'PULIZIE_E_FACCHINAGGIO', 'Pulizie e facchinaggio', true, now(), 'system', now(), 'system'),
    (893700000000000417, 'FIORI_E_VIVAI', 'Fiori e vivai', true, now(), 'system', now(), 'system'),
    (893700000000000418, 'AZIENDA_AGRICOLA_FATTORIA', 'Azienda agricola / Fattoria / Macchine agricole', true, now(), 'system', now(), 'system'),
    (893700000000000419, 'CENTRO_ESTETICA', 'Centro Estetica', true, now(), 'system', now(), 'system'),
    (893700000000000420, 'CASA_DI_RIPOSO', 'Casa di riposo', true, now(), 'system', now(), 'system'),
    (893700000000000421, 'OFFICINE_MECCANICHE_SALDAT', 'Officine meccaniche / Saldature / Lavorazione metalli', true, now(), 'system', now(), 'system'),
    (893700000000000422, 'PELLETTERIA_ACCESSORI', 'Pelletteria / Accessori', true, now(), 'system', now(), 'system'),
    (893700000000000423, 'LAVANDERIA', 'Lavanderia', true, now(), 'system', now(), 'system'),
    (893700000000000424, 'CARROZZERIA_GOMMISTA_ELETT', 'Carrozzeria / Gommista / Elettrauto / Distributore carburanti', true, now(), 'system', now(), 'system'),
    (893700000000000425, 'CIRCOLO_ASSOCIAZIONE_CULTU', 'Circolo / Associazione Culturale', true, now(), 'system', now(), 'system'),
    (893700000000000426, 'PASTICCERIA', 'Pasticceria', true, now(), 'system', now(), 'system'),
    (893700000000000427, 'LAVORI_EDILI_RESTAURI_PROD', 'Lavori edili / Restauri / Prodotti per l''edilizia', true, now(), 'system', now(), 'system'),
    (893700000000000428, 'PALESTRA_ASSOCIAZIONE_SPOR', 'Palestra / Associazione sportiva', true, now(), 'system', now(), 'system'),
    (893700000000000429, 'TRASPORTO_MERCI_TRASLOCHI', 'Trasporto merci / Traslochi / Imballaggi', true, now(), 'system', now(), 'system'),
    (893700000000000430, 'ASSISTENZA_COOPERAZIONE_SO', 'Assistenza / Cooperazione sociale e di servizi', true, now(), 'system', now(), 'system'),
    (893700000000000431, 'ARREDAMENTO_MOBILIFICIO_PR', 'Arredamento / Mobilificio / Prodotti casa / Decorazioni / Grafica', true, now(), 'system', now(), 'system'),
    (893700000000000432, 'BIBLIOTECHE_MUSEI', 'Biblioteche / Musei', true, now(), 'system', now(), 'system'),
    (893700000000000433, 'CINEMA_TEATRI_COMPAGNIE_TE', 'Cinema, Teatri / Compagnie teatrali', true, now(), 'system', now(), 'system'),
    (893700000000000434, 'NEGOZI_PER_ANIMALI', 'Negozi per Animali', true, now(), 'system', now(), 'system'),
    (893700000000000435, 'CALL_CENTER_INTERNET_POINT', 'Call Center / Internet Point', true, now(), 'system', now(), 'system'),
    (893700000000000436, 'CHIMICA_INDUSTRIALE_FARMAC', 'Chimica industriale / Farmaceutica / Laboratori analisi / Vernici', true, now(), 'system', now(), 'system'),
    (893700000000000437, 'TESSILE_E_MANIFATTURIERO', 'Tessile e Manifatturiero', true, now(), 'system', now(), 'system'),
    (893700000000000438, 'AGENZIE_FORMATIVE', 'Agenzie Formative', true, now(), 'system', now(), 'system'),
    (893700000000000439, 'LUDOTECHE', 'Ludoteche', true, now(), 'system', now(), 'system'),
    (893700000000000440, 'IMPIANTISTICA_ELETTRICA_SI', 'Impiantistica elettrica / Sistemi automazione / Vendita prodotti elettrici', true, now(), 'system', now(), 'system'),
    (893700000000000441, 'ODONTOIATRIA_DENTISTI', 'Odontoiatria Dentisti', true, now(), 'system', now(), 'system'),
    (893700000000000442, 'SERRAMENTI_E_INFISSI', 'Serramenti e Infissi', true, now(), 'system', now(), 'system'),
    (893700000000000443, 'GIOIELLERIA_LAVORAZIONE_OG', 'Gioielleria / Lavorazione oggetti metalli preziosi', true, now(), 'system', now(), 'system'),
    (893700000000000444, 'FALEGNAMERIA_CARPENTERIA', 'Falegnameria / Carpenteria', true, now(), 'system', now(), 'system'),
    (893700000000000445, 'SERVIZI_DI_VIGILANZA_E_POR', 'Servizi di vigilanza e portierato', true, now(), 'system', now(), 'system'),
    (893700000000000447, 'CALZATURIFICIO_E_SCARPE', 'Calzaturificio e scarpe', true, now(), 'system', now(), 'system'),
    (893700000000000448, 'NEGOZIO_GENERICO', 'Negozio generico', true, now(), 'system', now(), 'system'),
    (893700000000000449, 'TELECOMUNICAZIONI_INFORMAT', 'Telecomunicazioni, informatica, cinema e TV', true, now(), 'system', now(), 'system'),
    (893700000000000450, 'OFFICINE_AUTO_MOTO', 'Officine Auto-Moto', true, now(), 'system', now(), 'system'),
    (893700000000000452, 'MINUTERIE_METALLICHE_E_GAL', 'Minuterie metalliche e galvanica', true, now(), 'system', now(), 'system'),
    (893700000000000453, 'UFFICI_GENERICI', 'Uffici generici', true, now(), 'system', now(), 'system'),
    (893700000000000454, 'PROGRAMMAZIONE_GRAFICA_E_W', 'Programmazione grafica e web design', true, now(), 'system', now(), 'system'),
    (893700000000000455, 'PRODUZIONE_PLASTICA_CARTA', 'Produzione plastica carta e imballaggi', true, now(), 'system', now(), 'system')
ON CONFLICT (code) DO NOTHING;

-- Categoria e descrizione dell'attività dell'azienda, facoltative (in Access il campo «Paese», di 50 caratteri).
ALTER TABLE company ADD COLUMN business_category_id bigint;
ALTER TABLE company ADD COLUMN activity varchar(200);
ALTER TABLE company
    ADD CONSTRAINT fk_company_business_category FOREIGN KEY (business_category_id) REFERENCES business_category (id);
ALTER TABLE company ADD CONSTRAINT ck_company_activity CHECK (activity IS NULL OR btrim(activity) <> '');
CREATE INDEX ix_company_business_category ON company (business_category_id);
