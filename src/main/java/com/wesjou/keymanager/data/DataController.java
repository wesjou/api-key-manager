package com.wesjou.keymanager.data;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class DataController {

    @GetMapping("/data")
    ResponseEntity<String> getData() {
        return ResponseEntity.ok("Access granted!");
    }
}
