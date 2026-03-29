package com.wesjou.keymanager.apikey;

import java.time.LocalDateTime;

record ApiKeyInfoResponse(Long id, boolean revoked, LocalDateTime createdAt) {
}
