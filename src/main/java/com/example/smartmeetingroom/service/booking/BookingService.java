package com.example.smartmeetingroom.service.booking;

import com.example.smartmeetingroom.dto.booking.BookingDTO;
import com.example.smartmeetingroom.dto.booking.PatchBookingDTO;

import java.time.LocalDateTime;

public interface BookingService {

    public void bookMeetingRoom(BookingDTO dto);

    public void startMeetings(LocalDateTime now);

    public void endMeetings(LocalDateTime now);

    public void updateBookingInfo(PatchBookingDTO dto, Long bookingId);
}
