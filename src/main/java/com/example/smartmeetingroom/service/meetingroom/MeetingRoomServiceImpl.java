package com.example.smartmeetingroom.service.meetingroom;

import com.example.smartmeetingroom.dto.asset.AssetCountDTO;
import com.example.smartmeetingroom.dto.meetingrooms.MeetingRoomDTO;
import com.example.smartmeetingroom.dto.meetingrooms.MeetingRoomResponseDTO;
import com.example.smartmeetingroom.dto.page.PageResponseDTO;
import com.example.smartmeetingroom.entity.MeetingRoom;
import com.example.smartmeetingroom.enums.RoomStatus;
import com.example.smartmeetingroom.repository.AssetRepository;
import com.example.smartmeetingroom.repository.MeetingRoomRepository;
import com.example.smartmeetingroom.specification.MeetingRoomSpecification;
import com.example.smartmeetingroom.util.StringCapitalizeUtil;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MeetingRoomServiceImpl implements MeetingRoomService{

    private final AssetRepository assetRepository;
    private final MeetingRoomRepository meetingRoomRepository;

    @Override
    public void addMeetingRoom(MeetingRoomDTO dto){
        String roomName = StringCapitalizeUtil.capitalizeEachWord(dto.getMeetingRoomName());
        Integer floorNumber = dto.getFloorNumber();
        Integer capacity = dto.getCapacity();

        if (meetingRoomRepository.existsByRoomNameAndFloor(roomName, floorNumber)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, roomName + " already exits in floor " + floorNumber);
        }

        var meetingRoom = new MeetingRoom();
        meetingRoom.setRoomName(roomName);
        meetingRoom.setFloor(floorNumber);
        meetingRoom.setCapacity(capacity);
        meetingRoomRepository.save(meetingRoom);
    }

    public PageResponseDTO<MeetingRoomResponseDTO> getAllMeetingRooms(
            int page, int size, Integer floor, RoomStatus meetingRoomStatus) {

        var specification = MeetingRoomSpecification.getAllMeetingRooms(floor, meetingRoomStatus);
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
}
