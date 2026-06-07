package com.wesjou.keymanager.apikey;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record CreateApiKeyRequest(
        @Schema(description = "The set of permissions granted to the API key", example = "[\"READ\", \"WRITE\", " +
                "\"ADMIN\"]")
        @NotEmpty
        Set<Scope> scopes
) {
}
