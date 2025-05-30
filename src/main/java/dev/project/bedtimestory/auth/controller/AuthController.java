package dev.project.bedtimestory.auth.controller;

import dev.project.bedtimestory.auth.service.AuthService;
import dev.project.bedtimestory.request.UserLoginRequest;
import dev.project.bedtimestory.request.UserRegisterRequest;
import dev.project.bedtimestory.response.AuthenticationResponse;
import dev.project.bedtimestory.response.InformResponse;
import dev.project.bedtimestory.utils.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;
    @PostMapping("/auth/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody @Valid UserRegisterRequest userRegisterRequest,
            @NonNull HttpServletResponse response) {
        log.info("AuthController: call service to register user");
        return service.register(userRegisterRequest, response);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthenticationResponse> login(
            @NonNull HttpServletResponse response,
            @RequestBody @Valid UserLoginRequest userLoginRequest) {
        log.info("AuthController: call service to login user");
        return service.login(userLoginRequest, response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            @NonNull HttpServletResponse response,
            @NonNull HttpServletRequest request) throws Exception {
        log.info("AuthController: call service to refresh token");
        return service.refreshToken(request, response);
    }

    @PostMapping("/logout")
    public ResponseEntity<InformResponse> logout(
            @NonNull HttpServletResponse response,
            @NonNull HttpServletRequest request) {
        log.info("AuthController: logout user");
        CookieUtils.deleteCookies(request, response);
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new InformResponse("logout successfully"));
    }
}