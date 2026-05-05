package com.example.smartmeetingroom.repository;

import com.example.smartmeetingroom.dto.user.UserDTO;
import com.example.smartmeetingroom.entity.User;
import com.example.smartmeetingroom.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndIsDeletedFalse(String email);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.status = :status WHERE u.id IN :ids")
    void updateUserStatus(List<Long> ids, UserStatus status);

    @Query("""
        SELECT new com.example.smartmeetingroom.dto.user.UserDTO(
            u.id,
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

    boolean existsByEmailAndIsDeletedFalse(String email);

    Optional<User> findByIdAndRoles_Id(Long id, Byte rolesId);

    boolean existsByRoles_Id(Byte rolesId);

    @Query("""
    SELECT new com.example.smartmeetingroom.dto.user.UserDTO(
        u.firstName,
        u.lastName,
        u.email
    )
    FROM User u
    WHERE u.id = :userId
    """)
    Optional<UserDTO> getMyProfile(Long userId);

    Optional<User> findByIdAndIsDeletedFalse(Long id);

    @Query("""
    SELECT new com.example.smartmeetingroom.dto.user.UserDTO(
           u.id,
           u.firstName,
           u.lastName,
           u.email,
           u.createdAt,
           u.roles.roleName,
           u.status
    )
    FROM User u
    WHERE u.isDeleted = false
      AND u.id != :currentUserId
      AND (
            (:currentUserRole = 'SUPER_ADMIN')
            OR
            (:currentUserRole = 'ADMIN' AND u.roles.roleName != 'SUPER_ADMIN')
          )
      AND (:role IS NULL OR u.roles.roleName = :role)
""")
    Page<UserDTO> findUsersWithFilters(
            @Param("currentUserId") Long currentUserId,
            @Param("currentUserRole") String currentUserRole,
            @Param("role") String role,
            Pageable pageable
    );

    @Query("""
    SELECT new com.example.smartmeetingroom.dto.user.UserDTO(
        u.id,
        CONCAT(u.firstName, ' ', u.lastName),
        u.status
    )
    FROM User u
    WHERE u.isDeleted = false
    AND u.roles.roleName = "EMPLOYEE"
""")
    List<UserDTO> findAllEmployeeNames();
}
