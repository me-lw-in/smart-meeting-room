package com.example.smartmeetingroom.entity;

import com.example.smartmeetingroom.enums.AssetStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "assets")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @Column(name = "asset_name", nullable = false)
    private String assetName;

    @Size(max = 255)
    @Column(name = "serial_number", nullable = false)
    private String serialNumber;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "warranty_expiry", nullable = false)
    private LocalDate warrantyExpiry;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "room_id", nullable = false)
    private MeetingRoom room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "asset_type_id", nullable = false)
    private AssetType assetType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AssetStatus status;

    @ColumnDefault("0")
    @Column(name = "is_deleted", insertable = false)
    private Boolean isDeleted;


}