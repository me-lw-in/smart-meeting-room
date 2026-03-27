package com.example.smartmeetingroom.dto.devicetype;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceTypeDTO {
    @NotBlank(message = "Device type name is required")
    private String deviceType;
}
