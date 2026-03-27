package com.example.smartmeetingroom.dto.device;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceDTO {
    @NotBlank(message = "Device name is required")
    @Size(min = 3, max = 100, message = "Device name must be at least 3 characters")
    private String deviceName;

    @NotNull(message = "Meeting room is required")
    @Positive(message = "Meeting room ID must be a positive number")
    private Long meetingRoomId;

    @NotNull(message = "Device type is required")
    @Positive(message = "Device type ID must be a positive number")
    private Short deviceTypeId;


}
