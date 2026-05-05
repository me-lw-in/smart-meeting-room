package com.example.smartmeetingroom.service.booking;

import com.example.smartmeetingroom.dto.booking.BookingDTO;
import com.example.smartmeetingroom.dto.booking.PatchBookingDTO;
import com.example.smartmeetingroom.enums.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {

    public void bookMeetingRoom(BookingDTO dto);

    public void startMeetings(LocalDateTime now);

    public void endMeetings(LocalDateTime now);

    public void updateBookingInfo(PatchBookingDTO dto, Long bookingId);

    public List<BookingDTO> getMyBookings(BookingStatus status);

    public void cancelBooking(Long bookingId);
}
