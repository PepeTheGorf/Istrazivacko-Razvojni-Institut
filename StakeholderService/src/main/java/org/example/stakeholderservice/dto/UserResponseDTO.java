package org.example.stakeholderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.stakeholderservice.model.Role;
import org.example.stakeholderservice.model.User;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    private Long id;
    private String name;
    private String surname;
    private String email;
    private Role role;
    private String uuid;

    public static UserResponseDTO fromEntity(User user) {
        String userUuid = UUID.nameUUIDFromBytes(
                ("stakeholder:" + user.getId()).getBytes(StandardCharsets.UTF_8)
        ).toString();
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .role(user.getRole())
                .uuid(userUuid)
                .build();
    }
}
