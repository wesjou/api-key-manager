package com.wesjou.keymanager.data;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Data", description = "Protected sample endpoints used to demonstrate authorization flows.")
@SecurityRequirement(name = "apiKeyAuth")
@RestController
@RequestMapping("/api/v1/data")
class DataController {

    @Operation(summary = "Retrieve sample data", description = "Demonstrates access using an API key with READ scope.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access granted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing API key"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient scope")
    })
    @GetMapping
    ResponseEntity<String> getData() {
        return ResponseEntity.ok("Access granted!");
    }

    @Operation(summary = "Create sample data", description = "Demonstrates access using an API key with WRITE scope.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Creation granted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing API key"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient scope")
    })
    @PostMapping
    ResponseEntity<String> createData() {
        return ResponseEntity.ok("Creation granted!");
    }

    @Operation(summary = "Delete sample data", description = "Demonstrates access using an API key with ADMIN scope.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deletion granted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing API key"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient scope")
    })
    @DeleteMapping
    ResponseEntity<String> deleteData() {
        return ResponseEntity.ok("Deletion granted!");
    }
}
