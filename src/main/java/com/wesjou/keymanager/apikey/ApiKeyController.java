package com.wesjou.keymanager.apikey;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;
import java.util.List;

@RequestMapping("/api/v1/users/{userId}/apikeys")
@RestController
class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ApiKeyResponse generateApiKey(@PathVariable Long userId) throws NoSuchAlgorithmException {
        return apiKeyService.generateApiKey(userId);
    }

    @GetMapping
    List<ApiKeyInfoResponse> listApiKeys(@PathVariable Long userId) {
        return apiKeyService.getApiKeys(userId);
    }

    @DeleteMapping("/{apiKeyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void revokeApiKey(@PathVariable Long apiKeyId, @PathVariable Long userId) {
        apiKeyService.revokeApiKey(apiKeyId, userId);
    }
}
