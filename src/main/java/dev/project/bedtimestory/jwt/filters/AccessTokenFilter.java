package dev.project.bedtimestory.jwt.filters;

import dev.project.bedtimestory.security.UserDetailsServiceImpl;
import dev.project.bedtimestory.exception.ApiException;
import dev.project.bedtimestory.jwt.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AccessTokenFilter extends TokenFilter {
    private final static int JWT_TOKEN_SUBSTRING = 7;
    private final static String JWT_TOKEN_HEADER = "Bearer ";

    public AccessTokenFilter(JwtService jwtService, UserDetailsServiceImpl userService) {
        super(jwtService, userService);
    }
    @Override
    protected String getToken(@NonNull HttpServletRequest request) throws ServletException {
        log.info("AccessTokenFilter: invoke");
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(JWT_TOKEN_HEADER)) {
            throw new ApiException("Access token not found");
        }
        return authHeader.substring(JWT_TOKEN_SUBSTRING);
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.endsWith("/auth/refresh-token");
    }
}