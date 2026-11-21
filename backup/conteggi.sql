-- NEXUS — righe di ogni tabella dello schema nexus, una riga «tabella|righe» (US-1302). Solo numeri: nessun dato.
-- Lo usano il ripristino di prova e la prova della CI, sul database di origine e su quello ripristinato.
SELECT format('SELECT %L, count(*) FROM %I.%I', c.relname, n.nspname, c.relname)
FROM pg_class c
JOIN pg_namespace n ON n.oid = c.relnamespace
WHERE n.nspname = 'nexus' AND c.relkind IN ('r', 'p')
ORDER BY c.relname
\gexec
