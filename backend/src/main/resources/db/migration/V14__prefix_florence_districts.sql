-- Decisione del PO (04/10): i quartieri di Firenze si distinguono dai comuni dell'hinterland con il prefisso "Firenze "
-- (es. "Firenze Novoli"). I comuni restano invariati. Idempotente: un nome già con il prefisso non viene toccato.
UPDATE zone
   SET name = 'Firenze ' || name, updated_at = now(), updated_by = 'system'
 WHERE code IN (
    'NOVOLI', 'ISOLOTTO', 'LEGNAIA', 'RIFREDI', 'CAMPO_DI_MARTE', 'SANTA_CROCE', 'OLTRARNO', 'GALLUZZO',
    'LE_PIAGGE', 'ROVEZZANO', 'LE_CURE', 'TRESPIANO', 'SORGANE', 'CASELLINA', 'SOFFIANO', 'SERPIOLLE',
    'CASTELLO', 'SAN_NICCOLO', 'VARLUNGO', 'SANTO_SPIRITO', 'DUE_STRADE', 'PORTA_AL_PRATO', 'SAN_GIOVANNI',
    'STATUTO', 'EUROPA', 'GAVINANA', 'SAN_LORENZO', 'SAN_FREDIANO', 'SAN_MINIATO', 'SAN_MARCO'
 )
   AND name NOT LIKE 'Firenze %';
