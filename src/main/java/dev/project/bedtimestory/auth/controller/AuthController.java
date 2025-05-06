package dev.project.bedtimestory.auth.controller;

import dev.project.bedtimestory.auth.service.AuthService;
import dev.project.bedtimestory.request.UserLoginRequest;
import dev.project.bedtimestory.request.UserRegisterRequest;
import dev.project.bedtimestory.response.AuthenticationResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;
    @PostMapping("/auth/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody UserRegisterRequest userRegisterRequest,
            @NonNull HttpServletResponse response) {
        return service.register(userRegisterRequest, response);
    }

    @PostMapping("/auth/login")
    public  ResponseEntity<AuthenticationResponse> login(
            @NonNull HttpServletResponse response,
            @RequestBody UserLoginRequest userLoginRequest) {
        return service.login(userLoginRequest, response);
    }

    @GetMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            @NonNull HttpServletResponse response,
            @NonNull HttpServletRequest request) throws Exception {
        return service.refreshToken(request, response);
    }
}