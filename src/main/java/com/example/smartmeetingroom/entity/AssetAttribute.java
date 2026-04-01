package com.example.smartmeetingroom.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "asset_attributes")
public class AssetAttribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @Column(name = "attribute_name", nullable = false)
    private String attributeName;

    @Size(max = 255)
    @Column(name = "attribute_value", nullable = false)
    private String attributeValue;

    @Size(max = 50)
    @Column(name = "unit", length = 50)
    private String unit;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "recorded_at", nullable = false, insertable = false)
    private LocalDateTime recordedAt;


}