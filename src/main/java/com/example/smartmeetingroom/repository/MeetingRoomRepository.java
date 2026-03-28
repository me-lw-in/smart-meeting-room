package com.example.smartmeetingroom.repository;

import com.example.smartmeetingroom.entity.MeetingRoom;
import com.example.smartmeetingroom.enums.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Long> {
    boolean existsByRoomNameAndFloor(String roomName, Integer floorNumber);

    Optional<MeetingRoom> findByIdAndStatus(Long id, RoomStatus status);

    @Query("""
        SELECT COUNT(b) > 0
        FROM Booking b
        WHERE b.room.id = :roomId
          AND b.status IN ('CONFIRMED', 'STARTED')
          AND b.startTime < :endTime
          AND b.endTime > :startTime
    """)
    boolean existsOverlappingBooking(Long roomId,
                                     LocalDateTime startTime,
                                     LocalDateTime endTime);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE MeetingRoom m SET m.status = :status WHERE m.id IN :ids")
    void updateRoomStatus(List<Long> ids, RoomStatus status);
}
