package com.example.smartmeetingroom.service.meetingroom;

import com.example.smartmeetingroom.dto.asset.AssetCountDTO;
import com.example.smartmeetingroom.dto.meetingrooms.MeetingRoomDTO;
import com.example.smartmeetingroom.dto.meetingrooms.MeetingRoomResponseDTO;
import com.example.smartmeetingroom.dto.meetingrooms.UpdateMeetingRoomRequest;
import com.example.smartmeetingroom.dto.page.PageResponseDTO;
import com.example.smartmeetingroom.entity.MeetingRoom;
import com.example.smartmeetingroom.enums.BookingStatus;
import com.example.smartmeetingroom.enums.RoomStatus;
import com.example.smartmeetingroom.repository.AssetRepository;
import com.example.smartmeetingroom.repository.BookingRepository;
import com.example.smartmeetingroom.repository.MeetingRoomRepository;
import com.example.smartmeetingroom.specification.MeetingRoomSpecification;
import com.example.smartmeetingroom.util.SecurityUtil;
import com.example.smartmeetingroom.util.StringCapitalizeUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class MeetingRoomServiceImpl implements MeetingRoomService{

    private final AssetRepository assetRepository;
    private final BookingRepository bookingRepository;
    private final MeetingRoomRepository meetingRoomRepository;

    @Override
    @Transactional
    public void addMeetingRoom(MeetingRoomDTO dto){
        String roomName = StringCapitalizeUtil.capitalizeEachWord(dto.getMeetingRoomName());
        Integer floorNumber = dto.getFloorNumber();
        Integer capacity = dto.getCapacity();

        var meetingRoom = meetingRoomRepository.findByRoomNameAndFloor(roomName, floorNumber);
        if (meetingRoom.isPresent() && meetingRoom.get().getIsDeleted() == false) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, roomName + " already exits in floor " + floorNumber);
        }else if (meetingRoom.isPresent()) {
            meetingRoom.get().setRoomName(roomName);
            meetingRoom.get().setFloor(floorNumber);
            meetingRoom.get().setCapacity(capacity);
            meetingRoom.get().setStatus(RoomStatus.AVAILABLE);
        }else {
            var newMeetingRoom = new MeetingRoom();
            newMeetingRoom.setRoomName(roomName);
            newMeetingRoom.setFloor(floorNumber);
            newMeetingRoom.setCapacity(capacity);
            meetingRoomRepository.save(newMeetingRoom);
        }
    }

    public PageResponseDTO<MeetingRoomResponseDTO> getAllMeetingRoomsWithAssets(
            int page, int size, Integer floor, RoomStatus meetingRoomStatus, boolean includeDeleted) {

        String role = SecurityUtil.getCurrentUserRole();

        // Force restriction for non-admin users
        boolean isAdmin = "SUPER_ADMIN".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);

        if (!isAdmin) {
            includeDeleted = false;
        }

        var specification = MeetingRoomSpecification.getAllMeetingRooms(floor, meetingRoomStatus, includeDeleted, false);
        var sort = Sort.by(Sort.Direction.ASC, "floor");
        var pageable = PageRequest.of(page, size, sort);

        var rooms = meetingRoomRepository.findAll(specification, pageable);

        Set<Long> roomIds = rooms.getContent()
                .stream()
                .map(MeetingRoom::getId)
                .collect(Collectors.toSet());

        if (roomIds.isEmpty()) {
            return new PageResponseDTO<>(List.of(), page, size,
                    rooms.getTotalElements(), rooms.getTotalPages());
        }

        var totalAssets = assetRepository.getTotalAssets(roomIds);
        var assetStats = assetRepository.getAssetStats(roomIds);
        var assetNameAndCount = assetRepository.getAssetsByName(roomIds);

        Map<Long, Long> totalAssetMap = new HashMap<>();
        for (Object[] row : totalAssets) {
            totalAssetMap.put((Long) row[0], (Long) row[1]);
        }

        Map<Long, Long> workingMap = new HashMap<>();
        Map<Long, Long> nonWorkingMap = new HashMap<>();

        for (Object[] row : assetStats) {
            workingMap.put((Long) row[0], (Long) row[1]);
            nonWorkingMap.put((Long) row[0], (Long) row[2]);
        }


        Map<Long, List<AssetCountDTO>> assetNameMap = new HashMap<>();

        for (Object[] row : assetNameAndCount) {
            Long roomId = (Long) row[0];
            String assetName = (String) row[1];
            Long count = (Long) row[2];

            AssetCountDTO dto = new AssetCountDTO();
            dto.setAssetName(assetName);
            dto.setCount(count);

            assetNameMap
                    .computeIfAbsent(roomId, k -> new ArrayList<>())
                    .add(dto);
        }


        List<MeetingRoomResponseDTO> responseList = rooms.getContent().stream().map(room -> {

            Long roomId = room.getId();

            MeetingRoomResponseDTO dto = new MeetingRoomResponseDTO();
            dto.setRoomId(roomId);
            dto.setRoomName(room.getRoomName());
            dto.setFloor(room.getFloor());
            dto.setCapacity(room.getCapacity());
            dto.setStatus(room.getStatus().name());

            // 🔥 Handle NULL cases (IMPORTANT)
            dto.setTotalDevices(totalAssetMap.getOrDefault(roomId, 0L));
            dto.setWorkingDevices(workingMap.getOrDefault(roomId, 0L));
            dto.setNonWorkingDevices(nonWorkingMap.getOrDefault(roomId, 0L));

            dto.setAssets(assetNameMap.getOrDefault(roomId, List.of()));

            return dto;

        }).toList();

        return new PageResponseDTO<>(
                responseList,
                page,
                size,
                rooms.getTotalElements(),
                rooms.getTotalPages()
        );
    }

    @Override
    @Transactional
    public void deleteMeetingRoom(Long roomId) {

        MeetingRoom room = meetingRoomRepository.findByIdAndIsDeletedFalse(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting room not found"));

        boolean hasActiveBookings =
                bookingRepository.existsByRoom_IdAndStatusInAndIsDeletedFalse(
                        roomId,
                        List.of(BookingStatus.CONFIRMED, BookingStatus.STARTED)
                );
        if (hasActiveBookings) {
            throw new RuntimeException("Cannot delete room with active bookings");
        }

        if (room.getStatus() == RoomStatus.OCCUPIED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete meeting room during occupied.");
        }

        room.setIsDeleted(true);
        log.info("Meeting room and its assets are deleted by - {}", SecurityUtil.getCurrentUserId());
    }

    @Transactional
    public void updateMeetingRoom(Long roomId, UpdateMeetingRoomRequest request) {

        var room = meetingRoomRepository.findByIdAndIsDeletedFalse(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        if (room.getStatus() == RoomStatus.OCCUPIED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot update during meeting.");
        }

        if (request.getFloor() != null) {
            room.setFloor(request.getFloor());
        }

        if (request.getMeetingRoomName() != null) {
            room.setRoomName(StringCapitalizeUtil.capitalizeEachWord(request.getMeetingRoomName()));
        }

        if (request.getCapacity() != null) {
            room.setCapacity(request.getCapacity());
        }

        if (request.getStatus() != null && request.getStatus() == RoomStatus.OCCUPIED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are not allowed to update to this status.");
        }
        room.setStatus(request.getStatus());

    }

    @Override
    public Page<MeetingRoomResponseDTO> getMeetingRooms(
            RoomStatus status,
            boolean includeDeleted,
            boolean onlyDeleted,
            Pageable pageable
    ) {

        var role = SecurityUtil.getCurrentUserRole();

        if (role.equals("ADMIN")) {
            includeDeleted = false;
            onlyDeleted = false;
        }

        if (includeDeleted && onlyDeleted) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use either includeDeleted or onlyDeleted");
        }

        var spec = MeetingRoomSpecification.getAllMeetingRooms(
                null,
                status,
                includeDeleted,
                onlyDeleted
        );

        var meetingRooms =  meetingRoomRepository.findAll(spec, pageable);
        return meetingRooms.map(r ->
                new MeetingRoomResponseDTO(
                        r.getId(),
                        r.getRoomName(),
                        r.getFloor(),
                        r.getCapacity(),
                        r.getStatus().name()
                )
        );
    }
}
