package dev.project.bedtimestory.auth.controller;

import dev.project.bedtimestory.utils.ApplicationProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CsrfController {
    private final ApplicationProperties applicationProperties;

    @GetMapping("/csrf-token")
    public CsrfToken getCsrfToken(HttpServletRequest request) {
        log.info("CSRF: get request");
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(applicationProperties.getCsrfAttribute());
        if (csrfToken == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "CSRF Token not found");
        }
        log.info("CSRF: send token");
        return csrfToken;
    }
}