package com.wesjou.keymanager.apikey;

import java.time.LocalDateTime;
import java.util.Set;

record ApiKeyInfoResponse(Long id, String publicId, Set<Scope> scopes, boolean revoked, LocalDateTime createdAt) {
}
