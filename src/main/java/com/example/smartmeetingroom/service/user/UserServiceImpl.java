package com.example.smartmeetingroom.service.user;

import com.example.smartmeetingroom.dto.user.PasswordChangeDTO;
import com.example.smartmeetingroom.dto.user.UpdateUserProfileRequestDTO;
import com.example.smartmeetingroom.dto.user.UserDTO;
import com.example.smartmeetingroom.dto.user.UserResponseDTO;
import com.example.smartmeetingroom.entity.User;
import com.example.smartmeetingroom.enums.UserStatus;
import com.example.smartmeetingroom.repository.EmailVerificationRepository;
import com.example.smartmeetingroom.repository.RoleRepository;
import com.example.smartmeetingroom.repository.UserRepository;
import com.example.smartmeetingroom.util.SecurityUtil;
import com.example.smartmeetingroom.util.StringCapitalizeUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;


@Slf4j
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

        var user = userRepository.findByEmail(email);
        if (user.isPresent() && user.get().getIsDeleted() == false) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already taken.");
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

        if (user.isPresent() && user.get().getIsDeleted().equals(true)) {
            user.get().setFirstName(firstName);
            user.get().setLastName(lastName);
            user.get().setEmail(email);
            user.get().setPassword(password);
            user.get().setRoles(role);
            user.get().setStatus(UserStatus.AVAILABLE);
            user.get().setIsDeleted(false);
        } else {
            var newUser = new User();
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setEmail(email);
            newUser.setPassword(password);
            newUser.setRoles(role);
            userRepository.save(newUser);
        }

        verificationObj.setIsUsed(true);
        emailVerificationRepository.save(verificationObj);
        log.info("User created successfully with role: {}", userType);
    }

    @Transactional
    public void createUserByAdminOrSuperAdmin(UserDTO dto){
        var email = dto.getEmail().trim().toLowerCase();
        var currenUserRole = SecurityUtil.getCurrentUserRole();
        var isSuperAdmin = "SUPER_ADMIN".equals(currenUserRole);

        if (dto.getUserType() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User type is required.");
        }
        if (!isSuperAdmin && dto.getUserType().equalsIgnoreCase("SUPER_ADMIN")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only Super admin can create super admins.");
        }else {
            userType = dto.getUserType().toUpperCase().trim();
        }

        var user = userRepository.findByEmail(email);
        if (user.isPresent() && user.get().getIsDeleted() == false) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already taken.");
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

        if (user.isPresent() && user.get().getIsDeleted().equals(true)) {
            user.get().setFirstName(firstName);
            user.get().setLastName(lastName);
            user.get().setEmail(email);
            user.get().setPassword(password);
            user.get().setRoles(role);
            user.get().setStatus(UserStatus.AVAILABLE);
            user.get().setIsDeleted(false);
        } else {
            var newUser = new User();
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setEmail(email);
            newUser.setPassword(password);
            newUser.setRoles(role);
            userRepository.save(newUser);
        }

        log.info("User created successfully by {} with role {}", currenUserRole, userType);
    }

    public UserResponseDTO getAllUsers(int page, int size, String role) {

        var currentUserId = SecurityUtil.getCurrentUserId();
        var currentUserRole = SecurityUtil.getCurrentUserRole();

        if ("ADMIN".equalsIgnoreCase(currentUserRole) && "SUPER_ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.");
        }

        Pageable pageable = PageRequest.of(page, size);
        var usersPage = userRepository.findUsersWithFilters(
                currentUserId,
                currentUserRole,
                role,
                pageable
        );

        var users = usersPage.getContent();
        return new UserResponseDTO(usersPage.getTotalElements(),usersPage.getTotalPages(), users);
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
        log.info("User profile update request for userId: {}", currentUserId);
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
            log.info("Password change requested for userId: {}", currentUserId);
            if (passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
                if (dto.getNewPassword() == null  || dto.getNewPassword().isBlank()){
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password is required");
                }
                var newPassword = passwordEncoder.encode(dto.getNewPassword());
                user.setPassword(newPassword);
            }else {
                log.warn("Incorrect old password for userId: {}", currentUserId);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect old password");
            }

        }
    }


    @Transactional
    public void resetPassword(PasswordChangeDTO dto) {
        log.info("Password reset process started");
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
        log.info("Password reset successful");
    }

    public UserDTO getMyProfile() {
        var loggedInUserId = SecurityUtil.getCurrentUserId();
        return userRepository.getMyProfile(loggedInUserId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.")
        );
    }

    @Transactional
    public void deleteUser(Long id) {
        var user = userRepository.findByIdAndIsDeletedFalse(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.")
        );
        user.setIsDeleted(true);
    }

    public void updateUserRole(Long targetUserId, Byte roleId) {

        var currentUserId = SecurityUtil.getCurrentUserId();
        var currentUserRole = SecurityUtil.getCurrentUserRole();

        if (currentUserId.equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot change your own role");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        var newRole = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));

        if (currentUserRole.equals("ADMIN") && targetUser.getRoles().getRoleName().equals("SUPER_ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin cannot modify superadmin");
        }

        if (currentUserRole.equals("SUPER_ADMIN") && targetUser.getRoles().getRoleName().equals("SUPER_ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot modify another superadmin");
        }

        if (currentUserRole.equals("ADMIN")) {
            if (newRole.getRoleName().equals("ADMIN") ||
                    newRole.getRoleName().equals("SUPER_ADMIN")) {

                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin cannot assign this role");
            }
        }

        if (currentUserRole.equals("SUPER_ADMIN")) {
            if (newRole.getRoleName().equals("SUPER_ADMIN")) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot assign this role");
            }
        }

        targetUser.setRoles(newRole);
        userRepository.save(targetUser);
        log.info("User id - {} changed the role of user id- {} to -{}", currentUserId, targetUserId, newRole.getRoleName());
    }
}
