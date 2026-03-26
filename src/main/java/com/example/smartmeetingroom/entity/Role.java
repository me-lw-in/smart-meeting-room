package com.example.smartmeetingroom.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @Column(name = "id", nullable = false)
    private Byte id;

    @Column(name = "role_name", nullable = false, length = 50)
    private String roleName;


}