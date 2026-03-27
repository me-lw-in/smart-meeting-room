package com.example.smartmeetingroom.dto.booking;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
public class BookingDTO {
    @NotNull(message = "Meeting room id is required")
    private Long meetingRoomId;

    @NotEmpty(message = "Participants cannot be empty")
    private Set<@NotNull(message = "Participant ID cannot be null") Long> participantIds;

    @NotNull(message = "Start time is required")
    @FutureOrPresent(message = "Start time must be present or future")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    private LocalDateTime endTime;
}
