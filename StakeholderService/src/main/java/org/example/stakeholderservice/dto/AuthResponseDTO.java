package org.example.stakeholderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.stakeholderservice.model.Role;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {

    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long id;
    private String name;
    private String surname;
    private String email;
    private Role role;
    private LocalDateTime createdAt;
}
