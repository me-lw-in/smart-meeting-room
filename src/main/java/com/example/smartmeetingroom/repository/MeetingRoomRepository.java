package com.example.smartmeetingroom.repository;

import com.example.smartmeetingroom.entity.MeetingRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Long> {
    boolean existsByRoomNameAndFloor(String roomName, Integer floorNumber);
}
