package com.example.smartmeetingroom.dto.meetingrooms;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MeetingRoomDTO {
    @NotBlank(message = "Meeting room name is required")
    @Size(min = 3, max = 100, message = "Room name must be at least 3 characters")
    private String meetingRoomName;

    @NotNull(message = "Floor number is required")
    @Min(value = 0, message = "Floor number cannot be negative")
    @Max(value = 100, message = "Floor number seems too high")
    private Integer floorNumber;

    @NotNull(message = "Capacity is required")
    @Min(value = 2, message = "Capacity must be at least 2")
    @Max(value = 100, message = "Capacity is too large")
    private Integer capacity;
}
