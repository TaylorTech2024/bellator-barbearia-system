package com.taylortech.bellator.controller;

import com.taylortech.bellator.dto.AuthLoginRequest;
import com.taylortech.bellator.dto.AuthRegisterRequest;
import com.taylortech.bellator.dto.AuthResponse;
import com.taylortech.bellator.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody AuthRegisterRequest req) {
        return service.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthLoginRequest req) {
        return service.login(req);
    }
}
