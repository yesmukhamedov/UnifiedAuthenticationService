package men.yeskendyr.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "otp_challenges")
public class OtpChallenge {
    @Id
    @Column(nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OtpPurpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdentifierType type;

    @Column(nullable = false)
    private String value;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "code_hash", nullable = false)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected OtpChallenge() {
    }

    public OtpChallenge(UUID id, OtpPurpose purpose, IdentifierType type, String value, User user,
                        String codeHash, Instant expiresAt, Instant consumedAt, Instant createdAt) {
        this.id = id;
        this.purpose = purpose;
        this.type = type;
        this.value = value;
        this.user = user;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
        this.consumedAt = consumedAt;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public OtpPurpose getPurpose() {
        return purpose;
    }

    public IdentifierType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public User getUser() {
        return user;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getConsumedAt() {
        return consumedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setConsumedAt(Instant consumedAt) {
        this.consumedAt = consumedAt;
    }
}
