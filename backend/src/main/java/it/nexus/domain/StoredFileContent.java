package it.nexus.domain;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Contenuto di un file caricato (US-205), separato dai metadati per non leggerlo quando servono solo nome e dimensione.
 * Si cancella insieme al file ({@code ON DELETE CASCADE}).
 */
@Getter
@Entity
@Table(name = "stored_file_content")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoredFileContent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "file_id")
    private Long fileId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_id")
    private StoredFile file;

    @Column(name = "content", nullable = false)
    private byte[] content;

    public StoredFileContent(StoredFile file, byte[] content) {
        this.file = file;
        this.content = content;
    }

    @Override
    public String toString() {
        return "StoredFileContent[fileId=" + fileId + "]";
    }
}
