package com.wesjou.keymanager.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Users", description = "Endpoints for creating and listing users.")
@RequestMapping("/api/v1/users")
@RestController
class UserController {

    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Create a user", description = "Grants a secure access to the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @Operation(summary = "List all users", description = "Retrieves a complete list of all users registered in the " +
            "system.")
    @SecurityRequirement(name = "jwtAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All users retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Missing or invalid token"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin role required")
    })
    @GetMapping
    List<UserResponse> getAllUser() {
        return userService.getAllUser();
    }
}
