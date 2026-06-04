package com.wesjou.keymanager.apikey;

import com.wesjou.keymanager.jwt.JwtService;
import com.wesjou.keymanager.user.Role;
import com.wesjou.keymanager.user.User;
import com.wesjou.keymanager.user.UserRepository;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ApiKeyAuthenticationIT {
    @Autowired
    UserRepository userRepository;

    @Autowired
    ApiKeyRepository apiKeyRepository;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtService jwtService;

    @Autowired
    AuthenticationManager authenticationManager;

    @AfterEach
    void tearDown() {
        apiKeyRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void apiKey_shouldAccessReadEndpoint() throws Exception {
        var user = userRepository.save(createUser(Role.USER));

        var key = "secret";
        var apikey = apiKeyRepository.save(createApiKey(user, key, Set.of(Scope.READ)));

        var rawApiKey = apikey.getPublicId() + "." + key;

        mockMvc.perform(get("/api/v1/data")
                        .header("x-api-key", rawApiKey))
                .andExpect(status().isOk())
                .andExpect(content().string("Access granted!"));
    }

    @Test
    void apiKey_shouldNotAccessCreateEndpoint() throws Exception {
        var user = userRepository.save(createUser(Role.USER));

        var key = "secret";
        var apikey = apiKeyRepository.save(createApiKey(user, key, Set.of(Scope.READ)));

        var rawApiKey = apikey.getPublicId() + "." + key;

        mockMvc.perform(post("/api/v1/data")
                        .header("x-api-key", rawApiKey))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.message").value("User scope access is not allowed"));
    }

    @Test
    void adminUser_shouldCreateAdminScopedKey() throws Exception {
        var user = userRepository.save(createUser(Role.ADMIN));

        var authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), "test123"));
        var jwt = jwtService.generateToken(authentication);

        var userId = user.getId();

        mockMvc.perform(post("/api/v1/users/{userId}/apikeys", userId)
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scopes\": [\"ADMIN\"]}"))
                .andExpect(status().isCreated());

        var keys = apiKeyRepository.findAllByUser(user);
        assertThat(keys).hasSize(1);
        assertThat(keys.getFirst().getScopes()).contains(Scope.ADMIN);
    }

    @Test
    void shouldReturnForbidden_whenNormalUserCreatesAdminScopeKey() throws Exception {
        var user = userRepository.save(createUser(Role.USER));

        var authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), "test123"));
        var jwt = jwtService.generateToken(authentication);

        var userId = user.getId();

        mockMvc.perform(post("/api/v1/users/{userId}/apikeys", userId)
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scopes\": [\"ADMIN\"]}"))
                .andExpect(status().isForbidden());

        var keys = apiKeyRepository.findAllByUser(user);
        assertThat(keys).hasSize(0);
    }

    private String hashRawKey(@NonNull String key) throws NoSuchAlgorithmException {
        var digest = MessageDigest.getInstance("SHA-256");
        var hashedKey = digest.digest(key.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hashedKey);
    }

    private User createUser(Role role) {
        return User.builder()
                .email("user@test.com")
                .password(passwordEncoder.encode("test123"))
                .role(role).build();
    }

    private ApiKey createApiKey(User user, String key, Set<Scope> scopes) throws NoSuchAlgorithmException {
        return ApiKey.builder()
                .publicId("ak_test")
                .keyHash(hashRawKey(key))
                .scopes(scopes)
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .user(user).build();
    }
}
