package com.wesjou.keymanager.apikey;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ApiKeyResponse(
        @Schema(description = "The full API key (only shown once during creation!)", example = "ak_abc123.secretKey456")
        String apiKey,

        @Schema(description = "The public identifier for the API key", example = "ak_abc123")
        String publicId,

        @Schema(description = "The expiration date and time of the API key", example = "2026-07-06T10:00:00")
        LocalDateTime expiresAt
) {
}
