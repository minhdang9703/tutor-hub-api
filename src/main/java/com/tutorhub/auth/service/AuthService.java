package com.tutorhub.auth.service;

import com.tutorhub.auth.dto.*;
import com.tutorhub.auth.entity.*;
import com.tutorhub.auth.repository.*;
import com.tutorhub.common.config.AppProperties;
import com.tutorhub.common.exception.*;
import com.tutorhub.common.security.JwtService;
import com.tutorhub.common.security.UserPrincipal;
import com.tutorhub.common.util.TokenGenerator;
import com.tutorhub.parent.entity.ParentProfile;
import com.tutorhub.parent.repository.ParentProfileRepository;
import com.tutorhub.tutor.entity.TutorProfile;
import com.tutorhub.tutor.repository.TutorProfileRepository;
import com.tutorhub.user.entity.*;
import com.tutorhub.user.repository.UserRepository;
import java.time.Instant;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users; private final RefreshTokenRepository refreshTokens; private final VerificationTokenRepository verificationTokens;
    private final TutorProfileRepository tutors; private final ParentProfileRepository parents; private final PasswordEncoder passwordEncoder; private final TokenGenerator tokens; private final JwtService jwt; private final EmailSender email; private final AppProperties properties;
    public AuthService(UserRepository users, RefreshTokenRepository refreshTokens, VerificationTokenRepository verificationTokens, TutorProfileRepository tutors, ParentProfileRepository parents, PasswordEncoder passwordEncoder, TokenGenerator tokens, JwtService jwt, EmailSender email, AppProperties properties) { this.users = users; this.refreshTokens = refreshTokens; this.verificationTokens = verificationTokens; this.tutors = tutors; this.parents = parents; this.passwordEncoder = passwordEncoder; this.tokens = tokens; this.jwt = jwt; this.email = email; this.properties = properties; }
    @Transactional public RegisterResponse register(RegisterRequest request) {
        if (request.role() == Role.ADMIN) throw new BusinessRuleException("INVALID_ROLE", "ADMIN cannot self-register");
        if (users.existsByEmailIgnoreCase(request.email())) throw new ConflictException("EMAIL_EXISTS", "Email is already registered");
        User user = new User(); user.setEmail(request.email()); user.setPhone(request.phone()); user.setFullName(request.fullName()); user.setPasswordHash(passwordEncoder.encode(request.password())); user.setRole(request.role()); user.setStatus(UserStatus.PENDING_VERIFICATION); users.save(user);
        if (user.getRole() == Role.TUTOR) { TutorProfile profile = new TutorProfile(); profile.setUser(user); tutors.save(profile); } else { ParentProfile profile = new ParentProfile(); profile.setUser(user); parents.save(profile); }
        sendToken(user, TokenPurpose.VERIFY_EMAIL, "Verify your TutorHub email");
        return new RegisterResponse(user.getId(), "Verification email sent");
    }
    @Transactional public void verifyEmail(String rawToken) { VerificationToken token = token(rawToken, TokenPurpose.VERIFY_EMAIL); User user = token.getUser(); user.setEmailVerifiedAt(Instant.now()); user.setStatus(UserStatus.ACTIVE); token.use(); }
    @Transactional public void resendVerification(String emailAddress) { User user = users.findByEmailIgnoreCase(emailAddress).orElseThrow(() -> new NotFoundException("User not found")); if (user.getEmailVerifiedAt() == null) sendToken(user, TokenPurpose.VERIFY_EMAIL, "Verify your TutorHub email"); }
    @Transactional public AuthResponse login(LoginRequest request, String userAgent, String ip) {
        User user = users.findByEmailIgnoreCase(request.email()).orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) throw new UnauthorizedException("Invalid email or password");
        if (user.getStatus() == UserStatus.SUSPENDED) throw new ForbiddenException("Account is suspended");
        if (user.getStatus() != UserStatus.ACTIVE) throw new UnauthorizedException("Email is not verified");
        user.setLastLoginAt(Instant.now()); return issueTokens(user, userAgent, ip);
    }
    @Transactional public AuthResponse refresh(String rawToken, String userAgent, String ip) {
        RefreshToken token = refreshTokens.findForUpdate(tokens.sha256(rawToken)).orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        if (token.getRevokedAt() != null || !token.getExpiresAt().isAfter(Instant.now())) throw new UnauthorizedException("Refresh token has expired or was revoked");
        if (token.getUser().getStatus() != UserStatus.ACTIVE) throw new UnauthorizedException("Account is not active");
        token.revoke(); return issueTokens(token.getUser(), userAgent, ip);
    }
    @Transactional public void logout(String rawToken) { refreshTokens.findForUpdate(tokens.sha256(rawToken)).ifPresent(RefreshToken::revoke); }
    @Transactional public void forgotPassword(String emailAddress) { users.findByEmailIgnoreCase(emailAddress).ifPresent(user -> sendToken(user, TokenPurpose.RESET_PASSWORD, "Reset your TutorHub password")); }
    @Transactional public void resetPassword(ResetPasswordRequest request) { VerificationToken token = token(request.token(), TokenPurpose.RESET_PASSWORD); token.getUser().setPasswordHash(passwordEncoder.encode(request.newPassword())); token.use(); refreshTokens.revokeAllByUserId(token.getUser().getId()); }
    @Transactional public void changePassword(UserPrincipal principal, ChangePasswordRequest request) { User user = user(principal.id()); if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) throw new UnauthorizedException("Current password is incorrect"); user.setPasswordHash(passwordEncoder.encode(request.newPassword())); refreshTokens.revokeAllByUserId(user.getId()); }
    private AuthResponse issueTokens(User user, String userAgent, String ip) { String raw = tokens.generate(); RefreshToken refresh = new RefreshToken(); refresh.setUser(user); refresh.setTokenHash(tokens.sha256(raw)); refresh.setExpiresAt(Instant.now().plusSeconds(properties.getJwt().getRefreshDays() * 86400)); refresh.setUserAgent(userAgent); refresh.setIpAddress(ip); refreshTokens.save(refresh); return new AuthResponse(jwt.createAccessToken(user), raw, jwt.accessExpiresInSeconds()); }
    private void sendToken(User user, TokenPurpose purpose, String subject) { String raw = tokens.generate(); VerificationToken token = new VerificationToken(); token.setUser(user); token.setPurpose(purpose); token.setTokenHash(tokens.sha256(raw)); token.setExpiresAt(Instant.now().plusSeconds(3600)); verificationTokens.save(token); email.send(user.getEmail(), subject, "Token: " + raw); }
    private VerificationToken token(String raw, TokenPurpose purpose) { VerificationToken token = verificationTokens.findForUpdate(tokens.sha256(raw)).orElseThrow(() -> new UnauthorizedException("Invalid token")); if (token.getPurpose() != purpose || token.getUsedAt() != null || !token.getExpiresAt().isAfter(Instant.now())) throw new UnauthorizedException("Invalid or expired token"); return token; }
    private User user(Long id) { return users.findById(id).orElseThrow(() -> new NotFoundException("User not found")); }
}
