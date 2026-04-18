package com.wesjou.keymanager.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record CreateUserRequest(@NotBlank @Email String email, @NotBlank @Size(min = 8) String password) {

}
