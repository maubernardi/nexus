-- Zone iniziali (EN-3, decisione del PO del 01/10): quartieri di Firenze e comuni dell'hinterland.
-- Dati reali di partenza, presenti in ogni ambiente; l'ADMIN potrà modificarli dalle maschere dedicate (US-106).
-- Inserimento idempotente sul codice: una zona con lo stesso codice già presente non viene toccata.

INSERT INTO zone (id, code, name, active, created_at, created_by, updated_at, updated_by) VALUES
    -- quartieri di Firenze
    (893604051190856109, 'NOVOLI', 'Novoli', true, now(), 'system', now(), 'system'),
    (893604051193473491, 'ISOLOTTO', 'Isolotto', true, now(), 'system', now(), 'system'),
    (893604051190130563, 'LEGNAIA', 'Legnaia', true, now(), 'system', now(), 'system'),
    (893604051191153865, 'RIFREDI', 'Rifredi', true, now(), 'system', now(), 'system'),
    (893604051192228073, 'CAMPO_DI_MARTE', 'Campo di Marte', true, now(), 'system', now(), 'system'),
    (893604051189700383, 'SANTA_CROCE', 'Santa Croce', true, now(), 'system', now(), 'system'),
    (893604051189801675, 'OLTRARNO', 'Oltrarno', true, now(), 'system', now(), 'system'),
    (893604051192942530, 'GALLUZZO', 'Galluzzo', true, now(), 'system', now(), 'system'),
    (893604051191745508, 'LE_PIAGGE', 'Le Piagge', true, now(), 'system', now(), 'system'),
    (893604051189892666, 'ROVEZZANO', 'Rovezzano', true, now(), 'system', now(), 'system'),
    (893604051191031666, 'LE_CURE', 'Le Cure', true, now(), 'system', now(), 'system'),
    (893604051191942246, 'TRESPIANO', 'Trespiano', true, now(), 'system', now(), 'system'),
    (893604051189741121, 'SORGANE', 'Sorgane', true, now(), 'system', now(), 'system'),
    (893604051193313431, 'CASELLINA', 'Casellina', true, now(), 'system', now(), 'system'),
    (893604051191626195, 'SOFFIANO', 'Soffiano', true, now(), 'system', now(), 'system'),
    (893604051190398365, 'SERPIOLLE', 'Serpiolle', true, now(), 'system', now(), 'system'),
    (893604051189655124, 'CASTELLO', 'Castello', true, now(), 'system', now(), 'system'),
    (893604051189858344, 'SAN_NICCOLO', 'San Niccolò', true, now(), 'system', now(), 'system'),
    (893604051191316697, 'VARLUNGO', 'Varlungo', true, now(), 'system', now(), 'system'),
    (893604051191251797, 'SANTO_SPIRITO', 'Santo Spirito', true, now(), 'system', now(), 'system'),
    (893604051189790850, 'DUE_STRADE', 'Due Strade', true, now(), 'system', now(), 'system'),
    (893604051190507269, 'PORTA_AL_PRATO', 'Porta al Prato', true, now(), 'system', now(), 'system'),
    (893604051189878333, 'SAN_GIOVANNI', 'San Giovanni', true, now(), 'system', now(), 'system'),
    (893604051191809115, 'STATUTO', 'Statuto', true, now(), 'system', now(), 'system'),
    (893604051191278418, 'EUROPA', 'Europa', true, now(), 'system', now(), 'system'),
    (893604051189745783, 'GAVINANA', 'Gavinana', true, now(), 'system', now(), 'system'),
    (893604051192965925, 'SAN_LORENZO', 'San Lorenzo', true, now(), 'system', now(), 'system'),
    (893604051191869540, 'SAN_FREDIANO', 'San Frediano', true, now(), 'system', now(), 'system'),
    (893604051190017119, 'SAN_MINIATO', 'San Miniato', true, now(), 'system', now(), 'system'),
    (893604051193471748, 'SAN_MARCO', 'San Marco', true, now(), 'system', now(), 'system'),
    -- comuni dell'hinterland fiorentino
    (893604051190434188, 'SCANDICCI', 'Scandicci', true, now(), 'system', now(), 'system'),
    (893604051192142892, 'SESTO_FIORENTINO', 'Sesto Fiorentino', true, now(), 'system', now(), 'system'),
    (893604051192129501, 'CAMPI_BISENZIO', 'Campi Bisenzio', true, now(), 'system', now(), 'system'),
    (893604051191943122, 'BORGO_SAN_LORENZO', 'Borgo San Lorenzo', true, now(), 'system', now(), 'system'),
    (893604051193472835, 'SCARPERIA_E_SAN_PIERO', 'Scarperia e San Piero', true, now(), 'system', now(), 'system'),
    (893604051189757324, 'VICCHIO', 'Vicchio', true, now(), 'system', now(), 'system'),
    (893604051191918401, 'DICOMANO', 'Dicomano', true, now(), 'system', now(), 'system'),
    (893604051191953794, 'BAGNO_A_RIPOLI', 'Bagno a Ripoli', true, now(), 'system', now(), 'system'),
    (893604051191161654, 'IMPRUNETA', 'Impruneta', true, now(), 'system', now(), 'system'),
    (893604051189705848, 'FIESOLE', 'Fiesole', true, now(), 'system', now(), 'system')
ON CONFLICT (code) DO NOTHING;
