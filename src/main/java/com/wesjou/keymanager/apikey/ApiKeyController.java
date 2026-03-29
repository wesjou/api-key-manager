package com.wesjou.keymanager.apikey;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@RestController
class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping("/users/{userId}/apikeys")
    ResponseEntity<ApiKeyResponse> generateApiKey(@PathVariable Long userId) throws NoSuchAlgorithmException {
        ApiKeyResponse apiKeyResponse = apiKeyService.generateApiKey(userId);
        return ResponseEntity.ok(apiKeyResponse);
    }

    @GetMapping("/users/{userId}/apikeys")
    ResponseEntity<List<ApiKeyInfoResponse>> listApiKeys(@PathVariable Long userId) {
        List<ApiKeyInfoResponse> listApiKeys = apiKeyService.getApiKeys(userId);
        return ResponseEntity.ok(listApiKeys);
    }

    @DeleteMapping("/apikeys/{apiKeyId}")
    ResponseEntity<Void> revokeApiKey(@PathVariable Long apiKeyId) {
        apiKeyService.revokeApiKey(apiKeyId);
        return ResponseEntity.noContent().build();
    }
}
