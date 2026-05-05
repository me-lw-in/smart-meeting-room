package com.example.smartmeetingroom.controller;

import com.example.smartmeetingroom.dto.role.UpdateUserRoleRequest;
import com.example.smartmeetingroom.dto.user.EmailDTO;
import com.example.smartmeetingroom.dto.user.UpdateUserProfileRequestDTO;
import com.example.smartmeetingroom.dto.user.UserDTO;
import com.example.smartmeetingroom.dto.user.UserResponseDTO;
import com.example.smartmeetingroom.service.user.UserService;
import com.example.smartmeetingroom.service.verification.EmailVerificationService;
import com.example.smartmeetingroom.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
class UserController {

    private final UserService userService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping()
    public ResponseEntity<Void> createUsersByAdminOrSuperAdmin(@RequestBody @Valid UserDTO dto){
        userService.createUserByAdminOrSuperAdmin(dto);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/me/email-change")
    public ResponseEntity<String> changeEmail(@RequestBody @Valid EmailDTO dto){
        emailVerificationService.changeEmail(dto);
        return ResponseEntity.ok().body("Verification link sent to your email");
    }

    @PostMapping("/me/email/resend-verification")
    public ResponseEntity<String> resendEmail(@RequestBody @Valid EmailDTO dto) {
        emailVerificationService.resendEmailForVerification(dto);
        return ResponseEntity.status(HttpStatus.OK).body("Email sent again!");
    }

    @GetMapping("/employees/names")
    public ResponseEntity<?> getEmployeeNames() {
        return ResponseEntity.ok(userService.getAllEmployeeNames());
    }

    @GetMapping()
    public ResponseEntity<UserResponseDTO> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String role
    ){
        var allUsers = userService.getAllUsers(page,size,role);
        return ResponseEntity.status(HttpStatus.OK).body(allUsers);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @PatchMapping()
    public ResponseEntity<Void> updateUser(@RequestBody UpdateUserProfileRequestDTO dto) {
        userService.updateUserInfo(dto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<Void> updateUserRole(
            @PathVariable Long userId,
            @RequestBody UpdateUserRoleRequest request
    ) {
        userService.updateUserRole(userId, request.getRoleId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("Account deleted successfully");
    }

    @DeleteMapping("/me")
    public ResponseEntity<String> deleteLoggedInUser() {
        var id = SecurityUtil.getCurrentUserId();
        userService.deleteUser(id);
        return ResponseEntity.ok("Account deleted successfully");
    }

}
