package dev.project.bedtimestory.dto;

import dev.project.bedtimestory.entity.Role;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserDto {
    private String name;
    private String img;
    private String email;
    private Role role;
}