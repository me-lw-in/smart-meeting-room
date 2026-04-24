package com.example.smartmeetingroom.repository;

import com.example.smartmeetingroom.entity.Booking;
import com.example.smartmeetingroom.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
    SELECT DISTINCT CONCAT(u.firstName, ' ', u.lastName)
    FROM Booking b
    JOIN b.participants u
    WHERE u.id IN :participantIds
      AND b.status IN ('CONFIRMED', 'STARTED')
      AND b.startTime < :endTime
      AND b.endTime > :startTime
      AND (:bookingId IS NUll OR b.id <> :bookingId)
""")
    List<String> findConflictingParticipantNames(Set<Long> participantIds,
                                                 Long bookingId,
                                                 LocalDateTime startTime,
                                                 LocalDateTime endTime);



    @Query("""
        SELECT b.id
        FROM Booking b
        WHERE b.status = :status
        AND b.startTime <= :time
    """)
    List<Long> findIdsByStatusAndStartTimeLessThanEqual(BookingStatus status, LocalDateTime time);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Booking b SET b.status = :status WHERE b.id IN :ids")
    void updateBookingStatus(List<Long> ids, BookingStatus status);

    @Query("""
        SELECT DISTINCT b.room.id
        FROM Booking b
        WHERE b.id IN :bookingIds
    """)
    List<Long> findDistinctRoomIds(List<Long> bookingIds);

    @Query("""
        SELECT DISTINCT u.id
        FROM Booking b
        JOIN b.participants u
        WHERE b.id IN :bookingIds
    """)
    List<Long> findDistinctUserIds(List<Long> bookingIds);

    @Query("""
    SELECT b.id
    FROM Booking b
    WHERE b.status = :status
    AND b.endTime <= :time
""")
    List<Long> findIdsByStatusAndEndTimeLessThanEqual(BookingStatus status, LocalDateTime time);

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

@Query("""
        SELECT COUNT(b) > 0
        FROM Booking b
        WHERE b.room.id = :roomId
          AND b.status IN ('CONFIRMED', 'STARTED')
          AND b.startTime < :endTime
          AND b.endTime > :startTime
          AND b.id <> :bookingId
    """)
    boolean existsOverlappingBookingAndNotById(Long roomId,
                                     Long bookingId,
                                     LocalDateTime startTime,
                                     LocalDateTime endTime);

    boolean existsByRoom_IdAndStatusInAndIsDeletedFalse(
            Long room_id, Collection<BookingStatus> status
    );
}
