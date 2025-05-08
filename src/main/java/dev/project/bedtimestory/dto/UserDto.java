package dev.project.bedtimestory.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("isSubscribed")
    private boolean isSubscribed;
}