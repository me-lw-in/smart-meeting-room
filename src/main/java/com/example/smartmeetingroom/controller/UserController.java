package com.example.smartmeetingroom.controller;

import com.example.smartmeetingroom.dto.user.UserDTO;
import com.example.smartmeetingroom.service.user.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
class UserController {

    private final UserService userService;

    @PostMapping("/admin")
    public ResponseEntity<Void> createAdmin(@RequestBody @Valid UserDTO dto){
        userService.createUser(dto);
        return ResponseEntity.ok().build();
    }
}
