package com.wesjou.keymanager.apikey;

import java.time.LocalDateTime;

record ApiKeyResponse(String apiKey, String publicId, LocalDateTime expiresAt) {

}
