package com.example.smartmeetingroom.controller;

import com.example.smartmeetingroom.dto.user.UserDTO;
import com.example.smartmeetingroom.dto.user.UserResponseDTO;
import com.example.smartmeetingroom.service.user.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping()
    public ResponseEntity<UserResponseDTO> getAllUsers(){
        var allUsers = userService.getAllUsers();
        return ResponseEntity.status(HttpStatus.OK).body(allUsers);
    }
}
