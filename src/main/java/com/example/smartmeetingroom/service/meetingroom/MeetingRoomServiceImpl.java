package com.example.smartmeetingroom.service.meetingroom;

import com.example.smartmeetingroom.dto.meetingrooms.MeetingRoomDTO;
import com.example.smartmeetingroom.entity.MeetingRoom;
import com.example.smartmeetingroom.repository.MeetingRoomRepository;
import com.example.smartmeetingroom.util.StringCapitalizeUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class MeetingRoomServiceImpl implements MeetingRoomService{

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
}
