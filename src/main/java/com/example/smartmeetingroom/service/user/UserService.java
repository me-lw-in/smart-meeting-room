package com.example.smartmeetingroom.service.user;

import com.example.smartmeetingroom.dto.user.PasswordChangeDTO;
import com.example.smartmeetingroom.dto.user.UpdateUserProfileRequestDTO;
import com.example.smartmeetingroom.dto.user.UserDTO;
import com.example.smartmeetingroom.dto.user.UserResponseDTO;
import com.example.smartmeetingroom.entity.User;

import java.util.List;


public interface UserService {

    public void createUser(UserDTO dto);

    public UserResponseDTO getAllUsers(int page, int size, String role);

    public void changeEmail(User user, String email);

    public void updateUserInfo(UpdateUserProfileRequestDTO dto);

    public void createUserByAdminOrSuperAdmin(UserDTO dto);

    public void resetPassword(PasswordChangeDTO dto);

    public UserDTO getMyProfile();

    public void deleteUser(Long id);

    public void updateUserRole(Long targetUserId, Byte roleId);

    public List<UserDTO> getAllEmployeeNames();
}
