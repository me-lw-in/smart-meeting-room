package com.example.smartmeetingroom.dto.user;

import com.example.smartmeetingroom.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@RequiredArgsConstructor
public class UserDTO {
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;

    private String token;

    private String userType;

    private LocalDateTime createdAt;

    private String roleName;

    private UserStatus status;

    private Long totalUsers;

    private Long id;

    private String fullName;

    public UserDTO(Long id, String firstName, String lastName, String email, LocalDateTime createdAt, String roleName, UserStatus status){
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.createdAt = createdAt;
        this.roleName = roleName;
        this.status = status;
    }

    public UserDTO(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public UserDTO(Long id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UserDTO(Long id, String fullName, UserStatus status) {
        this.id = id;
        this.fullName = fullName;
        this.status = status;
    }


}
