package com.example.smartmeetingroom.repository;

import com.example.smartmeetingroom.dto.user.UserDTO;
import com.example.smartmeetingroom.entity.User;
import com.example.smartmeetingroom.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.status = :status WHERE u.id IN :ids")
    void updateUserStatus(List<Long> ids, UserStatus status);

    @Query("""
        SELECT new com.example.smartmeetingroom.dto.user.UserDTO(
            u.firstName,
            u.lastName,
            u.email,
            u.createdAt,
            u.roles.roleName,
            u.status
        )
        FROM User u

    """)
    List<UserDTO> findAllUsers();

    @Query("""
    SELECT count(u.id)
    FROM User u
    """)
    Long getTotalUsers();

    boolean existsByEmail(String email);

    Optional<User> findByIdAndRoles_Id(Long id, Byte rolesId);

    boolean existsByRoles_Id(Byte rolesId);
}
