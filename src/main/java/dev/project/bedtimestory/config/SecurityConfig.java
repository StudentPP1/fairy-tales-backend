package dev.project.bedtimestory.config;

import dev.project.bedtimestory.auth.oauth2.OAuth2LoginSuccessHandler;
import dev.project.bedtimestory.security.UserDetailsServiceImpl;
import dev.project.bedtimestory.jwt.filters.AccessTokenFilter;
import dev.project.bedtimestory.jwt.filters.RefreshTokenFilter;
import dev.project.bedtimestory.utils.ApplicationProperties;
import dev.project.bedtimestory.utils.CookieUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final ApplicationProperties applicationProperties;
    private final UserDetailsServiceImpl userDetailsService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final AccessTokenFilter accessTokenFilter;
    private final RefreshTokenFilter refreshTokenFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(applicationProperties.getAllowedOrigins());
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);
        corsConfigurationSource.registerCorsConfiguration("/**", configuration);
        return corsConfigurationSource;
    }

    @Bean
    SecurityFilterChain endpointsFilter(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/api/csrf-token").permitAll()
                        .requestMatchers("/api/logout").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/api/story/topStories").permitAll()
                        .requestMatchers("/api/story/getStories").permitAll()
                        .requestMatchers("/api/story/search").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(auth -> {
                        auth.loginPage(applicationProperties.getFrontUrl());
                        auth.successHandler(oAuth2LoginSuccessHandler);
                        auth.redirectionEndpoint(redirectionEndpoint ->
                            redirectionEndpoint.baseUri("/oauth2/callback/*"));
                        auth.authorizationEndpoint(authorizationEndpoint ->
                            authorizationEndpoint.baseUri("/oauth2/authorize")
                        );
                })
                .addFilterAfter(accessTokenFilter, OAuth2LoginAuthenticationFilter.class)
                .addFilterAfter(refreshTokenFilter, AccessTokenFilter.class)
                .logout((logout) -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            CookieUtils.deleteCookies(request, response);
                            SecurityContextHolder.clearContext();
                        })
                )
                .build();
    }
}