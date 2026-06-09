package com.example.smartmeetingroom.controller;

import com.example.smartmeetingroom.dto.auth.LoginDTO;
import com.example.smartmeetingroom.service.roomoccupancy.RoomOccupancy;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/room-occupancy")
class RoomOccupancyController {

    private final RoomOccupancy roomOccupancy;

    @PostMapping("/check-in/{roomId}")
    public ResponseEntity<Void> checkIn(@RequestBody @Valid LoginDTO dto, @PathVariable Long roomId){
        roomOccupancy.checkIn(dto,roomId);
        return ResponseEntity.ok().build();
    }
}
