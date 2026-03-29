package com.wesjou.keymanager.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

record CreateUserRequest(@NotBlank @Email String email) {

}
