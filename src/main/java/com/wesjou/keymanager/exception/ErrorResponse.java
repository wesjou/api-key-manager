package com.wesjou.keymanager.exception;

import java.time.LocalDateTime;

record ErrorResponse(int status, String message, LocalDateTime timestamp) {
}
