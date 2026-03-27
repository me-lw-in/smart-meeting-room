package com.example.smartmeetingroom.service.booking;

import com.example.smartmeetingroom.dto.booking.BookingDTO;
import com.example.smartmeetingroom.entity.MeetingRoom;
import com.example.smartmeetingroom.enums.RoomStatus;
import com.example.smartmeetingroom.repository.MeetingRoomRepository;
import com.example.smartmeetingroom.util.SecurityUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@Service
@AllArgsConstructor
public class BookingServiceImpl {

    private final MeetingRoomRepository meetingRoomRepository;

    public void bookMeetingRoom(BookingDTO dto){
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        var loggedInUserId = SecurityUtil.getCurrentUserId();
        dto.getParticipantIds().add(loggedInUserId);
        System.out.println("participant ids - ");
        dto.getParticipantIds().forEach(System.out::println);

        // Check timings
        validateTimings(startTime, endTime);

        // Check Room exists and availability
        var meetingRoom = checkMeetingRoomAvailability(dto.getMeetingRoomId(), dto.getParticipantIds(), startTime, endTime);

        // Check participant exits and availability
    }

    private MeetingRoom checkMeetingRoomAvailability(Long meetingRoomId,
                                                     Set<Long> participantIds,
                                                     LocalDateTime startTime,
                                                     LocalDateTime endTime) {
        var meetingRoom = meetingRoomRepository.findById(meetingRoomId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting room not found")
        );
        if (meetingRoom.getStatus() == RoomStatus.MAINTENANCE ){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Meeting room is under maintenance");
        }
        if (participantIds.size() > meetingRoom.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room capacity exceeded");
        }

        var isMeetingRoomBooked = meetingRoomRepository.existsOverlappingBooking(meetingRoomId, startTime, endTime);
        if (isMeetingRoomBooked) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Meeting room is already booked for the selected time slot");
        }
        return meetingRoom;
    }

    private static void validateTimings(LocalDateTime startTime, LocalDateTime endTime) {
        LocalTime start = startTime.toLocalTime();
        LocalTime end = endTime.toLocalTime();

        // Booking allowed range
        LocalTime minStart = LocalTime.of(9,0);
        LocalTime maxStart = LocalTime.of(17,45);
        LocalTime minEnd = LocalTime.of(9,15);
        LocalTime maxEnd = LocalTime.of(18,0);

        // Start time validation
        if (start.isBefore(minStart) || start.isAfter(maxStart)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time must be between 09:00 and 17:45");
        }

        // End time validation
        if (end.isBefore(minEnd) || end.isAfter(maxEnd)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be between 09:15 and 18:00");
        }
    }
}
