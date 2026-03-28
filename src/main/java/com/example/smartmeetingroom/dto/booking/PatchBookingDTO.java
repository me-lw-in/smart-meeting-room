package com.example.smartmeetingroom.dto.booking;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
public class PatchBookingDTO {

    private Long meetingRoomId;

    private Set<Long> participantIds;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
