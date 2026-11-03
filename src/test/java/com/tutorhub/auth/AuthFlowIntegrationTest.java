package com.tutorhub.auth;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutorhub.TutorHubApplication;
import com.tutorhub.auth.service.EmailSender;
import com.tutorhub.support.PostgresIntegrationTest;
import com.tutorhub.user.entity.UserStatus;
import com.tutorhub.user.repository.UserRepository;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = TutorHubApplication.class, properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@Import(AuthFlowIntegrationTest.EmailConfig.class)
class AuthFlowIntegrationTest extends PostgresIntegrationTest {
    @Autowired MockMvc mvc; @Autowired ObjectMapper json; @Autowired TestEmailSender email; @Autowired UserRepository users;
    @Test void healthIsUpAfterFlywayMigration() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/actuator/health"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status").value("UP"));
    }
    @Test void registerVerifyLoginRefreshAndLogout() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"parent@example.com\",\"password\":\"password123\",\"fullName\":\"Parent One\",\"role\":\"PARENT\"}")) .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/verify-email").contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"" + email.token() + "\"}")) .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNoContent());
        String login = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"parent@example.com\",\"password\":\"password123\"}")) .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk()).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.accessToken", notNullValue())).andReturn().getResponse().getContentAsString();
        String firstRefresh = json.readTree(login).get("refreshToken").asText();
        String refresh = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON).content("{\"refreshToken\":\"" + firstRefresh + "\"}")) .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk()).andReturn().getResponse().getContentAsString();
        String secondRefresh = json.readTree(refresh).get("refreshToken").asText();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON).content("{\"refreshToken\":\"" + firstRefresh + "\"}")) .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/logout").contentType(MediaType.APPLICATION_JSON).content("{\"refreshToken\":\"" + secondRefresh + "\"}")) .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNoContent());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON).content("{\"refreshToken\":\"" + secondRefresh + "\"}")) .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
    }
    @Test void suspendedUserCannotLogin() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"suspended@example.com\",\"password\":\"password123\",\"fullName\":\"Suspended\",\"role\":\"TUTOR\"}")) .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated());
        var user = users.findByEmailIgnoreCase("suspended@example.com").orElseThrow(); user.setStatus(UserStatus.SUSPENDED); users.save(user);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"suspended@example.com\",\"password\":\"password123\"}")) .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
    }
    @TestConfiguration static class EmailConfig { @Bean @Primary TestEmailSender testEmailSender() { return new TestEmailSender(); } }
    static class TestEmailSender implements EmailSender { private String body; public void send(String recipient, String subject, String body) { this.body = body; } String token() { Matcher matcher = Pattern.compile("Token: (\\S+)").matcher(body); assertNotNull(body); if (!matcher.find()) throw new AssertionError("No token sent"); return matcher.group(1); } }
}
