package dev.project.bedtimestory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@AllArgsConstructor
@Getter
@Setter
public class StoryDetailsDto {
    private String title;
    private String description;
    private String imgUrl;
    private String text;
    private int likedCount;
    private boolean liked;
    private boolean read;
}
