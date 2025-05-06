package dev.project.bedtimestory.auth.oauth2;

import dev.project.bedtimestory.auth.service.AuthService;
import dev.project.bedtimestory.utils.ApplicationProperties;
import dev.project.bedtimestory.entity.User;
import dev.project.bedtimestory.entity.UserConnectedAccount;
import dev.project.bedtimestory.jwt.service.JwtService;
import dev.project.bedtimestory.repository.UserConnectedAccountRepository;
import dev.project.bedtimestory.security.AppUserDetails;
import dev.project.bedtimestory.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private final UserConnectedAccountRepository userConnectedAccountRepository;
    private final UserService userService;
    private final ApplicationProperties applicationProperties;
    private final JwtService jwtService;

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        log.info("OAuth2LoginSuccessHandler: get auth token");
        OAuth2AuthenticationToken authenticationToken = (OAuth2AuthenticationToken) authentication;
        String provider = authenticationToken.getAuthorizedClientRegistrationId();
        String providerId = authentication.getName();
        String email = authenticationToken.getPrincipal().getAttribute("email");

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Email is required to use the application");
        }

        // check if you have user based on this account
        Optional<UserConnectedAccount> connectedAccount = userConnectedAccountRepository.findByProviderAndProviderId(
                provider, providerId
        );
        if (connectedAccount.isPresent()) {
            log.info("OAuth2LoginSuccessHandler: find connected account");
            authenticateUserAndRedirect(connectedAccount.get().getUser(), request, response);
        }

        // find user by email & add connect user or create a new user
        User user;
        try {
            user = userService.getUserByEmail(email);
            log.info("OAuth2LoginSuccessHandler: create a new connected account");
            UserConnectedAccount newConnectedAccount = new UserConnectedAccount(provider, providerId);
            user.addConnectedAccount(newConnectedAccount);
            user = userService.save(user);
            userConnectedAccountRepository.save(newConnectedAccount);
        } catch (final Exception exception) {
            log.info("OAuth2LoginSuccessHandler: create a new user");
            user = createUserFromOauth2User(authenticationToken);
        }
        authenticateUserAndRedirect(user, request, response);
    }
    private User createUserFromOauth2User(OAuth2AuthenticationToken token) {
        User user = new User(token.getPrincipal());
        String provider = token.getAuthorizedClientRegistrationId();
        String providerId = token.getName();
        UserConnectedAccount connectedAccount = new UserConnectedAccount(provider, providerId);
        user.addConnectedAccount(connectedAccount);
        user = userService.save(user);
        userConnectedAccountRepository.save(connectedAccount);
        return user;
    }
    private void authenticateUserAndRedirect(
            User user,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        AuthService.authenticateUserInSecurityContext(new AppUserDetails(user));
        jwtService.setRefreshTokenToCookie(user, response);
        log.info("OAuth2LoginSuccessHandler: sent redirect");
        getRedirectStrategy().sendRedirect(
                request,
                response,
                applicationProperties.getLoginSuccessUrl()
        );
    }
}