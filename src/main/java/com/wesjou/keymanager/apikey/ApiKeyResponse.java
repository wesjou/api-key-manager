package com.wesjou.keymanager.apikey;

import java.time.LocalDateTime;

record ApiKeyResponse(String encodedKey, String publicId, LocalDateTime expiresAt) {

}
