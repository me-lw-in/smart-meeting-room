package com.example.smartmeetingroom.service.meetingroom;

import com.example.smartmeetingroom.dto.meetingrooms.MeetingRoomDTO;
import com.example.smartmeetingroom.dto.meetingrooms.MeetingRoomResponseDTO;
import com.example.smartmeetingroom.dto.page.PageResponseDTO;
import com.example.smartmeetingroom.enums.RoomStatus;

public interface MeetingRoomService {

    public void addMeetingRoom(MeetingRoomDTO dto);

    public PageResponseDTO<MeetingRoomResponseDTO> getAllMeetingRooms(int page, int size, Integer floor, RoomStatus meetingRoomStatus, boolean includeDeleted);

    public void deleteMeetingRoom(Long roomId);
}
