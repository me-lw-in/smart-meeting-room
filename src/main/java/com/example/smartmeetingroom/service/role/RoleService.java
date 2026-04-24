package com.example.smartmeetingroom.service.role;

import com.example.smartmeetingroom.dto.role.RoleDTO;

import java.util.List;

public interface RoleService {
    public void addRoles(RoleDTO dto);

    public List<RoleDTO> getAllRoles();

    public void deleteRoleById(Byte roleId);
}
