package com.wesjou.keymanager.user;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserResponse(
        @Schema(description = "The unique identifier of the user", example = "1")
        Long id,

        @Schema(description = "The email address of the user", example = "user@example.com")
        String email
) {
}
