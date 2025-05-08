package dev.project.bedtimestory.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @NotNull(message = "The img field field should not be null")
    @NotEmpty(message = "The img field should not be empty")
    private String name;

    private String img;

    @NotNull(message = "The isSubscribed field field should not be null")
    private Boolean isSubscribed;
}