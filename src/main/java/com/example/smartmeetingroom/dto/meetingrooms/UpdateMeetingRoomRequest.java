package com.example.smartmeetingroom.dto.meetingrooms;

import com.example.smartmeetingroom.enums.RoomStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMeetingRoomRequest {

    @Min(value = 0, message = "Floor number cannot be negative")
    @Max(value = 100, message = "Floor number seems too high")
    private Integer floor;

    @Size(min = 3, max = 100, message = "Room name must be at least 3 characters")
    private String meetingRoomName;

    @Min(value = 2, message = "Capacity must be at least 2")
    @Max(value = 100, message = "Capacity is too large")
    private Integer capacity;

    private RoomStatus status;
}
