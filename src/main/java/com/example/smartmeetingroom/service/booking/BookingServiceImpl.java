package com.example.smartmeetingroom.service.booking;

import com.example.smartmeetingroom.dto.booking.BookingDTO;
import com.example.smartmeetingroom.entity.Booking;
import com.example.smartmeetingroom.entity.MeetingRoom;
import com.example.smartmeetingroom.entity.User;
import com.example.smartmeetingroom.enums.BookingStatus;
import com.example.smartmeetingroom.enums.RoomStatus;
import com.example.smartmeetingroom.enums.UserStatus;
import com.example.smartmeetingroom.repository.BookingRepository;
import com.example.smartmeetingroom.repository.MeetingRoomRepository;
import com.example.smartmeetingroom.repository.UserRepository;
import com.example.smartmeetingroom.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.awt.print.Book;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService{

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final MeetingRoomRepository meetingRoomRepository;

    @Override
    public void bookMeetingRoom(BookingDTO dto){
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        var loggedInUserId = SecurityUtil.getCurrentUserId();
        dto.getParticipantIds().add(loggedInUserId);

        // Check timings
        validateTimings(startTime, endTime);

        // Check Room exists and availability
        var meetingRoom = checkMeetingRoomAvailability(dto.getMeetingRoomId(), dto.getParticipantIds(), startTime, endTime);

        // Check participant exits and availability
        var users = checkUsersAvailability(dto, startTime, endTime);

        var booking = new Booking();
        booking.setCreatedBy(userRepository.getReferenceById(loggedInUserId));
        booking.setRoom(meetingRoom);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setParticipants(new HashSet<>(users));
        bookingRepository.save(booking);

    }

    @Override
    @Transactional
    public void startMeetings(LocalDateTime now) {
        var bookingIds = bookingRepository
                .findIdsByStatusAndStartTimeLessThanEqual(BookingStatus.CONFIRMED, now);
        if (bookingIds.isEmpty()) return;
        bookingRepository.updateBookingStatus(bookingIds, BookingStatus.STARTED);

        List<Long> roomIds = bookingRepository.findDistinctRoomIds(bookingIds);
        List<Long> userIds = bookingRepository.findDistinctUserIds(bookingIds);

        meetingRoomRepository.updateRoomStatus(roomIds, RoomStatus.OCCUPIED);
        userRepository.updateUserStatus(userIds, UserStatus.IN_MEETING);
    }

    @Override
    @Transactional
    public void endMeetings(LocalDateTime now) {
        var bookingIds = bookingRepository
                .findIdsByStatusAndEndTimeLessThanEqual(BookingStatus.STARTED, now);
        if (bookingIds.isEmpty()) return;
        bookingRepository.updateBookingStatus(bookingIds, BookingStatus.COMPLETED);

        List<Long> roomIds = bookingRepository.findDistinctRoomIds(bookingIds);
        List<Long> userIds = bookingRepository.findDistinctUserIds(bookingIds);

        meetingRoomRepository.updateRoomStatus(roomIds, RoomStatus.AVAILABLE);
        userRepository.updateUserStatus(userIds, UserStatus.AVAILABLE);

    }

    private List<User> checkUsersAvailability(BookingDTO dto, LocalDateTime startTime, LocalDateTime endTime) {
        var users = userRepository.findAllById(dto.getParticipantIds());

        if (users.size() != dto.getParticipantIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Some participant IDs are invalid");
        }

        List<String> unavailableUsers = new ArrayList<>();
        users.forEach(u ->{
            if (u.getStatus() == UserStatus.ON_LEAVE || u.getStatus() == UserStatus.NOT_AVAILABLE){
                unavailableUsers.add(u.getFirstName() + " " + u.getLastName());
            }
        });

        validateParticipants(unavailableUsers, " is buys", " are busy");
        var conflictingUsers = bookingRepository.findConflictingParticipantNames(dto.getParticipantIds(), startTime, endTime);
        validateParticipants(conflictingUsers, " already has a meeting", " already have meetings");
        return users;
    }

    private static void validateParticipants(List<String> users,
                                             String singularMessage,
                                             String pluralMessage) {

        if (!users.isEmpty()) {
            String message = String.join(", ", users) +
                    (users.size() == 1 ? singularMessage : pluralMessage) + " on this slot";

            throw new ResponseStatusException(HttpStatus.CONFLICT, message);
        }
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

        LocalDate startDate = startTime.toLocalDate();
        LocalDate endDate = endTime.toLocalDate();

        // Booking allowed range
        LocalTime minStart = LocalTime.of(9,0);
        LocalTime maxStart = LocalTime.of(17,45);
        LocalTime minEnd = LocalTime.of(9,15);
        LocalTime maxEnd = LocalTime.of(18,0);

        // Check if start and end dates are same
        if (!startDate.equals(endDate)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Meeting should start and end on same day");
        }

        // Check Start and End Time
        if (!endTime.isAfter(startTime)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time cannot be before or equal to start time");
        }

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
