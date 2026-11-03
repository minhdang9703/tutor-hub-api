package com.tutorhub.auth.entity;

import com.tutorhub.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "verification_tokens")
public class VerificationToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private TokenPurpose purpose;
    @Column(name = "token_hash", nullable = false, unique = true) private String tokenHash;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "used_at") private Instant usedAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @PrePersist void onCreate() { createdAt = Instant.now(); }
    public User getUser() { return user; } public void setUser(User value) { user = value; } public TokenPurpose getPurpose() { return purpose; } public void setPurpose(TokenPurpose value) { purpose = value; }
    public String getTokenHash() { return tokenHash; } public void setTokenHash(String value) { tokenHash = value; } public Instant getExpiresAt() { return expiresAt; } public void setExpiresAt(Instant value) { expiresAt = value; } public Instant getUsedAt() { return usedAt; } public void use() { usedAt = Instant.now(); }
}
