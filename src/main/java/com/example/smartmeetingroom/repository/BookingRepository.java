package com.example.smartmeetingroom.repository;

import com.example.smartmeetingroom.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
