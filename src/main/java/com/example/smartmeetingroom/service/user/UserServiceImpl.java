package com.example.smartmeetingroom.service.user;

import com.example.smartmeetingroom.dto.user.PasswordChangeDTO;
import com.example.smartmeetingroom.dto.user.UpdateUserProfileRequestDTO;
import com.example.smartmeetingroom.dto.user.UserDTO;
import com.example.smartmeetingroom.dto.user.UserResponseDTO;
import com.example.smartmeetingroom.entity.User;
import com.example.smartmeetingroom.repository.EmailVerificationRepository;
import com.example.smartmeetingroom.repository.RoleRepository;
import com.example.smartmeetingroom.repository.UserRepository;
import com.example.smartmeetingroom.util.SecurityUtil;
import com.example.smartmeetingroom.util.StringCapitalizeUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;


@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService{

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private static String userType = "EMPLOYEE";

    @Transactional
    public void createUser(UserDTO dto){
        String email = dto.getEmail().trim().toLowerCase();
        if (dto.getToken() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token is required");
        }
        String token = dto.getToken();
        var verificationObj = emailVerificationRepository.findByToken(token).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please singup by sending your email.")
        );

        if (verificationObj.getIsUsed()){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account is already created.");
        }

        if (verificationObj.getVerifiedAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email not verified.");
        }


        if (LocalDateTime.now().isAfter(verificationObj.getVerifiedValidUntil())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Verification session expired.");
        }

        if (!verificationObj.getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token does not match email.");
        }


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
        verificationObj.setIsUsed(true);
        emailVerificationRepository.save(verificationObj);
    }

    public void createUserByAdminOrSuperAdmin(UserDTO dto){
        var email = dto.getEmail().trim().toLowerCase();
        var currenUserRole = SecurityUtil.getCurrentUserRole();
        var isSuperAdmin = "SUPER_ADMIN".equals(currenUserRole);

        if (dto.getUserType() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User type is required.");
        }
        if (!isSuperAdmin) {
            userType = "EMPLOYEE";
        }else {
            userType = dto.getUserType().toUpperCase().trim();
        }
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

    @Override
    @Transactional
    public void changeEmail(User user, String email) {
        user.setEmail(email);
    }

    @Override
    @Transactional
    public void updateUserInfo(UpdateUserProfileRequestDTO dto) {
        var currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Please authenticate.");
        }
        var user = userRepository.findById(currentUserId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        );
        if (dto.getFirstName() != null && !dto.getFirstName().isBlank()) {
            String firstName = StringCapitalizeUtil.capitalizeEachWord(dto.getFirstName().trim());
            user.setFirstName(firstName);
        }
        if (dto.getLastName() != null && !dto.getLastName().isBlank()) {
            String lastName = StringCapitalizeUtil.capitalizeEachWord(dto.getLastName().trim());
            user.setLastName(lastName);
        }
        if (dto.getOldPassword() != null && !dto.getOldPassword().isBlank()){
            if (passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
                if (dto.getNewPassword() == null  || dto.getNewPassword().isBlank()){
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password is required");
                }
                var newPassword = passwordEncoder.encode(dto.getNewPassword());
                user.setPassword(newPassword);
            }else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect old password");
            }

        }
    }


    @Transactional
    public void resetPassword(PasswordChangeDTO dto) {
        var verificationObj = emailVerificationRepository.findByToken(dto.getToken()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid token")
        );
        if (verificationObj.getVerifiedAt() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Please verify the token.");
        }
        if (LocalDateTime.now().isAfter(verificationObj.getVerifiedValidUntil())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Password change session expired. Click on resend verification email.");
        }
        var user = verificationObj.getUser();
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        verificationObj.setIsUsed(true);

    }
}
