package com.wesjou.keymanager.apikey;

import java.security.NoSuchAlgorithmException;
import java.util.List;

interface ApiKeyService {
    ApiKeyResponse generateApiKey(Long userId, CreateApiKeyRequest createApiKeyRequest) throws NoSuchAlgorithmException;
    List<ApiKeyInfoResponse> getApiKeys(Long userId);
    void revokeApiKey(Long apiKeyId, Long userId);
    boolean hasScope(String apiKey, Scope scope) throws NoSuchAlgorithmException;
}
