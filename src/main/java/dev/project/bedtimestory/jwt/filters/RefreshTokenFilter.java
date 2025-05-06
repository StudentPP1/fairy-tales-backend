package dev.project.bedtimestory.jwt.filters;

import dev.project.bedtimestory.security.UserDetailsServiceImpl;
import dev.project.bedtimestory.utils.ApplicationProperties;
import dev.project.bedtimestory.exception.ApiException;
import dev.project.bedtimestory.jwt.service.JwtService;
import dev.project.bedtimestory.utils.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class RefreshTokenFilter extends TokenFilter {
    private final ApplicationProperties applicationProperties;

    public RefreshTokenFilter(JwtService jwtService, UserDetailsServiceImpl userService, ApplicationProperties applicationProperties) {
        super(jwtService, userService);
        this.applicationProperties = applicationProperties;
    }
    @Override
    protected String getToken(@NonNull HttpServletRequest request) {
        log.info("RefreshTokenFilter: invoke");
        Optional<String> refreshTokenCookie = CookieUtils.getCookie(
                request,
                applicationProperties.getJwtRefreshTokenName()
        );
        return refreshTokenCookie.orElseThrow(() -> new ApiException("refreshToken isn't present"));
    }
}