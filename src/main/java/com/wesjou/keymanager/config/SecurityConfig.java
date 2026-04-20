package com.wesjou.keymanager.config;

import com.wesjou.keymanager.exception.ErrorEnvelope;
import com.wesjou.keymanager.exception.ErrorResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/users/list", "/apikeys/", "/data/create").hasRole("ADMIN")
                        .requestMatchers("/data/read", "/users/*/apikeys").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/login", "/users/create").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            ObjectMapper mapper = new ObjectMapper();
                            String jsonString =
                                    mapper.writeValueAsString(new ErrorEnvelope(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Missing or invalid token", LocalDateTime.now())));
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write(jsonString);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            ObjectMapper mapper = new ObjectMapper();
                            String jsonString =
                                    mapper.writeValueAsString(new ErrorEnvelope(new ErrorResponse(HttpStatus.FORBIDDEN.value(), "You do not have permission to access", LocalDateTime.now())));
                            response.setStatus(403);
                            response.setContentType("application/json");
                            response.getWriter().write(jsonString);
                        }));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
        return configuration.getAuthenticationManager();
    }
}
