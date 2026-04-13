package com.example.smartmeetingroom.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "app_config")
public class AppConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 100)
    @Column(name = "config_key", nullable = false, length = 100)
    private String configKey;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config_value", nullable = false)
    private List<String> configValue;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;


}