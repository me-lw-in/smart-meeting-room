package com.example.smartmeetingroom.controller;

import com.example.smartmeetingroom.dto.role.RoleDTO;
import com.example.smartmeetingroom.service.role.RoleService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@AllArgsConstructor
@RequestMapping("/api/roles")
class RoleController {

    private final RoleService roleService;

    @PostMapping()
    public ResponseEntity<Void> addRoles(@RequestBody @Valid RoleDTO dto){
        roleService.addRoles(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
