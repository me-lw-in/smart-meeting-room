package com.example.smartmeetingroom.service.user;

import com.example.smartmeetingroom.dto.user.PasswordChangeDTO;
import com.example.smartmeetingroom.dto.user.UpdateUserProfileRequestDTO;
import com.example.smartmeetingroom.dto.user.UserDTO;
import com.example.smartmeetingroom.dto.user.UserResponseDTO;
import com.example.smartmeetingroom.entity.User;

import java.time.LocalDateTime;
import java.util.List;


public interface UserService {

    void createUser(UserDTO dto);

    UserResponseDTO getAllUsers(int page, int size, String role);

    void changeEmail(User user, String email);

    void updateUserInfo(UpdateUserProfileRequestDTO dto);

    void createUserByAdminOrSuperAdmin(List<UserDTO> dtos);

    void resetPassword(PasswordChangeDTO dto);

    UserDTO getMyProfile();

    void deleteUser(Long id);

    void updateUserRole(Long targetUserId, Byte roleId);

    List<UserDTO> getAllEmployeeNames(LocalDateTime from, LocalDateTime to);
}
