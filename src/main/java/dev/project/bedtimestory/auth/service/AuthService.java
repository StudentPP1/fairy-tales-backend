package dev.project.bedtimestory.auth.service;

import dev.project.bedtimestory.entity.User;
import dev.project.bedtimestory.exception.ApiException;
import dev.project.bedtimestory.jwt.service.JwtService;
import dev.project.bedtimestory.request.UserLoginRequest;
import dev.project.bedtimestory.request.UserRegisterRequest;
import dev.project.bedtimestory.response.AuthenticationResponse;
import dev.project.bedtimestory.security.AppUserDetails;
import dev.project.bedtimestory.service.UserService;
import dev.project.bedtimestory.utils.ApplicationProperties;
import dev.project.bedtimestory.utils.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final ApplicationProperties applicationProperties;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final JwtService jwtService;
    public static void authenticateUserInSecurityContext(UserDetails user) {
        log.info("AuthService: start authentication");
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                user,
                user.getPassword(),
                user.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        log.info("AuthService: end authentication");
    }
    public ResponseEntity<AuthenticationResponse> register(
            UserRegisterRequest registerRequest,
            @NonNull HttpServletResponse response
    ) {
        log.info("AuthService: start register user");
        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user = userService.save(user);
        return saveUserInContextAndSendTokens(user, response);
    }
    public ResponseEntity<AuthenticationResponse> login(
            UserLoginRequest loginRequest,
            @NonNull HttpServletResponse response
    ) {
        log.info("AuthService: start login user");
        User user = userService.getUserByEmail(loginRequest.getEmail());
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new ApiException("Wrong password", HttpStatus.BAD_REQUEST.value());
        }
        return saveUserInContextAndSendTokens(user, response);
    }
    public ResponseEntity<AuthenticationResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("AuthService: refresh tokens");
        String refreshToken = CookieUtils.getCookie(
                request,
                applicationProperties.getJwtRefreshTokenName()
        ).orElseThrow(
                () -> new ApiException("refreshToken isn't present")
        );
        AppUserDetails user = userService.getUserFromContext();
        return jwtService.validateAndSendTokens(user, refreshToken, response);
    }
    private ResponseEntity<AuthenticationResponse> saveUserInContextAndSendTokens(User user, @NonNull HttpServletResponse response) {
        authenticateUserInSecurityContext(new AppUserDetails(user));
        var jwtToken = jwtService.generateAccessToken(user);
        jwtService.setRefreshTokenToCookie(user, response);
        return ResponseEntity.ok(AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .build());
    }
}