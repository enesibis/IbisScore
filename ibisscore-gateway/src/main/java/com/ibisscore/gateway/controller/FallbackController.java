package com.ibisscore.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/user")
    public ResponseEntity<Map<String, String>> userFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", "Kullanıcı servisi şu an kullanılamıyor. Lütfen tekrar deneyin."));
    }

    @GetMapping("/match")
    public ResponseEntity<Map<String, String>> matchFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", "Maç servisi şu an kullanılamıyor. Lütfen tekrar deneyin."));
    }

    @GetMapping("/betting")
    public ResponseEntity<Map<String, String>> bettingFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", "Bahis servisi şu an kullanılamıyor. Lütfen tekrar deneyin."));
    }
}
