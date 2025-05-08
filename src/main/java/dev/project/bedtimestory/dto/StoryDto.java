package dev.project.bedtimestory.dto;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class StoryDto implements Serializable {
    private Long id;
    private String title;
    private String description;
    private String imgUrl;
    private int likedCount;
}
