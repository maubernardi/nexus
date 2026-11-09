-- Contenuto dei file caricati (US-205, curriculum del beneficiario), nel database insieme ai metadati di stored_file:
-- il backup del database copre anche i file e cancellare il file (es. con l'anonimizzazione, US-1303) cancella il
-- contenuto. Al massimo 5 MB per file.
CREATE TABLE stored_file_content (
    file_id bigint NOT NULL,
    content bytea  NOT NULL,
    CONSTRAINT pk_stored_file_content PRIMARY KEY (file_id),
    CONSTRAINT fk_stored_file_content_file FOREIGN KEY (file_id) REFERENCES stored_file (id) ON DELETE CASCADE,
    CONSTRAINT ck_stored_file_content_size CHECK (octet_length(content) BETWEEN 1 AND 5242880)
);
