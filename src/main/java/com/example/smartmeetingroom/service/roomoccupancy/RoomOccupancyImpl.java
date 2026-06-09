package com.example.smartmeetingroom.service.roomoccupancy;

import com.example.smartmeetingroom.config.PasswordEncoderConfig;
import com.example.smartmeetingroom.dto.auth.LoginDTO;
import com.example.smartmeetingroom.enums.BookingStatus;
import com.example.smartmeetingroom.repository.BookingRepository;
import com.example.smartmeetingroom.repository.MeetingRoomRepository;
import com.example.smartmeetingroom.repository.RoomOccupancyRepository;
import com.example.smartmeetingroom.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class RoomOccupancyImpl implements RoomOccupancy{

    private final UserRepository userRepository;
    private final PasswordEncoderConfig passwordEncoder;
    private final BookingRepository bookingRepository;
    private final RoomOccupancyRepository roomOccupancyRepository;
    private final MeetingRoomRepository meetingRoomRepository;

    @Override
    public void checkIn(LoginDTO dto, Long roomId) {
        var email  = dto.getEmail().toLowerCase();
        var password  = dto.getPassword();
        var hashedPassword = passwordEncoder.passwordEncoder().encode(password);
        var user = userRepository.findByEmail(email).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.")
        );
        if (!hashedPassword.equals(user.getPassword())){
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

        roomOccupancyRepository.save(roomOccupancy);
    }
}
