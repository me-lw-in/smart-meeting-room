package com.example.smartmeetingroom.dto.booking;

import com.example.smartmeetingroom.dto.user.UserDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    private Long bookingId;

    private List<UserDTO> participants;

    private String roomName;

    public BookingDTO(Long bookingId, String roomName, LocalDateTime startTime, LocalDateTime endTime){
        this.bookingId = bookingId;
        this.roomName = roomName;
        this.startTime = startTime;
        this.endTime = endTime;

    }
}
