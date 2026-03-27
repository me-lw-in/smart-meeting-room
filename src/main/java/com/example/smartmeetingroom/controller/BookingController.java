package com.example.smartmeetingroom.controller;

import com.example.smartmeetingroom.dto.booking.BookingDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
class BookingController {

    @PostMapping()
    public ResponseEntity<Void> bookMeetingRoom(@RequestBody @Valid BookingDTO dto){

    }
}
