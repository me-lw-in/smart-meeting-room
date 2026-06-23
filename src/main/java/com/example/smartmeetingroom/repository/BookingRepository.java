package com.example.smartmeetingroom.repository;

import com.example.smartmeetingroom.dto.booking.BookingDTO;
import com.example.smartmeetingroom.dto.booking.ExtensionReminderProjection;
import com.example.smartmeetingroom.entity.Booking;
import com.example.smartmeetingroom.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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

    @Query("""
    SELECT new com.example.smartmeetingroom.dto.booking.BookingDTO(
        b.id,
        b.room.roomName,
        b.startTime,
        b.endTime
    )
    FROM Booking b
    WHERE b.createdBy.id = :userId
    AND (:status IS NULL OR b.status = :status)
""")
    List<BookingDTO> findBookingsByUser(@Param("userId") Long userId,
                                        @Param("status") BookingStatus status);

    @Query("""
    SELECT b.id, new com.example.smartmeetingroom.dto.user.UserDTO(
        u.id,
        u.firstName,
        u.lastName
    )
    FROM Booking b
    JOIN b.participants u
    WHERE b.createdBy.id = :userId
    AND (:status IS NULL OR b.status = :status)
""")
    List<Object[]> findParticipantsForBookings(@Param("userId") Long userId,
                                               @Param("status") BookingStatus status);


    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.room.id = :roomId
        AND :now BETWEEN b.startTime AND b.endTime
        AND b.status = :status
    """)
    Optional<Booking> findBookingIdByRoomIdAndCurrentTime(
            @Param("roomId") Long roomId,
            @Param("now") LocalDateTime now,
            @Param("status") BookingStatus status
    );

    @Query("""
        SELECT COUNT(b) > 0
        FROM Booking b
        JOIN b.participants p
        WHERE b.id = :bookingId
          AND p.id = :userId
    """)
    boolean existsParticipantInBooking(
            @Param("bookingId") Long bookingId,
            @Param("userId") Long userId
    );

    @Query(value = """
    SELECT
        b.id AS bookingId,
        b.created_by AS createdBy,

        COALESCE(
            (
                SELECT MIN(nb.start_time)
                FROM bookings nb
                WHERE nb.room_id = b.room_id
                  AND nb.status = 'CONFIRMED'
                  AND nb.start_time >= b.end_time
            ),
            TIMESTAMP(CURRENT_DATE, '18:00:00')
        ) AS nextMeetingTime

    FROM bookings b
    WHERE b.status = 'STARTED'
      AND b.extension_reminder_sent = false
      AND b.end_time BETWEEN NOW()
                         AND DATE_ADD(NOW(), INTERVAL 10 MINUTE)

      AND COALESCE(
            (
                SELECT MIN(nb.start_time)
                FROM bookings nb
                WHERE nb.room_id = b.room_id
                  AND nb.status = 'CONFIRMED'
                  AND nb.start_time >= b.end_time
            ),
            TIMESTAMP(CURRENT_DATE, '18:00:00')
          ) > DATE_ADD(b.end_time, INTERVAL 5 MINUTE)
    """,
            nativeQuery = true)
    List<ExtensionReminderProjection> findExtensionEligibleBookings();

    @Modifying
    @Query("""
        UPDATE Booking b
        SET b.extensionReminderSent = true
        WHERE b.id IN :bookingIds
    """)
    void setExtensionRemainderSent(List<Long> bookingIds);

    @Query("""
    SELECT new com.example.smartmeetingroom.dto.booking.BookingDTO(
          b.startTime,
          b.endTime,
          b.room.id
    )
    FROM Booking b
    WHERE b.status = "STARTED"
    AND b.createdBy.id = :userId
    """)
    BookingDTO getCurrentUserBookingInfo(Long userId);

    @Query("""
    SELECT b.id
    FROM Booking b
    WHERE b.startTime < :to
    AND b.endTime > :from
    """)
    List<Long> findOverLappingBookings(LocalDateTime from, LocalDateTime to);

    @Query("""
    SELECT DISTINCT p.id
    FROM Booking b
    JOIN b.participants p
    WHERE b.id IN :bookingIds
    
    """)
    List<Long> findAvailableUsers(List<Long> bookingIds);
}
