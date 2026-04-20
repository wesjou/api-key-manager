package com.wesjou.keymanager.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record CreateUserRequest(@NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,
                         @NotBlank(message = "Password is required") @Size(min = 8,
                                 message = "Minimum password is 8 characters") String password) {

}
