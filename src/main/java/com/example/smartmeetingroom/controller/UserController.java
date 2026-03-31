package com.example.smartmeetingroom.controller;

import com.example.smartmeetingroom.dto.user.EmailDTO;
import com.example.smartmeetingroom.dto.user.UpdateUserProfileRequestDTO;
import com.example.smartmeetingroom.dto.user.UserDTO;
import com.example.smartmeetingroom.dto.user.UserResponseDTO;
import com.example.smartmeetingroom.service.user.UserService;
import com.example.smartmeetingroom.service.verification.EmailVerificationService;
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

    @GetMapping()
    public ResponseEntity<UserResponseDTO> getAllUsers(){
        var allUsers = userService.getAllUsers();
        return ResponseEntity.status(HttpStatus.OK).body(allUsers);
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

    @PatchMapping()
    public ResponseEntity<Void> updateUser(@RequestBody UpdateUserProfileRequestDTO dto) {
        userService.updateUserInfo(dto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
