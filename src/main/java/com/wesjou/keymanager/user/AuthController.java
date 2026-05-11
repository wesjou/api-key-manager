package com.wesjou.keymanager.user;

import com.wesjou.keymanager.jwt.JwtService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/api/v1/login")
    String login(@Valid @RequestBody LoginRequest request) {
        var authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(),
                        request.password()));

        return jwtService.generateToken(authentication);
    }
}
