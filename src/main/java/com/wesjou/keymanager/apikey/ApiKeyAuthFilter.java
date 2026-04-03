package com.wesjou.keymanager.apikey;

import com.wesjou.keymanager.exception.BadApiKeyException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    private final ApiKeyService apiKeyService;
    private final HandlerExceptionResolver resolver;

    ApiKeyAuthFilter(ApiKeyService apiKeyService, @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.apiKeyService = apiKeyService;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String apiKey = request.getHeader("x-api-key");
            if (apiKey == null || !apiKeyService.isValid(apiKey)) {
                throw new BadApiKeyException();
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            resolver.resolveException(request, response, null, e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return path.startsWith("/users") || path.startsWith("/apikeys");
    }
}
