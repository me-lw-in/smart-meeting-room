package com.example.smartmeetingroom.service.user;

import com.example.smartmeetingroom.dto.user.UserDTO;
import com.example.smartmeetingroom.dto.user.UserResponseDTO;

import java.util.List;

public interface UserService {

    public void createUser(UserDTO dto);

    public UserResponseDTO getAllUsers();

}
