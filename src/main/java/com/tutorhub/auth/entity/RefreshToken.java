package com.tutorhub.auth.entity;

import com.tutorhub.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "refresh_tokens")
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(name = "token_hash", nullable = false, unique = true) private String tokenHash;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "revoked_at") private Instant revokedAt;
    @Column(name = "user_agent") private String userAgent;
    @Column(name = "ip_address") private String ipAddress;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @PrePersist void onCreate() { createdAt = Instant.now(); }
    public Long getId() { return id; } public User getUser() { return user; } public void setUser(User value) { user = value; } public String getTokenHash() { return tokenHash; } public void setTokenHash(String value) { tokenHash = value; }
    public Instant getExpiresAt() { return expiresAt; } public void setExpiresAt(Instant value) { expiresAt = value; } public Instant getRevokedAt() { return revokedAt; } public void revoke() { revokedAt = Instant.now(); }
    public void setUserAgent(String value) { userAgent = value; } public void setIpAddress(String value) { ipAddress = value; }
}
