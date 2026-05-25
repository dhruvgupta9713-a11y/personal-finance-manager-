package com.finance.manager.controller;

import com.finance.manager.dto.request.LoginRequest;
import com.finance.manager.dto.request.RegisterRequest;
import com.finance.manager.dto.response.MessageResponse;
import com.finance.manager.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// handles all authentication stuff - register, login, logout
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // register a new user account
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@RequestBody @Valid RegisterRequest registerRequest) {
        MessageResponse response = authService.register(registerRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // login with username and password
    @PostMapping("/login")
    public ResponseEntity<MessageResponse> login(@RequestBody @Valid LoginRequest loginRequest,
                                                  HttpServletRequest request) {
        MessageResponse response = authService.login(loginRequest, request);
        return ResponseEntity.ok(response);
    }

    // logout the current user
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest request) {
        MessageResponse response = authService.logout(request);
        return ResponseEntity.ok(response);
    }
}
