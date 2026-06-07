package com.wesjou.keymanager.apikey;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;
import java.util.List;

@SecurityRequirement(name = "jwtAuth")
@Tag(name = "API Keys", description = "Endpoints for creating, listing, and revoking API keys.")
@RequestMapping("/api/v1/users/{userId}/apikeys")
@RestController
class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Operation(summary = "Generate a new API Key", description = "Creates a secure API key for the specified user. Only the user themselves or an admin can generate keys.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "API Key successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or scopes"),
            @ApiResponse(responseCode = "403", description = "Permission denied"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ApiKeyResponse generateApiKey(
            @Parameter(description = "The ID of the user to generate the key for", example = "1")
            @PathVariable Long userId,
            @Valid @RequestBody CreateApiKeyRequest createApiKeyRequest) throws NoSuchAlgorithmException {
        return apiKeyService.generateApiKey(userId, createApiKeyRequest);
    }

    @Operation(summary = "List all API keys for a user", description = "Retrieves a list of all API keys (public information only) for the specified user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved API keys"),
            @ApiResponse(responseCode = "403", description = "Permission denied"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping
    List<ApiKeyInfoResponse> listApiKeys(
            @Parameter(description = "The ID of the user whose keys to list", example = "1")
            @PathVariable Long userId) {
        return apiKeyService.getApiKeys(userId);
    }

    @Operation(summary = "Revoke an API Key", description = "Marks an API key as revoked. Revoked keys can no longer be used. Only the key owner or an admin can revoke keys.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "API Key successfully revoked"),
            @ApiResponse(responseCode = "403", description = "Permission denied"),
            @ApiResponse(responseCode = "404", description = "API Key or User not found")
    })
    @DeleteMapping("/{apiKeyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void revokeApiKey(
            @Parameter(description = "The ID of the API key to revoke", example = "1")
            @PathVariable Long apiKeyId,
            @Parameter(description = "The ID of the user who owns the key", example = "1")
            @PathVariable Long userId) {
        apiKeyService.revokeApiKey(apiKeyId, userId);
    }
}
