package com.wesjou.keymanager.apikey;

import com.wesjou.keymanager.exception.BadApiKeyException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    private final ApiKeyService apiKeyService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    ApiKeyAuthFilter(ApiKeyService apiKeyService, HandlerExceptionResolver handlerExceptionResolver) {
        this.apiKeyService = apiKeyService;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("x-api-key");
            if (authHeader == null || !apiKeyService.isValid(authHeader)) {
                throw new BadApiKeyException();
            }

            filterChain.doFilter(request, response);
        } catch (BadApiKeyException | NoSuchAlgorithmException e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return !path.startsWith("/api/v1/data");
    }
}
