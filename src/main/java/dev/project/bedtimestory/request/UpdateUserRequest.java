package dev.project.bedtimestory.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @NotNull
    @NotBlank
    private String name;
    private String img;
}
