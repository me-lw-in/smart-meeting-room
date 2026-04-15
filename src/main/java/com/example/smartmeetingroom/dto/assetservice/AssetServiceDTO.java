package com.example.smartmeetingroom.dto.assetservice;

import com.example.smartmeetingroom.enums.AssetServiceDecision;
import com.example.smartmeetingroom.enums.AssetServiceStatus;
import com.example.smartmeetingroom.enums.AssetStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class AssetServiceDTO {

    private Long id;

    private String description;

    @FutureOrPresent(message = "Date cannot be in past.")
    private LocalDate scheduledDate;

    private LocalDate completedDate;

    private AssetServiceStatus assetServiceStatus;

    private LocalDateTime createdAt;

    private String raiseBy;

    private String assetName;

    private AssetStatus assetStatus;

    @NotNull(message = "Decision is required.")
    private AssetServiceDecision decision;

    private String remark;

    public AssetServiceDTO(Long id,
                           String description,
                           LocalDate scheduledDate,
                           LocalDate completedDate,
                           AssetServiceStatus assetServiceStatus,
                           LocalDateTime createdAt,
                           String raiseBy,
                           String assetName,
                           AssetStatus assetStatus) {
        this.id = id;
        this.description = description;
        this.scheduledDate = scheduledDate;
        this.completedDate = completedDate;
        this.assetServiceStatus = assetServiceStatus;
        this.createdAt = createdAt;
        this.raiseBy = raiseBy;
        this.assetName = assetName;
        this.assetStatus = assetStatus;
    }

}
