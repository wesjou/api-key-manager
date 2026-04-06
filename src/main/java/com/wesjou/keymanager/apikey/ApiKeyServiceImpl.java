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
import java.time.LocalDateTime;
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

        byte[] publicBytes = new byte[4];
        secureRandom.nextBytes(publicBytes);
        String publicId = "ak_" + encoder.encodeToString(publicBytes).substring(0, 6);

        byte[] secretBytes = new byte[32];
        secureRandom.nextBytes(secretBytes);
        String secretKey = encoder.encodeToString(secretBytes);

        String fullKey = publicId + "." + secretKey;

        // for database storing
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedKey = digest.digest(secretKey.getBytes(StandardCharsets.UTF_8));
        String encodedHashedKey = encoder.encodeToString(hashedKey);

        ApiKey apiKey = new ApiKey();
        apiKey.setUser(user);
        apiKey.setPublicId(publicId);
        apiKey.setKeyHash(encodedHashedKey);
        apiKey.setRevoked(false);
        apiKey.setExpiresAt(LocalDateTime.now().plusDays(30));

        apiKeyRepository.save(apiKey);

        return new ApiKeyResponse(fullKey, publicId, apiKey.getExpiresAt());
    }

    @Override
    public List<ApiKeyInfoResponse> getApiKeys(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        List<ApiKey> listApiKeys = apiKeyRepository.findAllByUser(user);

        return listApiKeys.stream()
                .map(apiKey -> new ApiKeyInfoResponse(
                        apiKey.getId(),
                        apiKey.getPublicId(),
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
        String[] parts = apiKey.split("\\.");
        if (parts.length != 2) return false;

        String publicId = parts[0];
        String secretKey = parts[1];

        Optional<ApiKey> apiKeyOptional = apiKeyRepository.findByPublicId(publicId);
        if (apiKeyOptional.isEmpty() || isInvalid(apiKeyOptional.get())) {
            return false;
        }

        ApiKey storedSecretKey = apiKeyOptional.get();

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedSecretKey = digest.digest(secretKey.getBytes(StandardCharsets.UTF_8));
        String providedSecretKey = encoder.encodeToString(hashedSecretKey);

        return MessageDigest.isEqual(providedSecretKey.getBytes(StandardCharsets.UTF_8),
                storedSecretKey.getKeyHash().getBytes(StandardCharsets.UTF_8));
    }

    private boolean isInvalid(ApiKey key) {
        boolean isRevoked = key.isRevoked();
        boolean isExpired = key.getExpiresAt() != null && key.getExpiresAt().isBefore(LocalDateTime.now());

        return isRevoked || isExpired;
    }

}
