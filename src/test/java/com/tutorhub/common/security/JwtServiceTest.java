package com.tutorhub.common.security;

import static org.junit.jupiter.api.Assertions.*;
import com.tutorhub.common.config.AppProperties;
import com.tutorhub.user.entity.*;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    @Test void createsAndParsesAnAccessToken() {
        AppProperties properties = new AppProperties();
        properties.getJwt().setSecret(Base64.getEncoder().encodeToString("01234567890123456789012345678901".getBytes()));
        JwtService service = new JwtService(properties);
        User user = new User(); user.setRole(Role.TUTOR);
        setId(user, 42L);
        UserPrincipal principal = service.parse(service.createAccessToken(user));
        assertEquals(42L, principal.id());
        assertEquals(Role.TUTOR, principal.role());
    }
    private void setId(User user, Long id) { try { var field = User.class.getDeclaredField("id"); field.setAccessible(true); field.set(user, id); } catch (ReflectiveOperationException e) { throw new AssertionError(e); } }
}
