package com.test.ticket.controller;

import com.test.ticket.dto.Auth.AuthRequest;
import com.test.ticket.dto.Auth.LoginResponse;
import com.test.ticket.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Value("${app.jwt.cookie-name}")
    private String cookieName;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequest request) {
        authService.register(request);
        return ResponseEntity.ok(Map.of("message", "Registrasi berhasil"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        LoginResponse result = authService.login(request);

        // HttpOnly Cookie
        ResponseCookie cookie = ResponseCookie.from(cookieName, result.getToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(jwtExpirationMs / 1000)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of(
                        "message", "Login berhasil",
                        "role", result.getRole()
                ));
    }
}
