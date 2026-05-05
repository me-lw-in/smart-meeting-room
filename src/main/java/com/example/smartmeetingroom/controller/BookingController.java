package com.example.smartmeetingroom.controller;

import com.example.smartmeetingroom.dto.booking.BookingDTO;
import com.example.smartmeetingroom.dto.booking.PatchBookingDTO;
import com.example.smartmeetingroom.enums.BookingStatus;
import com.example.smartmeetingroom.service.booking.BookingService;
import com.example.smartmeetingroom.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/bookings")
class BookingController {

    private final BookingService bookingService;

    @PostMapping()
    public ResponseEntity<Void> bookMeetingRoom(@RequestBody @Valid BookingDTO dto){
        log.info("Booking request received for roomId: {}, startTime: {}, endTime: {} by userId: {}",
                dto.getMeetingRoomId(), dto.getStartTime(), dto.getEndTime(), SecurityUtil.getCurrentUserId());
        bookingService.bookMeetingRoom(dto);
        log.info("Meeting room booked successfully for roomId: {}", dto.getMeetingRoomId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Void> updateBookingInfo(@RequestBody PatchBookingDTO dto, @PathVariable Long bookingId) {
        log.info("Update booking request received for bookingId: {} by userId: {}", bookingId, SecurityUtil.getCurrentUserId());
        bookingService.updateBookingInfo(dto, bookingId);
        log.info("Booking updated successfully for bookingId: {}", bookingId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<List<BookingDTO>> getMyBookings(
            @RequestParam(required = false) BookingStatus status) {

        return ResponseEntity.ok(bookingService.getMyBookings(status));
    }

    @PatchMapping("/{bookingId}/cancel")
    public ResponseEntity<String> cancelBooking(@PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok("Booking cancelled successfully");
    }
}
