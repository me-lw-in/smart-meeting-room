package com.example.smartmeetingroom.controller;

import com.example.smartmeetingroom.dto.meetingrooms.MeetingRoomDTO;
import com.example.smartmeetingroom.dto.meetingrooms.MeetingRoomResponseDTO;
import com.example.smartmeetingroom.dto.meetingrooms.UpdateMeetingRoomRequest;
import com.example.smartmeetingroom.dto.page.PageResponseDTO;
import com.example.smartmeetingroom.enums.RoomStatus;
import com.example.smartmeetingroom.service.meetingroom.MeetingRoomService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @GetMapping("/details")
    public ResponseEntity<PageResponseDTO<MeetingRoomResponseDTO>> getRoomsWithAssets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) RoomStatus status,
            @RequestParam(defaultValue = "false") boolean includeDeleted
    ) {
        log.info("Fetch rooms request - page: {}, size: {}, floor: {}, status: {}",
                page, size, floor, status);

        var response = meetingRoomService.getAllMeetingRoomsWithAssets(page, size, floor, status, includeDeleted);

        log.info("Rooms fetched successfully - count: {}", response.getContent().size());

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<MeetingRoomResponseDTO>> getMeetingRooms(
            @RequestParam(required = false) RoomStatus status,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(defaultValue = "false") boolean onlyDeleted,
            Pageable pageable
    ) {
        var rooms = meetingRoomService.getMeetingRooms(status, includeDeleted, onlyDeleted, pageable);
        return ResponseEntity.ok(rooms);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<String> updateMeetingRoom(
            @PathVariable Long id,
            @RequestBody UpdateMeetingRoomRequest request
    ) {
        meetingRoomService.updateMeetingRoom(id, request);
        return ResponseEntity.ok("Meeting room updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMeetingRoom(@PathVariable Long id) {
        meetingRoomService.deleteMeetingRoom(id);
        return ResponseEntity.ok("Meeting room is deleted");
    }
}
