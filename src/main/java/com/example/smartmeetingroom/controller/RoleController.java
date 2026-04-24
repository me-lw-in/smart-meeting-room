package com.example.smartmeetingroom.controller;

import com.example.smartmeetingroom.dto.role.RoleDTO;
import com.example.smartmeetingroom.service.role.RoleService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        var roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRoleById(@PathVariable Byte id) {
        roleService.deleteRoleById(id);
        return ResponseEntity.ok("Role is deleted");
    }
}
