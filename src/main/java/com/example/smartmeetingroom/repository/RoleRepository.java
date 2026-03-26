package com.example.smartmeetingroom.repository;

import com.example.smartmeetingroom.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Byte> {
    boolean existsByRoleName(String roleName);

    Optional<Role> findByRoleName(String roleName);
}
