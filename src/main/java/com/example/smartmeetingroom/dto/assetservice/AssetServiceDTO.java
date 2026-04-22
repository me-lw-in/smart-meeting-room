package com.example.smartmeetingroom.dto.assetservice;

import com.example.smartmeetingroom.enums.AssetServiceDecision;
import com.example.smartmeetingroom.enums.AssetServiceStatus;
import com.example.smartmeetingroom.enums.AssetStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetServiceDTO {

    private Long id;

    private String description;

    @FutureOrPresent(message = "Date cannot be in past.")
    private LocalDate scheduledDate;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private AssetServiceStatus assetServiceStatus;

    private LocalDateTime createdAt;

    private String raisedBy;

    private String assetName;

    private AssetStatus assetStatus;

    @NotNull(message = "Decision is required.")
    private AssetServiceDecision decision;

    private String remark;

    private Long technicianId;

    private String technicianName;

    public AssetServiceDTO(Long id,
                           String description,
                           LocalDate scheduledDate,
                           LocalDateTime startedAt,
                           LocalDateTime completedAt,
                           AssetServiceStatus assetServiceStatus,
                           LocalDateTime createdAt,
                           String raisedBy,
                           String technicianName,
                           String assetName,
                           AssetStatus assetStatus) {
        this.id = id;
        this.description = description;
        this.scheduledDate = scheduledDate;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.assetServiceStatus = assetServiceStatus;
        this.createdAt = createdAt;
        this.raisedBy = raisedBy;
        this.technicianName = technicianName;
        this.assetName = assetName;
        this.assetStatus = assetStatus;
    }

}
