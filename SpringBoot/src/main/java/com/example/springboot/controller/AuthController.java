package com.example.springboot.controller;

import com.example.springboot.dto.*;
import com.example.springboot.security.AuthContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final com.example.springboot.service.AuthService authService;

    public AuthController(com.example.springboot.service.AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public void register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
    }

    @PostMapping("/login/customer")
    public LoginResponse loginCustomer(@Valid @RequestBody LoginRequest req) {
        return authService.loginCustomer(req);
    }

    @PostMapping("/login/staff")
    public LoginResponse loginStaff(@Valid @RequestBody LoginRequest req) {
        return authService.loginStaff(req);
    }

    @GetMapping("/me")
    public Object me() {
        return authService.getProfile();
    }

    @PutMapping("/me")
    public Object updateProfile(@RequestBody UpdateProfileRequest req) {
        return authService.updateProfile(req, AuthContext.require());
    }

    @PutMapping("/me/password")
    public void changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        authService.changePassword(req.getOldPassword(), req.getNewPassword(), AuthContext.require());
    }
}
