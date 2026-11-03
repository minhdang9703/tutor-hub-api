package com.tutorhub.common.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import com.tutorhub.common.config.AppProperties;
import com.tutorhub.user.entity.User;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final AppProperties.Jwt properties;
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    public JwtService(AppProperties properties) {
        this.properties = properties.getJwt();
        byte[] key = Base64.getDecoder().decode(this.properties.getSecret());
        if (key.length < 32) throw new IllegalArgumentException("JWT secret must be at least 256 bits");
        SecretKey secretKey = new SecretKeySpec(key, "HmacSHA256");
        encoder = new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(secretKey));
        decoder = NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
    }
    public String createAccessToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().subject(user.getId().toString()).issuedAt(now).expiresAt(now.plusSeconds(properties.getAccessMinutes() * 60)).claim("role", user.getRole().name()).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
    public UserPrincipal parse(String token) {
        Jwt jwt = decoder.decode(token);
        return new UserPrincipal(Long.valueOf(jwt.getSubject()), com.tutorhub.user.entity.Role.valueOf(jwt.getClaimAsString("role")));
    }
    public long accessExpiresInSeconds() { return properties.getAccessMinutes() * 60; }
}
