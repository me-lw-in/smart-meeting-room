package com.example.smartmeetingroom.dto.technician;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAssetServiceDTO {

    @NotNull(message = "Service status is required.")
    private String serviceStatus;

    private String remarks;
}
