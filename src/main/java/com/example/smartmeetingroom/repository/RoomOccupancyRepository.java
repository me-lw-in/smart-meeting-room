package com.example.smartmeetingroom.repository;

import com.example.smartmeetingroom.entity.RoomOccupancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface RoomOccupancyRepository extends JpaRepository<RoomOccupancy, Long> {

    @Query("""
        SELECT COUNT(r)
        FROM RoomOccupancy r
        WHERE r.meetingRoom.id = :meetingRoomId
        AND r.recordedAt >= :startTime
        AND r.recordedAt <= :currentTime
    """)
    int getCurrentOccupants(Long meetingRoomId, LocalDateTime startTime,LocalDateTime currentTime);
}
