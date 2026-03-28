package com.example.smartmeetingroom.controller;

import com.example.smartmeetingroom.dto.booking.BookingDTO;
import com.example.smartmeetingroom.dto.booking.PatchBookingDTO;
import com.example.smartmeetingroom.service.booking.BookingService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/bookings")
class BookingController {

    private final BookingService bookingService;

    @PostMapping()
    public ResponseEntity<Void> bookMeetingRoom(@RequestBody @Valid BookingDTO dto){
        bookingService.bookMeetingRoom(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Void> updateBookingInfo(@RequestBody PatchBookingDTO dto, @PathVariable Long bookingId) {
        bookingService.updateBookingInfo(dto, bookingId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
