package com.wesjou.keymanager.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new FakeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void handleApiKeyNotFound_withApiKey_returnNotFound() throws Exception {
        mockMvc.perform(get("/test-404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.message").value("API key not found"));
    }

    @Test
    void handleBadApiKey_whenInvalidKey_returnUnauthorized() throws Exception {
        mockMvc.perform(get("/test-401"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.message").value("Invalid API key"));
    }

    @Test
    void handleApiKeyScopeDenied_withApiKeyScope_returnForbidden() throws Exception {
        mockMvc.perform(get("/test-403"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.message").value("User scope access is not allowed"));
    }

    @Test
    void handleInvalidScope_withInvalidScope_returnBadRequest() throws Exception {
        mockMvc.perform(get("/test-400"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Scopes must not be empty"));
    }

    @RestController
    private static class FakeController {
        @GetMapping("/test-404")
        void throw404() {
            throw new ApiKeyNotFoundException();
        }

        @GetMapping("/test-401")
        void throw401() {
            throw new BadApiKeyException();
        }

        @GetMapping("/test-403")
        void throw403() {
            throw new ApiKeyScopeDeniedException();
        }

        @GetMapping("/test-400")
        void throw400() {
            throw new InvalidScopeException();
        }
    }
}
