package it.nexus.domain;

import java.io.Serial;

import it.nexus.domain.enumeration.MessageKey;
import it.nexus.domain.enumeration.MessagePart;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Testo di un messaggio cambiato dall'ADMIN (US-1204). Esiste solo se è diverso dal predefinito: «ripristina» lo
 * cancella.
 */
@Entity
@Table(name = "message_text")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageText extends AbstractTsidEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Enumerated(EnumType.STRING)
    @Column(name = "message", nullable = false, updatable = false, length = 40)
    private MessageKey message;

    @Enumerated(EnumType.STRING)
    @Column(name = "part", nullable = false, updatable = false, length = 20)
    private MessagePart part;

    @Setter
    @Column(name = "text", nullable = false, length = 4000)
    private String text;

    public MessageText(MessageKey message, MessagePart part, String text) {
        this.message = message;
        this.part = part;
        this.text = text;
    }
}
