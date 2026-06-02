package com.wesjou.keymanager.apikey;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

record CreateApiKeyRequest(@NotEmpty Set<Scope> scopes) {
}
