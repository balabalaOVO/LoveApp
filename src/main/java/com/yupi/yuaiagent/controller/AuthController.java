package com.yupi.yuaiagent.controller;

import com.yupi.yuaiagent.Service.LoginRequest;
import com.yupi.yuaiagent.Service.LoginResponse;
import com.yupi.yuaiagent.Service.RegisterRequest;
import com.yupi.yuaiagent.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) throws Exception {
        userService.register(request);
        return ResponseEntity.ok().body("注册成功");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) throws Exception {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }
}