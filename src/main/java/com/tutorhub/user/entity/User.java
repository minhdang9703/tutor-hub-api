package com.tutorhub.user.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, columnDefinition = "citext") private String email;
    @Column(unique = true) private String phone;
    @Column(name = "password_hash", nullable = false) private String passwordHash;
    @Column(name = "full_name", nullable = false) private String fullName;
    @Column(name = "avatar_key") private String avatarKey;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Role role;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private UserStatus status;
    @Column(name = "email_verified_at") private Instant emailVerifiedAt;
    @Column(name = "phone_verified_at") private Instant phoneVerifiedAt;
    @Column(name = "last_login_at") private Instant lastLoginAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @PrePersist void onCreate() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    public Long getId() { return id; } public String getEmail() { return email; } public void setEmail(String value) { email = value; }
    public String getPhone() { return phone; } public void setPhone(String value) { phone = value; } public String getPasswordHash() { return passwordHash; } public void setPasswordHash(String value) { passwordHash = value; }
    public String getFullName() { return fullName; } public void setFullName(String value) { fullName = value; } public String getAvatarKey() { return avatarKey; } public void setAvatarKey(String value) { avatarKey = value; }
    public Role getRole() { return role; } public void setRole(Role value) { role = value; } public UserStatus getStatus() { return status; } public void setStatus(UserStatus value) { status = value; }
    public Instant getEmailVerifiedAt() { return emailVerifiedAt; } public void setEmailVerifiedAt(Instant value) { emailVerifiedAt = value; } public Instant getLastLoginAt() { return lastLoginAt; } public void setLastLoginAt(Instant value) { lastLoginAt = value; }
}
