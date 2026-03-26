package com.example.smartmeetingroom.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleDTO {
    @NotBlank(message = "Role name is required")
    @Size(min = 2 , max = 45, message = "Role name should have at least 2 characters")
    private String roleName;
}
