package com.wesjou.keymanager.apikey;

import java.time.LocalDateTime;

record ApiKeyInfoResponse(Long id, String publicId, boolean revoked, LocalDateTime createdAt) {
}
