package com.wesjou.keymanager.apikey;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.List;

@RestController
class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping("/users/{userId}/apikeys")
    @ResponseStatus(HttpStatus.CREATED)
    ApiKeyResponse generateApiKey(@PathVariable Long userId) throws NoSuchAlgorithmException {
        return apiKeyService.generateApiKey(userId);
    }

    @GetMapping("/users/{userId}/apikeys")
    List<ApiKeyInfoResponse> listApiKeys(@PathVariable Long userId) {
        return apiKeyService.getApiKeys(userId);
    }

    @DeleteMapping("/apikeys/{apiKeyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void revokeApiKey(@PathVariable Long apiKeyId) {
        apiKeyService.revokeApiKey(apiKeyId);
    }
}
