package com.wesjou.keymanager.apikey;

import com.wesjou.keymanager.exception.ApiKeyAccessDeniedException;
import com.wesjou.keymanager.exception.ApiKeyNotFoundException;
import com.wesjou.keymanager.exception.ApiKeyScopeDeniedException;
import com.wesjou.keymanager.exception.AuthenticatedUserNotFoundException;
import com.wesjou.keymanager.exception.InvalidScopeException;
import com.wesjou.keymanager.exception.UnauthenticatedException;
import com.wesjou.keymanager.exception.UserNotFoundException;
import com.wesjou.keymanager.user.Role;
import com.wesjou.keymanager.user.User;
import com.wesjou.keymanager.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
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
    public ApiKeyResponse generateApiKey(Long userId, CreateApiKeyRequest createApiKeyRequest) throws NoSuchAlgorithmException {
        var scopes = createApiKeyRequest.scopes();
        if (scopes == null || scopes.isEmpty()) {
            throw new InvalidScopeException();
        }

        var authUser = authorizeApiKeyAccess(userId);
        if (authUser.getRole() != Role.ADMIN && scopes.contains(Scope.ADMIN)) {
            throw new ApiKeyScopeDeniedException();
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

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
        apiKey.setScopes(createApiKeyRequest.scopes());

        apiKeyRepository.save(apiKey);

        return new ApiKeyResponse(fullKey, publicId, apiKey.getExpiresAt());
    }

    @Override
    public List<ApiKeyInfoResponse> getApiKeys(Long userId) {
        authorizeApiKeyAccess(userId);

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
    public void revokeApiKey(Long apiKeyId, Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new UnauthenticatedException();
        }

        boolean hasAdminRole = authentication.getAuthorities().stream()
                .anyMatch(r -> Objects.equals(r.getAuthority(), "ROLE_ADMIN"));

        ApiKey apiKey = apiKeyRepository.findById(apiKeyId).orElseThrow(ApiKeyNotFoundException::new);

        String email = authentication.getName();
        User currentAuthUser = userRepository.findByEmail(email).orElseThrow(AuthenticatedUserNotFoundException::new);
        boolean isAuthUser = currentAuthUser.getId().equals(userId) || hasAdminRole;
        boolean isOwner = apiKey.getUser().getId().equals(userId);
        if (!(isAuthUser && isOwner)) {
            throw new ApiKeyAccessDeniedException();
        }

        apiKey.setRevoked(true);
        apiKeyRepository.save(apiKey);
    }

    @Override
    public boolean hasScope(String apiKey, Scope requiredScope) throws NoSuchAlgorithmException {
        var storedKey = authorizeApiKey(apiKey);
        if (storedKey.isEmpty()) {
            return false;
        }

        var scopes = storedKey.get().getScopes();
        if (scopes == null || scopes.isEmpty() || requiredScope == null) {
            return false;
        }

        return scopes.contains(Scope.ADMIN) || scopes.contains(requiredScope);
    }

    private Optional<ApiKey> authorizeApiKey(String apiKey) throws NoSuchAlgorithmException {
        String[] parts = apiKey.split("\\.");
        if (parts.length != 2) {
            return Optional.empty();
        }

        String publicId = parts[0];
        String secretKey = parts[1];

        Optional<ApiKey> apiKeyOptional = apiKeyRepository.findByPublicId(publicId);
        if (apiKeyOptional.isEmpty() || isInvalid(apiKeyOptional.get())) {
            return Optional.empty();
        }

        ApiKey storedApiKey = apiKeyOptional.get();

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedSecretKey = digest.digest(secretKey.getBytes(StandardCharsets.UTF_8));
        String providedSecretKey = encoder.encodeToString(hashedSecretKey);

        if (!MessageDigest.isEqual(providedSecretKey.getBytes(StandardCharsets.UTF_8),
                storedApiKey.getKeyHash().getBytes(StandardCharsets.UTF_8))) {
            return Optional.empty();
        } else {
            return Optional.of(storedApiKey);
        }
    }

    private boolean isInvalid(ApiKey key) {
        boolean isRevoked = key.isRevoked();
        boolean isExpired = key.getExpiresAt() != null && key.getExpiresAt().isBefore(LocalDateTime.now());

        return isRevoked || isExpired;
    }

    private User authorizeApiKeyAccess(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new UnauthenticatedException();
        }

        String email = authentication.getName();
        User currentAuthUser = userRepository.findByEmail(email)
                .orElseThrow(AuthenticatedUserNotFoundException::new);

        boolean hasAdminRole = authentication.getAuthorities().stream()
                .anyMatch(r -> Objects.equals(r.getAuthority(), "ROLE_ADMIN"));
        if (!userId.equals(currentAuthUser.getId()) && !hasAdminRole) {
            throw new ApiKeyAccessDeniedException();
        }
        return currentAuthUser;
    }

}
