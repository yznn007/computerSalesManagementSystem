package com.example.springboot.controller;

import com.example.springboot.dto.*;
import com.example.springboot.security.AuthContext;
import com.example.springboot.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 认证与账户接口（/api/auth）。
 * register 与两个 login 为公开入口；/me 系列需登录后凭 token 访问。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** 客户自助注册（公开） */
    @PostMapping("/register")
    public void register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
    }

    /** 客户登录（公开），返回 token 与用户信息 */
    @PostMapping("/login/customer")
    public LoginResponse loginCustomer(@Valid @RequestBody LoginRequest req) {
        return authService.loginCustomer(req);
    }

    /** 销售员登录（公开），返回 token 与用户信息 */
    @PostMapping("/login/staff")
    public LoginResponse loginStaff(@Valid @RequestBody LoginRequest req) {
        return authService.loginStaff(req);
    }

    /** 查看当前登录账户信息 */
    @GetMapping("/me")
    public Object me() {
        return authService.getProfile();
    }

    /** 修改当前账户基本资料 */
    @PutMapping("/me")
    public Object updateProfile(@RequestBody UpdateProfileRequest req) {
        return authService.updateProfile(req, AuthContext.require());
    }

    /** 修改当前账户密码 */
    @PutMapping("/me/password")
    public void changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        authService.changePassword(req.oldPassword(), req.newPassword(), AuthContext.require());
    }
}
