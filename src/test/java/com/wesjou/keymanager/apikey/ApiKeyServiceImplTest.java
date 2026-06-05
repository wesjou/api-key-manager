package com.wesjou.keymanager.apikey;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class ApiKeyServiceImplTest {
    @Mock
    ApiKeyRepository apiKeyRepository;

    @InjectMocks
    ApiKeyServiceImpl apiKeyService;

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "invalid-key-format", "too.many.dots.key", "ak_123.secret.extra"})
    void isValid_withInvalidApiKey_returnFalse(String apiKey) throws NoSuchAlgorithmException {
        var result = apiKeyService.isValid(apiKey);
        assertThat(result).isFalse();
        verifyNoInteractions(apiKeyRepository);
    }
}
