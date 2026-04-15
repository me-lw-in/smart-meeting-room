package com.example.smartmeetingroom.service.booking;

import com.example.smartmeetingroom.dto.booking.BookingDTO;
import com.example.smartmeetingroom.dto.booking.PatchBookingDTO;
import com.example.smartmeetingroom.entity.Booking;
import com.example.smartmeetingroom.entity.MeetingRoom;
import com.example.smartmeetingroom.entity.User;
import com.example.smartmeetingroom.enums.*;
import com.example.smartmeetingroom.repository.*;
import com.example.smartmeetingroom.service.notification.NotificationService;
import com.example.smartmeetingroom.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService{

    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final BookingRepository bookingRepository;
    private final MeetingRoomRepository meetingRoomRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void bookMeetingRoom(BookingDTO dto){
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        var loggedInUserId = SecurityUtil.getCurrentUserId();
        if (loggedInUserId == null){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        dto.getParticipantIds().add(loggedInUserId);

        // Check timings
        validateTimings(startTime, endTime);

        // Check Room exists and availability
        boolean isOnlyTimeChanged = false;
        var meetingRoom = checkMeetingRoomAvailability(dto.getMeetingRoomId(), null,isOnlyTimeChanged, dto.getParticipantIds(), startTime, endTime);

        // Check participant exits and availability
        var users = checkUsersAvailability(dto.getParticipantIds(), null, startTime, endTime);

        // Booking
        var booking = new Booking();
        booking.setCreatedBy(userRepository.getReferenceById(loggedInUserId));
        booking.setRoom(meetingRoom);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setParticipants(new HashSet<>(users));
        bookingRepository.save(booking);

        notificationService.sendMeetingCreatedNotifications(dto.getParticipantIds(), loggedInUserId, startTime, meetingRoom.getRoomName(),NotificationType.MEETING_CREATED);
    }



    @Override
    @Transactional
    public void updateBookingInfo(PatchBookingDTO dto, Long bookingId){
        // fetch the booking
        var booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found")
        );

        validateAccess(booking);

        // old data
        var oldRoomId = booking.getRoom().getId();
        var oldStartTime = booking.getStartTime();
        var oldEndTime = booking.getEndTime();
        Set<Long> oldParticipantIds = booking.getParticipants()
                .stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        // new data
        LocalDateTime newStartTime = dto.getStartTime() == null ? oldStartTime : (oldStartTime.equals(dto.getStartTime()) ? oldStartTime : dto.getStartTime());
        LocalDateTime newEndTime = dto.getEndTime() == null ? oldEndTime : (oldEndTime.equals(dto.getEndTime()) ? oldEndTime : dto.getEndTime());
        Long newRoomId = dto.getMeetingRoomId() == null ? oldRoomId : (dto.getMeetingRoomId().equals(oldRoomId) ? oldRoomId : dto.getMeetingRoomId());
        Set<Long> newParticipantIds = dto.getParticipantIds() == null || dto.getParticipantIds().isEmpty() ? oldParticipantIds : dto.getParticipantIds();
        newParticipantIds.add(booking.getCreatedBy().getId());


        // check for room change
        boolean isRoomChanged = dto.getMeetingRoomId() != null && (!dto.getMeetingRoomId().equals(oldRoomId));
        // check for time change
        boolean isTimeChanged = (dto.getStartTime() != null && !newStartTime.equals(oldStartTime)) || (dto.getEndTime() != null && !newEndTime.equals(oldEndTime));
        if (isTimeChanged) {
            validateTimings(newStartTime, newEndTime);
        }
        // check for participant changes
        boolean isParticipantsChanged = !oldParticipantIds.equals(newParticipantIds);

        MeetingRoom meetingRoom = null;
        List<User> users = null;
        if (isParticipantsChanged) {
            users = validateTimeAndUsers(dto, bookingId, isTimeChanged, newStartTime, newEndTime, true, newParticipantIds);
        }

        if (booking.getStatus() != BookingStatus.STARTED || LocalDateTime.now().isBefore(oldStartTime)){
            if (isRoomChanged){
                boolean hasOnlyTimeChanged = false;
                meetingRoom = checkMeetingRoomAvailability(newRoomId, bookingId, hasOnlyTimeChanged, newParticipantIds, newStartTime, newEndTime);
            }else if (isTimeChanged){
                boolean hasOnlyTimeChanged = true;
                meetingRoom = checkMeetingRoomAvailability(newRoomId, bookingId, hasOnlyTimeChanged, newParticipantIds, newStartTime, newEndTime);
            }
        }else {
            meetingRoom = checkMeetingRoomAvailability(newRoomId, bookingId, true, newParticipantIds, newStartTime, newEndTime);
        }

        if (meetingRoom != null){
            booking.setRoom(meetingRoom);
        }

        if (users != null) {
            booking.setParticipants(new HashSet<>(users));
        }
        booking.setStartTime(newStartTime);
        booking.setEndTime(newEndTime);

    }

    private static void validateAccess(Booking booking) {
        // logged in userId
        var loggedInUserId = SecurityUtil.getCurrentUserId();
        if (loggedInUserId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.");
        }

        // check if he is the owner of the booking
        var role = SecurityUtil.getCurrentUserRole();
        if (!loggedInUserId.equals(booking.getCreatedBy().getId()) && !"SUPER_ADMIN".equals(role) && !"ADMIN".equals(role)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to modify this booking");
        }
    }

    private List<User> validateTimeAndUsers(PatchBookingDTO dto,
                                            Long bookingId,
                                            boolean isTimeChanged,
                                            LocalDateTime newStartTime,
                                            LocalDateTime newEndTime,
                                            boolean isParticipantsChanged,
                                            Set<Long> newParticipantIds) {

        if (isTimeChanged){
            // check for pre-pone
            if (dto.getStartTime() != null) {
                if (dto.getStartTime().isBefore(LocalDateTime.now())){
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot start the meeting in the past");
                }
            }
            // validate timings
            validateTimings(newStartTime, newEndTime);
        }
        List<User> users = List.of();
        if (isParticipantsChanged){
            users = checkUsersAvailability(newParticipantIds, bookingId, newStartTime, newEndTime);
        }
        return users;
    }

    @Override
    public void startMeetings(LocalDateTime now) {
        var bookingIds = bookingRepository
                .findIdsByStatusAndStartTimeLessThanEqual(BookingStatus.CONFIRMED, now);
        if (bookingIds.isEmpty()) return;
        bookingRepository.updateBookingStatus(bookingIds, BookingStatus.STARTED);

        List<Long> roomIds = bookingRepository.findDistinctRoomIds(bookingIds);
        List<Long> userIds = bookingRepository.findDistinctUserIds(bookingIds);

        meetingRoomRepository.updateRoomStatus(roomIds, RoomStatus.OCCUPIED);
        userRepository.updateUserStatus(userIds, UserStatus.IN_MEETING);
        assetRepository.updateStatusByRoomAndCurrentStatus(roomIds, AssetStatus.AVAILABLE, AssetStatus.IN_USE);
    }

    @Override
    public void endMeetings(LocalDateTime now) {
        var bookingIds = bookingRepository
                .findIdsByStatusAndEndTimeLessThanEqual(BookingStatus.STARTED, now);
        if (bookingIds.isEmpty()) return;
        bookingRepository.updateBookingStatus(bookingIds, BookingStatus.COMPLETED);

        List<Long> roomIds = bookingRepository.findDistinctRoomIds(bookingIds);
        List<Long> userIds = bookingRepository.findDistinctUserIds(bookingIds);

        meetingRoomRepository.updateRoomStatus(roomIds, RoomStatus.AVAILABLE);
        userRepository.updateUserStatus(userIds, UserStatus.AVAILABLE);
        assetRepository.updateStatusByRoomAndCurrentStatus(roomIds, AssetStatus.IN_USE, AssetStatus.AVAILABLE);
    }

    private List<User> checkUsersAvailability(Set<Long> participantIds,Long bookingId, LocalDateTime startTime, LocalDateTime endTime) {
        var users = userRepository.findAllById(participantIds);

        if (users.size() != participantIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Some participant IDs are invalid");
        }

        List<String> unavailableUsers = new ArrayList<>();
        users.forEach(u ->{
            if (u.getStatus() == UserStatus.ON_LEAVE || u.getStatus() == UserStatus.NOT_AVAILABLE){
                unavailableUsers.add(u.getFirstName() + " " + u.getLastName());
            }
        });

        validateParticipants(unavailableUsers, " is buys", " are busy");
        var conflictingUsers = bookingRepository.findConflictingParticipantNames(participantIds,bookingId, startTime, endTime);
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
                                                     Long bookingId,
                                                     boolean hasOnlyTimeChanged,
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

        boolean isMeetingRoomBooked;
        if (hasOnlyTimeChanged){
            isMeetingRoomBooked = bookingRepository.existsOverlappingBookingAndNotById(meetingRoomId, bookingId, startTime, endTime);
        }else{
            isMeetingRoomBooked = bookingRepository.existsOverlappingBooking(meetingRoomId, startTime, endTime);
        }
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
