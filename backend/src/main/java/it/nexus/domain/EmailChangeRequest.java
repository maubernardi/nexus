package it.nexus.domain;

import java.io.Serial;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Richiesta di cambio del proprio indirizzo email (US-110), in attesa della conferma con il codice inviato al nuovo
 * indirizzo. Una per utente: {@link #renew} la sostituisce con un nuovo codice. Il codice c'è solo come hash.
 */
@Getter
@Entity
@Table(name = "email_change_request")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailChangeRequest extends AbstractTsidEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Tentativi sbagliati ammessi per codice. */
    public static final int MAX_ATTEMPTS = 5;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    @Column(name = "new_email", nullable = false, length = 254)
    private String newEmail;

    @Column(name = "code_hash", nullable = false, length = 100)
    private String codeHash;

    @Column(name = "attempts", nullable = false)
    private short attempts;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    public EmailChangeRequest(AppUser user, String newEmail, String codeHash, Instant sentAt, Instant expiresAt) {
        this.user = user;
        renew(newEmail, codeHash, sentAt, expiresAt);
    }

    /** Nuovo indirizzo e nuovo codice: il codice precedente non vale più e i tentativi ripartono da zero. */
    public final void renew(String newEmail, String codeHash, Instant sentAt, Instant expiresAt) {
        this.newEmail = newEmail;
        this.codeHash = codeHash;
        this.sentAt = sentAt;
        this.expiresAt = expiresAt;
        this.attempts = 0;
    }

    public void recordFailedAttempt() {
        attempts++;
    }

    public int remainingAttempts() {
        return MAX_ATTEMPTS - attempts;
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }
}
