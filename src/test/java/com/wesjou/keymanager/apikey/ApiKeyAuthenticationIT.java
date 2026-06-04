package com.wesjou.keymanager.apikey;

import com.wesjou.keymanager.user.Role;
import com.wesjou.keymanager.user.User;
import com.wesjou.keymanager.user.UserRepository;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ApiKeyAuthenticationIT {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    PasswordEncoder passwordEncoder;

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
