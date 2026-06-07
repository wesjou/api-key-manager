package com.wesjou.keymanager.user;

import com.wesjou.keymanager.jwt.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Endpoints for user authentication and token management.")
@RestController
class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Operation(summary = "User Login", description = "Authenticates a user with email and password and returns a JWT access token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated, returns JWT token"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    @PostMapping("/api/v1/login")
    String login(@Valid @RequestBody LoginRequest request) {
        var authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(),
                        request.password()));

        return jwtService.generateToken(authentication);
    }
}
