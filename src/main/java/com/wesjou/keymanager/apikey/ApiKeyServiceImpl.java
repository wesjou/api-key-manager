package com.wesjou.keymanager.apikey;

import com.wesjou.keymanager.audit.AuditAction;
import com.wesjou.keymanager.audit.AuditResourceType;
import com.wesjou.keymanager.audit.Auditable;
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

    @Auditable(action = AuditAction.API_KEY_CREATED, resourceType = AuditResourceType.API_KEY)
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

        var user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        var publicBytes = new byte[4];
        secureRandom.nextBytes(publicBytes);
        var publicId = "ak_" + encoder.encodeToString(publicBytes).substring(0, 6);

        var secretBytes = new byte[32];
        secureRandom.nextBytes(secretBytes);
        var secretKey = encoder.encodeToString(secretBytes);

        var fullKey = publicId + "." + secretKey;

        // for database storing
        var digest = MessageDigest.getInstance("SHA-256");
        var hashedKey = digest.digest(secretKey.getBytes(StandardCharsets.UTF_8));
        var encodedHashedKey = encoder.encodeToString(hashedKey);

        var apiKey = new ApiKey();
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

        var user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        var listApiKeys = apiKeyRepository.findAllByUser(user);

        return listApiKeys.stream()
                .map(apiKey -> new ApiKeyInfoResponse(
                        apiKey.getId(),
                        apiKey.getPublicId(),
                        apiKey.getScopes(),
                        apiKey.isRevoked(),
                        apiKey.getCreatedAt()
                )).toList();
    }

    @Auditable(action = AuditAction.API_KEY_REVOKED, resourceType = AuditResourceType.API_KEY, resourceIdArgIndex = 0)
    @Override
    public void revokeApiKey(Long apiKeyId, Long userId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new UnauthenticatedException();
        }

        var hasAdminRole = authentication.getAuthorities().stream()
                .anyMatch(r -> Objects.equals(r.getAuthority(), "ROLE_ADMIN"));

        var apiKey = apiKeyRepository.findById(apiKeyId).orElseThrow(ApiKeyNotFoundException::new);

        var email = authentication.getName();
        var currentAuthUser = userRepository.findByEmail(email).orElseThrow(AuthenticatedUserNotFoundException::new);
        var isAuthUser = currentAuthUser.getId().equals(userId) || hasAdminRole;
        var isOwner = apiKey.getUser().getId().equals(userId);
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

    @Override
    public boolean isValid(String apiKey) throws NoSuchAlgorithmException {
        if (apiKey == null || apiKey.isEmpty()) {
            return false;
        }
        return authorizeApiKey(apiKey).isPresent();
    }

    private Optional<ApiKey> authorizeApiKey(String apiKey) throws NoSuchAlgorithmException {
        var parts = apiKey.split("\\.");
        if (parts.length != 2) {
            return Optional.empty();
        }

        var publicId = parts[0];
        var secretKey = parts[1];

        var apiKeyOpt = apiKeyRepository.findByPublicId(publicId);
        if (apiKeyOpt.isEmpty() || isInvalid(apiKeyOpt.get())) {
            return Optional.empty();
        }

        var storedApiKey = apiKeyOpt.get();

        var digest = MessageDigest.getInstance("SHA-256");
        var hashedSecretKey = digest.digest(secretKey.getBytes(StandardCharsets.UTF_8));
        var providedSecretKey = encoder.encodeToString(hashedSecretKey);

        if (!MessageDigest.isEqual(providedSecretKey.getBytes(StandardCharsets.UTF_8),
                storedApiKey.getKeyHash().getBytes(StandardCharsets.UTF_8))) {
            return Optional.empty();
        } else {
            return Optional.of(storedApiKey);
        }
    }

    private boolean isInvalid(ApiKey key) {
        var isRevoked = key.isRevoked();
        var isExpired = key.getExpiresAt() != null && key.getExpiresAt().isBefore(LocalDateTime.now());

        return isRevoked || isExpired;
    }

    private User authorizeApiKeyAccess(Long userId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new UnauthenticatedException();
        }

        var email = authentication.getName();
        var currentAuthUser = userRepository.findByEmail(email)
                .orElseThrow(AuthenticatedUserNotFoundException::new);

        var hasAdminRole = authentication.getAuthorities().stream()
                .anyMatch(r -> Objects.equals(r.getAuthority(), "ROLE_ADMIN"));
        if (!userId.equals(currentAuthUser.getId()) && !hasAdminRole) {
            throw new ApiKeyAccessDeniedException();
        }
        return currentAuthUser;
    }
}
