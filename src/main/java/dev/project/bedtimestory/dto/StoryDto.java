package dev.project.bedtimestory.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class StoryDto {
    private String id;
    private String title;
    private String description;
    private String imgUrl;
    private int likedCount;
}
