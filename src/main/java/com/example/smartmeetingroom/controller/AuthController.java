package com.example.smartmeetingroom.controller;

import com.example.smartmeetingroom.dto.auth.LoginDTO;
import com.example.smartmeetingroom.dto.user.EmailDTO;
import com.example.smartmeetingroom.dto.user.PasswordChangeDTO;
import com.example.smartmeetingroom.dto.user.UserDTO;
import com.example.smartmeetingroom.service.jwt.JwtService;
import com.example.smartmeetingroom.service.user.UserService;
import com.example.smartmeetingroom.service.verification.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final EmailVerificationService emailVerificationService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<Void> createAccount(@RequestBody @Valid UserDTO dto){
        log.info("Create account request received for email: {}", dto.getEmail());
        userService.createUser(dto);
        log.info("Account created successfully for email: {}", dto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String,String>> loginUser(@RequestBody @Valid LoginDTO dto){
        String email = dto.getEmail().trim().toLowerCase();
        log.info("Login attempt for email: {}", email);
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                email,
                dto.getPassword()
        ));
        log.info("User authenticated successfully: {}", email);
        var token = jwtService.generateToken(email);
        log.info("JWT token generated for user: {}", email);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("accessToken", token));
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyToken(@RequestParam String token) {
        log.info("Email verification attempt with token");
        var url = emailVerificationService.verifyToken(token);
        log.info("Email verified successfully");
        return ResponseEntity.status(HttpStatus.OK).body(url);
    }

    @PostMapping("/singup/email")
    public ResponseEntity<String> singupEmail(@RequestBody @Valid EmailDTO dto) {
        log.info("Signup email request for: {}", dto.getEmail());
        emailVerificationService.singUpUser(dto);
        return ResponseEntity.status(HttpStatus.OK).body("Verification link sent successfully");
    }

    @PostMapping("/complete-singup")
    public ResponseEntity<Void> completeSingup(@RequestBody @Valid UserDTO dto) {
        log.info("Completing signup for email: {}", dto.getEmail());
        userService.createUser(dto);
        log.info("User signup completed successfully: {}", dto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<String> requestPasswordReset(@RequestBody @Valid EmailDTO dto) {
        log.info("Password reset requested for email: {}", dto.getEmail());
        emailVerificationService.requestPasswordReset(dto);
        return ResponseEntity.status(HttpStatus.OK).body("Verification link sent successfully");
    }

    @PostMapping("/password/reset")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid PasswordChangeDTO dto){
        log.info("Password reset attempt using token");
        userService.resetPassword(dto);
        log.info("Password reset successful");
        return ResponseEntity.status(HttpStatus.OK).body("Password changed successfully");
    }

}
