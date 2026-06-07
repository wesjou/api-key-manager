package com.wesjou.keymanager.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @Schema(description = "The email address of the user", example = "user@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @Schema(description = "The password for the account (minimum 8 characters)", example = "strongPassword123")
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Minimum password is 8 characters")
        String password
) {
}
