package dev.project.bedtimestory.request;

import dev.project.bedtimestory.validation.Unique;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UserRegisterRequest {
    @NotNull
    private String name;

    @NotNull
    @Length(min = 8)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "must contain at least one uppercase letter, one lowercase letter, and one digit.")
    private String password;

    // check if username is already exists in database
    @NotNull
    @Unique(
            columnName = "email",
            tableName = "app_user",
            message = "User with this email already exists")
    private String email;
}
