package com.example.smartmeetingroom.service.booking;

import com.example.smartmeetingroom.dto.booking.BookingDTO;
import com.example.smartmeetingroom.dto.booking.ParticipantsDTO;
import com.example.smartmeetingroom.dto.booking.PatchBookingDTO;
import com.example.smartmeetingroom.enums.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {

    void bookMeetingRoom(BookingDTO dto);

    void startMeetings(LocalDateTime now);

    void endMeetings(LocalDateTime now);

    void updateBookingInfo(PatchBookingDTO dto, Long bookingId);

    List<BookingDTO> getMyBookings(BookingStatus status);

    void cancelBooking(Long bookingId);

    void sendNotificationForMeetingExtension();

    ParticipantsDTO getTotalParticipantsCount();
}
