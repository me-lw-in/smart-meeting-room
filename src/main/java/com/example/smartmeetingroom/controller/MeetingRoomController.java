package com.example.smartmeetingroom.controller;

import com.example.smartmeetingroom.dto.meetingrooms.MeetingRoomDTO;
import com.example.smartmeetingroom.dto.meetingrooms.MeetingRoomResponseDTO;
import com.example.smartmeetingroom.dto.page.PageResponseDTO;
import com.example.smartmeetingroom.enums.RoomStatus;
import com.example.smartmeetingroom.service.meetingroom.MeetingRoomService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/meeting-rooms")
class MeetingRoomController {

    private final MeetingRoomService meetingRoomService;

    @PostMapping()
    public ResponseEntity<Void> addMeetingRooms(@RequestBody @Valid MeetingRoomDTO dto){
        meetingRoomService.addMeetingRoom(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<MeetingRoomResponseDTO>> getAllRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) RoomStatus status,
            @RequestParam(defaultValue = "false") boolean includeDeleted
    ) {
        log.info("Fetch rooms request - page: {}, size: {}, floor: {}, status: {}",
                page, size, floor, status);

        var response = meetingRoomService.getAllMeetingRooms(page, size, floor, status, includeDeleted);

        log.info("Rooms fetched successfully - count: {}", response.getContent().size());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMeetingRoom(@PathVariable Long id) {
        meetingRoomService.deleteMeetingRoom(id);
        return ResponseEntity.ok("Meeting room is deleted");
    }
}
