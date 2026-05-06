package com.wesjou.keymanager.limiter;

import com.wesjou.keymanager.exception.AuthenticatedUserNotFoundException;
import com.wesjou.keymanager.exception.RateLimitExceededException;
import com.wesjou.keymanager.exception.UnauthenticatedException;
import com.wesjou.keymanager.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

import static com.wesjou.keymanager.limiter.RateLimitPolicy.AUTH;
import static com.wesjou.keymanager.limiter.RateLimitPolicy.LOGIN;
import static com.wesjou.keymanager.limiter.RateLimitPolicy.NONE;
import static com.wesjou.keymanager.limiter.RateLimitPolicy.REGISTER;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final UserRepository userRepository;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public RateLimitingFilter(RateLimitService rateLimitService, UserRepository userRepository,
                              HandlerExceptionResolver handlerExceptionResolver) {
        this.rateLimitService = rateLimitService;
        this.userRepository = userRepository;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            var path = request.getServletPath();
            var method = request.getMethod();

            var policy = checkPolicy(path, method);

            if (policy == NONE) {
                filterChain.doFilter(request, response);
                return;
            }

            if (policy == LOGIN || policy == REGISTER) {
                var publicIdentityKey = "ip:" + request.getRemoteAddr();
                rateLimitService.enforceRateLimit(publicIdentityKey, policy);
            } else {
                var authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null) {
                    throw new UnauthenticatedException();
                }

                var email = authentication.getName();
                var currentAuthUser = userRepository.findByEmail(email).orElseThrow(AuthenticatedUserNotFoundException::new);
                var authIdentityKey = "user:" + currentAuthUser.getId();

                rateLimitService.enforceRateLimit(authIdentityKey, policy);
            }
            filterChain.doFilter(request, response);
        } catch (RateLimitExceededException | UnauthenticatedException | AuthenticatedUserNotFoundException e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }

    private RateLimitPolicy checkPolicy(String path, String method) {
        return switch (path) {
            case String p when p.equals("/api/v1/login") && method.equals("POST") -> LOGIN;
            case String p when p.equals("/api/v1/users") && method.equals("POST") -> REGISTER;
            case String p when p.matches("/api/v1/users/\\d+/apikeys") && method.equals("GET") -> AUTH;
            case String p when p.matches("/api/v1/users/\\d+/apikeys") && method.equals("POST") -> AUTH;
            case String p when p.matches("/api/v1/users/\\d+/apikeys/\\d+") && method.equals("DELETE") -> AUTH;
            default -> NONE;
        };
    }

}
