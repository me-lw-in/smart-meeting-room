package com.example.smartmeetingroom.service.user;

import com.example.smartmeetingroom.dto.user.UpdateUserProfileRequestDTO;
import com.example.smartmeetingroom.dto.user.UserDTO;
import com.example.smartmeetingroom.dto.user.UserResponseDTO;
import com.example.smartmeetingroom.entity.User;

import java.util.List;

public interface UserService {

    public void createUser(UserDTO dto);

    public UserResponseDTO getAllUsers();

    public void changeEmail(User user, String email);

    public void updateUserInfo(UpdateUserProfileRequestDTO dto);
}
