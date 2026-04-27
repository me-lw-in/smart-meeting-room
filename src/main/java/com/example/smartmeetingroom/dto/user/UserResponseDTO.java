package com.example.smartmeetingroom.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class UserResponseDTO {
    private Long totalUsers;

    private int totalPages;

    private List<UserDTO> allUsers;
}
