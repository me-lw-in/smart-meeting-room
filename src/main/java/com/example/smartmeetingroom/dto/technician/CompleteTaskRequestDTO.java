package com.example.smartmeetingroom.dto.technician;

import com.example.smartmeetingroom.enums.AssetServiceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompleteTaskRequestDTO {

    @NotNull(message = "Service status is required.")
    private AssetServiceStatus serviceStatus; // RESOLVED or FAILED

    private String remarks;
}
