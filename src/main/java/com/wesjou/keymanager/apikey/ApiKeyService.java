package com.wesjou.keymanager.apikey;

import java.security.NoSuchAlgorithmException;
import java.util.List;

interface ApiKeyService {
    ApiKeyResponse generateApiKey(Long userId) throws NoSuchAlgorithmException;
    List<ApiKeyInfoResponse> getApiKeys(Long userId);
    void revokeApiKey(Long apiKeyId);
    boolean isValid(String apiKey) throws NoSuchAlgorithmException;
}
