package dev.project.bedtimestory.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Story extends BaseEntity {
    private String title;
    private byte[] img;
    private String text;
    private int likedCount;
}
