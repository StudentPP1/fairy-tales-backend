package dev.project.bedtimestory.exception;

import dev.project.bedtimestory.response.HttpErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.*;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        Map<String, String> errors = new HashMap<>(); // fields validation errors
        List<String> generalErrors = new ArrayList<>(); // objects errors
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            if (error instanceof FieldError fieldError) {
                String fieldName = fieldError.getField();
                String errorMessage = fieldError.getDefaultMessage();
                errors.put(fieldName, errorMessage);
                logger.info("fieldName: {}; error: {}", fieldName, errorMessage);
            } else {
                generalErrors.add(error.getDefaultMessage());
                logger.info("generalErrors: {}", Arrays.toString(generalErrors.toArray()));
            }
        });
        HttpErrorResponse response = HttpErrorResponse.of(
                "Unprocessable entity",
                422,
                errors,
                generalErrors);
        return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<HttpErrorResponse> handleException(final ApiException exception) {
        logger.error("Handing BadCredentialsException: {}", exception.getMessage());
        var response = HttpErrorResponse.of("Unexpected error", exception.getStatus());
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(exception.getStatus()));
    }

    // handle other errors as server unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpErrorResponse> handleException(final Exception exception) {
        logger.error("Unhandled exception: {}", exception.getMessage());
        var response = HttpErrorResponse.of("Unexpected error", 500);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}