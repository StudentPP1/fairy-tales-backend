package dev.project.bedtimestory.jwt.filters;

import dev.project.bedtimestory.auth.service.AuthService;
import dev.project.bedtimestory.auth.service.UserDetailsServiceImpl;
import dev.project.bedtimestory.jwt.service.JwtService;
import dev.project.bedtimestory.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public abstract class TokenFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userService;
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            log.info("TokenFilter: validating tokens");
            String token = this.getToken(request);
            this.validateTokenAndSaveUser(request, response, filterChain, token);
        } catch (final Exception e) {
            log.error("TokenFilter: error {}", e.getMessage());
            filterChain.doFilter(request, response);
        }
    }
    protected abstract String getToken(@NonNull HttpServletRequest request) throws ServletException;
    protected void validateTokenAndSaveUser(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain,
            String token
    ) throws IOException, ServletException {
        log.info("TokenFilter: validation token");
        if (jwtService.isTokenValid(token)) {
            String email = jwtService.extractSubject(token);
            log.info("TokenFilter: get user email from token");
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.info("TokenFilter: find user by email");
                UserDetails user = userService.loadUserByUsername(email);
                log.info("TokenFilter: register user in context");
                AuthService.authenticateUserInSecurityContext(user);
            }
        }
        filterChain.doFilter(request, response);
    }
}
