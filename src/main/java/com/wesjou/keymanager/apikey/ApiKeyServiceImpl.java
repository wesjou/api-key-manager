package com.wesjou.keymanager.apikey;

import com.wesjou.keymanager.exception.ApiKeyGenerationException;
import com.wesjou.keymanager.exception.ApiKeyNotFoundException;
import com.wesjou.keymanager.exception.UserNotFoundException;
import com.wesjou.keymanager.user.User;
import com.wesjou.keymanager.user.UserRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
class ApiKeyServiceImpl implements ApiKeyService {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;

    ApiKeyServiceImpl(ApiKeyRepository apiKeyRepository, UserRepository userRepository) {
        this.apiKeyRepository = apiKeyRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ApiKeyResponse generateApiKey(Long userId) throws NoSuchAlgorithmException {

        User user = userRepository.findById(userId).orElseThrow(ApiKeyGenerationException::new);

        byte[] rawKey = new byte[32];
        secureRandom.nextBytes(rawKey);

        String encodedKey = "ak_" + encoder.encodeToString(rawKey);

        // for database storing
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedKey = digest.digest(encodedKey.getBytes(StandardCharsets.UTF_8));
        String encodedHashedKey = encoder.encodeToString(hashedKey);

        ApiKey apiKey = new ApiKey();
        apiKey.setUser(user);
        apiKey.setKeyHash(encodedHashedKey);
        apiKey.setRevoked(false);

        apiKeyRepository.save(apiKey);

        return new ApiKeyResponse(encodedKey);
    }

    @Override
    public List<ApiKeyInfoResponse> getApiKeys(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        List<ApiKey> listApiKeys = apiKeyRepository.findAllByUser(user);

        return listApiKeys.stream()
                .map(apiKey -> new ApiKeyInfoResponse(
                        apiKey.getId(),
                        apiKey.isRevoked(),
                        apiKey.getCreatedAt()
                )).toList();
    }

    @Override
    public void revokeApiKey(Long apiKeyId) {
        ApiKey apiKey = apiKeyRepository.findById(apiKeyId).orElseThrow(ApiKeyNotFoundException::new);

        apiKey.setRevoked(true);
        apiKeyRepository.save(apiKey);
    }

    @Override
    public boolean isValid(String apiKey) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedKey = digest.digest(apiKey.getBytes(StandardCharsets.UTF_8));
        String encodedHashedKey = encoder.encodeToString(hashedKey);

        Optional<ApiKey> apiKeyOptional = apiKeyRepository.findByKeyHash(encodedHashedKey);

        if (apiKeyOptional.isEmpty()) {
            return false;
        }

        ApiKey storedHashedKey = apiKeyOptional.get();

        return !storedHashedKey.isRevoked();
    }

}
