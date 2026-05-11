package com.wesjou.keymanager.data;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/data")
class DataController {

    @GetMapping
    ResponseEntity<String> getData() {
        return ResponseEntity.ok("Access granted!");
    }

    @PostMapping
    ResponseEntity<String> createData() {
        return ResponseEntity.ok("Creation granted!");
    }

    @DeleteMapping
    ResponseEntity<String> deleteData() {
        return ResponseEntity.ok("Deletion granted!");
    }
}
