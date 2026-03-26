package com.example.smartmeetingroom.service.user;

import com.example.smartmeetingroom.dto.user.UserDTO;
import com.example.smartmeetingroom.entity.User;
import com.example.smartmeetingroom.repository.RoleRepository;
import com.example.smartmeetingroom.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService{

    private final static String userEmp = "EMPLOYEE";

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;


    public void createUser(UserDTO dto){
        String email = dto.getEmail().trim().toLowerCase();
        if (userRepository.findByEmail(email).isPresent()){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User with " + email + " already exists!"
            );
        }
        var role = roleRepository.findByRoleName(userEmp).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        userEmp + " role not found"
                )
        );
        String firstName = dto.getFirstName().trim();
        String lastName = dto.getLastName().trim();
        String password = passwordEncoder.encode(dto.getPassword());
        var user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(password);
        userRepository.save(user);
    }
}
