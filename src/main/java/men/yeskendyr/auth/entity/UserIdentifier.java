package men.yeskendyr.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "user_identifiers")
public class UserIdentifier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdentifierType type;

    @Column(nullable = false)
    private String value;

    @Column(nullable = false)
    private boolean verified = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected UserIdentifier() {
    }

    public UserIdentifier(User user, IdentifierType type, String value, boolean verified, Instant createdAt) {
        this.user = user;
        this.type = type;
        this.value = value;
        this.verified = verified;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public IdentifierType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public boolean isVerified() {
        return verified;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
