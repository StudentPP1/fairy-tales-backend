package dev.project.bedtimestory.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserLoginRequest {
    @NotNull(message = "The email field field should not be null")
    @NotEmpty(message = "The email field should not be empty")
    private String email;

    @NotNull(message = "The password field field should not be null")
    @NotEmpty(message = "The password field should not be empty")
    private String password;
}