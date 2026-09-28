package it.nexus.domain;

import java.io.Serial;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Metadati di un file (CV, documenti generati); il contenuto è nello storage indicato da {@code storageKey}. */
@Getter
@Setter
@Entity
@Table(name = "stored_file")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoredFile extends AbstractTsidEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "content_type", nullable = false, length = 150)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "checksum_sha256", nullable = false, length = 64)
    private String checksumSha256;

    @Column(name = "storage_key", nullable = false, unique = true, length = 500)
    private String storageKey;
}
