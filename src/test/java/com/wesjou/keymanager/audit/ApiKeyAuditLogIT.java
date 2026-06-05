package com.wesjou.keymanager.audit;

import com.wesjou.keymanager.jwt.JwtService;
import com.wesjou.keymanager.user.Role;
import com.wesjou.keymanager.user.User;
import com.wesjou.keymanager.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class ApiKeyAuditLogIT {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuditLogRepository auditLogRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtService jwtService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Sql(statements = {"DELETE FROM apikey_scopes", "DELETE FROM apikeys", "DELETE FROM audit_logs", "DELETE FROM " +
            "users"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    void shouldCreateAuditLog_whenCreatingApiKey() throws Exception {
        var user = User.builder()
                .email("user@test.com")
                .password(passwordEncoder.encode("test123"))
                .role(Role.ADMIN).build();
        userRepository.save(user);

        var authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), "test123"));
        var jwt = jwtService.generateToken(authentication);

        var userId = user.getId();

        mockMvc.perform(post("/api/v1/users/{userId}/apikeys", userId)
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scopes\": [\"WRITE\"]}"))
                .andExpect(status().isCreated());

        var log = auditLogRepository.findTop50ByOrderByCreatedAtDesc();
        assertThat(log.getFirst().getAction()).isEqualTo(AuditAction.API_KEY_CREATED);
        assertThat(log.getFirst().getActorId()).isEqualTo(user.getEmail());
        assertThat(log.getFirst().getResourceType()).isEqualTo(AuditResourceType.API_KEY);
        assertThat(log.getFirst().isSuccess()).isTrue();
    }
}
