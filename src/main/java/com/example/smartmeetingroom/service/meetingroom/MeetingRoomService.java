package com.example.smartmeetingroom.service.meetingroom;

import com.example.smartmeetingroom.dto.meetingrooms.MeetingRoomDTO;
import com.example.smartmeetingroom.dto.meetingrooms.MeetingRoomResponseDTO;
import com.example.smartmeetingroom.dto.meetingrooms.UpdateMeetingRoomRequest;
import com.example.smartmeetingroom.dto.page.PageResponseDTO;
import com.example.smartmeetingroom.enums.RoomStatus;
import org.springframework.data.domain.Page;

public interface MeetingRoomService {

    public void addMeetingRoom(MeetingRoomDTO dto);

    public PageResponseDTO<MeetingRoomResponseDTO> getAllMeetingRoomsWithAssets(int page, int size, Integer floor, RoomStatus meetingRoomStatus, boolean includeDeleted);

    public void deleteMeetingRoom(Long roomId);

    public void updateMeetingRoom(Long roomId, UpdateMeetingRoomRequest request);

    public Page<MeetingRoomResponseDTO> getMeetingRooms(
            RoomStatus status,
            boolean includeDeleted,
            boolean onlyDeleted,
            int page,
            int size
    );
}
