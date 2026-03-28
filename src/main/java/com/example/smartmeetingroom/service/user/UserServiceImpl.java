package com.example.smartmeetingroom.service.user;

import com.example.smartmeetingroom.dto.user.UserDTO;
import com.example.smartmeetingroom.dto.user.UserResponseDTO;
import com.example.smartmeetingroom.entity.User;
import com.example.smartmeetingroom.repository.RoleRepository;
import com.example.smartmeetingroom.repository.UserRepository;
import com.example.smartmeetingroom.util.StringCapitalizeUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService{

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private static String userType = "EMPLOYEE";

    public void createUser(UserDTO dto){
        var authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        if (authorities != null){
            var isSuperAdmin = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
            userType = isSuperAdmin ? "ADMIN" : "EMPLOYEE";
        }
        String email = dto.getEmail().trim().toLowerCase();
        if (userRepository.findByEmail(email).isPresent()){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User with " + email + " already exists!"
            );
        }
        var role = roleRepository.findByRoleName(userType.toUpperCase()).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        userType + " role not found"
                )
        );
        String firstName = StringCapitalizeUtil.capitalizeEachWord(dto.getFirstName().trim());
        String lastName = StringCapitalizeUtil.capitalizeEachWord(dto.getLastName().trim());
        String password = passwordEncoder.encode(dto.getPassword());
        var user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(password);
        user.setRoles(role);
        userRepository.save(user);
    }

    public UserResponseDTO getAllUsers() {
        var allUsers = userRepository.findAllUsers();
        var totalUsers = userRepository.getTotalUsers();
        return new UserResponseDTO(totalUsers, allUsers);
    }
}
