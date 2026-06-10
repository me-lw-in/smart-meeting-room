package com.example.smartmeetingroom.service.roomoccupancy;

import com.example.smartmeetingroom.config.PasswordEncoderConfig;
import com.example.smartmeetingroom.dto.audit.AuditEventDTO;
import com.example.smartmeetingroom.dto.auth.LoginDTO;
import com.example.smartmeetingroom.enums.BookingStatus;
import com.example.smartmeetingroom.enums.UserStatus;
import com.example.smartmeetingroom.repository.BookingRepository;
import com.example.smartmeetingroom.repository.MeetingRoomRepository;
import com.example.smartmeetingroom.repository.RoomOccupancyRepository;
import com.example.smartmeetingroom.repository.UserRepository;
import com.example.smartmeetingroom.service.producer.ProducerService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class RoomOccupancyImpl implements RoomOccupancy{

    private final UserRepository userRepository;
    private final ProducerService auditEventProducer;
    private final BookingRepository bookingRepository;
    private final PasswordEncoderConfig passwordEncoder;
    private final MeetingRoomRepository meetingRoomRepository;
    private final RoomOccupancyRepository roomOccupancyRepository;

    @Override
    @Transactional
    public void checkIn(LoginDTO dto, Long roomId) {
        var email  = dto.getEmail().toLowerCase();
        var password  = dto.getPassword();

        var user = userRepository.findByEmail(email).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.")
        );

        if (user.getStatus().equals(UserStatus.IN_MEETING)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You are already checked-in.");
        }
        if (!passwordEncoder.passwordEncoder().matches(password, user.getPassword())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Password is incorrect.");
        }

        var currentTime = LocalDateTime.now();
        var booking = bookingRepository.findBookingIdByRoomIdAndCurrentTime(roomId, currentTime, BookingStatus.STARTED).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "At current time no meeting is scheduled for you")
        );
        var isParticipantExists = bookingRepository.existsParticipantInBooking(booking.getId(), user.getId());
        if (!isParticipantExists){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sorry but you are not invited to this meeting.");
        }
        var roomOccupants = roomOccupancyRepository.getCurrentOccupants(roomId, booking.getStartTime(), currentTime);
        var roomOccupancy = new com.example.smartmeetingroom.entity.RoomOccupancy();
        roomOccupancy.setMeetingRoom(meetingRoomRepository.getReferenceById(roomId));

        if (roomOccupants == 0) {
            roomOccupancy.setCurrentCount(1);
        }else {
            roomOccupancy.setCurrentCount(roomOccupants + 1);
        }
        userRepository.updateUserStatus(List.of(user.getId()), UserStatus.IN_MEETING);
        roomOccupancyRepository.save(roomOccupancy);
        auditEventProducer.sendAuditEvent(
                new AuditEventDTO(
                        "ROOM_CHECK_IN",
                        "ROOM_OCCUPANCY",
                        roomOccupancy.getId(),
                        user.getId(),
                        user.getRoles().getId(),
                        "User checked into room " + roomId,
                        LocalDateTime.now()
                )
        );
    }
}
