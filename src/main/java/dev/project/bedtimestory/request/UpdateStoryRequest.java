package dev.project.bedtimestory.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStoryRequest {
    @NotNull
    @NotBlank
    private Long id;
    @NotNull
    @NotBlank
    private String title;
    @NotNull
    @NotBlank
    private String description;
    @NotNull
    @NotBlank
    private String imgUrl;
    @NotNull
    @NotBlank
    private String text;
}