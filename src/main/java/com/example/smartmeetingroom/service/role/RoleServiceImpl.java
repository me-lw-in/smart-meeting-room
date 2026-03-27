package com.example.smartmeetingroom.service.role;

import com.example.smartmeetingroom.dto.role.RoleDTO;
import com.example.smartmeetingroom.entity.Role;
import com.example.smartmeetingroom.repository.RoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

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

}
