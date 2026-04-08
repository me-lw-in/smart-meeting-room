package com.example.smartmeetingroom.scheduler;

import com.example.smartmeetingroom.service.booking.BookingService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@AllArgsConstructor
public class BookingSchedulerImpl implements BookingScheduler{

    private final BookingService bookingService;

    @Override
    @Transactional
    @Scheduled(cron = "0 * * * * *") // every 60 sec
    public void updateBookingStatus() {
        LocalDateTime now = LocalDateTime.now();

        bookingService.startMeetings(now);
        bookingService.endMeetings(now);
    }


}
