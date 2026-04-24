package com.example.smartmeetingroom.service.role;

import com.example.smartmeetingroom.dto.role.RoleDTO;
import com.example.smartmeetingroom.entity.Role;
import com.example.smartmeetingroom.repository.RoleRepository;
import com.example.smartmeetingroom.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    public void addRoles(RoleDTO dto){
        String roleName = dto.getRoleName().trim().toUpperCase();
        if (roleRepository.existsByRoleName(roleName)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, roleName + " already exists");
        }
        var role = new Role();
        role.setRoleName(roleName.toUpperCase());
        roleRepository.save(role);
    }

    @Override
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(role -> new RoleDTO(
                        role.getId(),
                        role.getRoleName()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteRoleById(Byte roleId) {

        // 1. Check if role exists
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));

        // 2. Prevent SUPER_ADMIN deletion
        if ("SUPER_ADMIN".equalsIgnoreCase(role.getRoleName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete SUPER_ADMIN role");
        }

        // 3. Check if role is assigned to any user
        boolean isUsed = userRepository.existsByRoles_Id(roleId);

        if (isUsed) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Role is assigned to users and cannot be deleted");
        }

        // 4. Delete role
        roleRepository.delete(role);
    }

}
