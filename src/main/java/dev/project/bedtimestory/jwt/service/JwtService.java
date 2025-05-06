package dev.project.bedtimestory.jwt.service;

import dev.project.bedtimestory.entity.AppUser;
import dev.project.bedtimestory.utils.ApplicationProperties;
import dev.project.bedtimestory.response.AuthenticationResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.file.AccessDeniedException;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {
    private final ApplicationProperties applicationProperties;
    private final static int TO_MILLIS = 1000;
    public String generateAccessToken(AppUser user) {
        return buildToken(user, applicationProperties.getJwtAccessTokenExpiration());
    }
    public void setRefreshTokenToCookie(AppUser user, HttpServletResponse response) {
        var refreshToken = this.generateRefreshToken(user);
        Cookie refreshTokenCookie = new Cookie(
                applicationProperties.getJwtRefreshTokenName(),
                refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(applicationProperties.getJwtRefreshTokenExpiration());
        refreshTokenCookie.setAttribute("SameSite", "Strict");
        log.info("jwtService: set new refresh token to cookie");
        response.addCookie(refreshTokenCookie);
    }
    public ResponseEntity<AuthenticationResponse> validateAndSendTokens(AppUser user, String token, HttpServletResponse response) throws AccessDeniedException {
        this.isTokenValid(token);
        String newAccessToken = this.generateAccessToken(user);
        this.setRefreshTokenToCookie(user, response);
        log.info("jwtService: send new tokens");
        return ResponseEntity.ok(AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .build());
    }
    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    public boolean isTokenValid(String token) throws AccessDeniedException {
        if (isTokenExpired(token)) throw new AccessDeniedException("Token expired");
        return true;
    }
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }
    private Claims getAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    private String generateRefreshToken(AppUser user) {
        return buildToken(user, applicationProperties.getJwtRefreshTokenExpiration());
    }
    private String buildToken(AppUser user, long expiration) {
        return Jwts.builder()
                .expiration(new Date(System.currentTimeMillis() + expiration * TO_MILLIS))
                .issuedAt(new Date())
                .subject(user.getUsername())
                .signWith(getSignInKey())
                .compact();
    }
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(applicationProperties.getJwtSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
