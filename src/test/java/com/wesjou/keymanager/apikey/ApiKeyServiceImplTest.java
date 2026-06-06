package com.wesjou.keymanager.apikey;

import com.wesjou.keymanager.exception.ApiKeyAccessDeniedException;
import com.wesjou.keymanager.user.Role;
import com.wesjou.keymanager.user.User;
import com.wesjou.keymanager.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApiKeyServiceImplTest {
    @Mock
    ApiKeyRepository apiKeyRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    ApiKeyServiceImpl apiKeyService;

    @Test
    void generateApiKey_withApiKey_returnValidResponseAndSaveToDb() throws NoSuchAlgorithmException {
        var userId = 1L;
        var user = User.builder().id(userId).email("user@test.com").role(Role.USER).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        var auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(user.getEmail());
        var securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        var request = new CreateApiKeyRequest(Set.of(Scope.READ));
        var response = apiKeyService.generateApiKey(userId, request);

        assertThat(response.publicId()).startsWith("ak_");
        assertThat(response.expiresAt()).isCloseTo(LocalDateTime.now().plusDays(30), within(2, ChronoUnit.SECONDS));

        var apiKeyCaptor = ArgumentCaptor.forClass(ApiKey.class);
        verify(apiKeyRepository).save(apiKeyCaptor.capture());

        var savedApiKey = apiKeyCaptor.getValue();
        assertThat(savedApiKey.getUser()).isEqualTo(user);
        assertThat(savedApiKey.getPublicId()).isEqualTo(response.publicId());
        assertThat(savedApiKey.getScopes()).contains(Scope.READ);

        // check if stored key is hashed
        var rawSecret = response.apiKey().split("\\.")[1];
        assertThat(savedApiKey.getKeyHash()).isNotEqualTo(rawSecret);
    }

    @Test
    void revokeApiKey_whenUserIsOwner_returnRevokedTrue() {
        var userId = 1L;
        var user = User.builder().id(userId).email("user@test.com").build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        var apiKey = ApiKey.builder()
                .id(10L)
                .user(user)
                .revoked(false)
                .build();
        when(apiKeyRepository.findById(10L)).thenReturn(Optional.of(apiKey));

        mockNormalUser(user.getEmail());

        apiKeyService.revokeApiKey(apiKey.getId(), userId);
        assertThat(apiKey.isRevoked()).isTrue();
        verify(apiKeyRepository).save(apiKey);
    }

    @Test
    void revokeApiKey_whenUserIsNotOwner_throwApiKeyAccessDenied() {
        var userA = User.builder().id(1L).email("userA@test.com").build();
        var userB = User.builder().id(2L).email("userB@test.com").build();
        when(userRepository.findByEmail(userB.getEmail())).thenReturn(Optional.of(userB));

        var apiKey = ApiKey.builder()
                .id(10L)
                .user(userA)
                .revoked(false)
                .build();
        when(apiKeyRepository.findById(10L)).thenReturn(Optional.of(apiKey));

        mockNormalUser(userB.getEmail());

        assertThatThrownBy(() -> apiKeyService.revokeApiKey(apiKey.getId(), userA.getId())).isInstanceOf(ApiKeyAccessDeniedException.class);
        assertThat(apiKey.isRevoked()).isFalse();
        verify(apiKeyRepository, never()).save(any());
    }

    @Test
    void revokeApiKey_whenAdminAndNotOwner_returnRevokedTrue() {
        var user = User.builder().id(1L).email("user@test.com").build();
        var admin = User.builder().id(2L).email("admin@test.com").build();
        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));

        var apiKey = ApiKey.builder()
                .id(10L)
                .user(user)
                .revoked(false)
                .build();
        when(apiKeyRepository.findById(10L)).thenReturn(Optional.of(apiKey));

        mockAdminUser(admin.getEmail());

        apiKeyService.revokeApiKey(apiKey.getId(), user.getId());
        assertThat(apiKey.isRevoked()).isTrue();
        verify(apiKeyRepository).save(apiKey);
    }

    @Test
    void hasScope_whenAdminScopeIsPresent_returnTrue() throws NoSuchAlgorithmException {
        var publicId = "ak_test";
        var secret = "secret";

        var digest = MessageDigest.getInstance("SHA-256");
        var hashedKey = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
        var encodedHashedKey = Base64.getUrlEncoder().withoutPadding().encodeToString(hashedKey);

        var apiKey = ApiKey.builder()
                .publicId(publicId)
                .keyHash(encodedHashedKey)
                .scopes(Set.of(Scope.ADMIN))
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        when(apiKeyRepository.findByPublicId(publicId)).thenReturn(Optional.of(apiKey));

        var result = apiKeyService.hasScope(publicId + "." + secret, Scope.READ);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "invalid-key-format", "too.many.dots.key", "ak_123.secret.extra"})
    void isValid_withInvalidApiKey_returnFalse(String apiKey) throws NoSuchAlgorithmException {
        var result = apiKeyService.isValid(apiKey);
        assertThat(result).isFalse();
        verifyNoInteractions(apiKeyRepository);
    }

    private void mockNormalUser(String email) {
        mockUser(email, "ROLE_USER");
    }

    private void mockAdminUser(String email) {
        mockUser(email, "ROLE_ADMIN");
    }

    private void mockUser(String email, String role) {
        var auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(email);
        doReturn(List.of(new SimpleGrantedAuthority(role))).when(auth).getAuthorities();

        var securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }
}
