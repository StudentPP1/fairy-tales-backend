package dev.project.bedtimestory.exception;

import lombok.*;

import java.util.Map;

@Getter
public class ApiException extends RuntimeException {
    private String message;
    private int status = 401;
    private Map<String, String> errors;
    public ApiException(String message) {
        this.message = message;
    }
    public ApiException(String message, int status) {
        this.message = message;
        this.status = status;
    }
}