package dev.project.bedtimestory.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStoryRequest {
    @NotNull(message = "The id field field should not be null")
    private Long id;

    @NotNull(message = "The title field field should not be null")
    @NotEmpty(message = "The title field should not be empty")
    private String title;

    @NotNull(message = "The description field field should not be null")
    @NotEmpty(message = "The description field should not be empty")
    private String description;

    @NotNull(message = "The imgUrl field field should not be null")
    @NotEmpty(message = "The imgUrl field should not be empty")
    private String imgUrl;

    @NotNull(message = "The text field field should not be null")
    @NotEmpty(message = "The text field should not be empty")
    private String text;
}