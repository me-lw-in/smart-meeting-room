package com.example.smartmeetingroom.repository;

import com.example.smartmeetingroom.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

interface UserRepository extends JpaRepository<User, Long> {
}
