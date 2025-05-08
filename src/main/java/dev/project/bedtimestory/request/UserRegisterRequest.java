package dev.project.bedtimestory.request;

import dev.project.bedtimestory.validation.Unique;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UserRegisterRequest {
    @NotNull(message = "The name field field should not be null")
    @NotEmpty(message = "The name field should not be empty")
    private String name;

    @NotNull(message = "The password field field should not be null")
    @NotEmpty(message = "The password field should not be empty")
    @Length(min = 8)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "must contain at least one uppercase letter, one lowercase letter, and one digit.")
    private String password;

    // check if username is already exists in database
    @NotNull(message = "The email field field should not be null")
    @NotEmpty(message = "The email field should not be empty")
    @Unique(
            columnName = "email",
            tableName = "app_user",
            message = "User with this email already exists")
    private String email;
}
