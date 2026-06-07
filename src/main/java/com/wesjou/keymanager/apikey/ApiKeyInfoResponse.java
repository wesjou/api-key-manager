package com.wesjou.keymanager.apikey;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;

public record ApiKeyInfoResponse(
        @Schema(description = "The unique identifier of the API key entry", example = "1")
        Long id,

        @Schema(description = "The public identifier of the API key", example = "ak_abc123")
        String publicId,

        @Schema(description = "The permissions associated with this API key", example = "[\"READ\"]")
        Set<Scope> scopes,

        @Schema(description = "Whether the API key has been revoked", example = "false")
        boolean revoked,

        @Schema(description = "When the API key was created", example = "2026-06-06T10:00:00")
        LocalDateTime createdAt
) {
}
