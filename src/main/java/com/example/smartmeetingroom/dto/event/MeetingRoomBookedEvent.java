package com.example.smartmeetingroom.dto.event;

import com.example.smartmeetingroom.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MeetingRoomBookedEvent {
    private Long bookingId;

    private String roomName;

    private String bookedBy;

    private Set<Long> participantIds;

    private LocalDateTime startTime;

    private Long loggedInUserId;

    private NotificationType type;
}
