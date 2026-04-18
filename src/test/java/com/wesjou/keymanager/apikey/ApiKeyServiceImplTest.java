package com.wesjou.keymanager.apikey;

import com.wesjou.keymanager.user.User;
import com.wesjou.keymanager.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceImplTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ApiKeyServiceImpl apiKeyService;

    @Test
    void generateApiKey_shouldReturnKeyWithExpiry() throws NoSuchAlgorithmException {
        Long userId = 123L;
        User mockedUser = new User();
        mockedUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockedUser));

        ApiKeyResponse apiKeyGeneration = apiKeyService.generateApiKey(userId);

        assertNotNull(apiKeyGeneration);
        assertTrue(apiKeyGeneration.expiresAt().isBefore(LocalDateTime.now().plusDays(31)));
        assertTrue(apiKeyGeneration.expiresAt().isAfter(LocalDateTime.now().plusDays(29)));
    }

    @Test
    void getApiKeys() {
    }

    @Test
    void revokeApiKey() {
    }

    @Test
    void isValid() {
    }
}