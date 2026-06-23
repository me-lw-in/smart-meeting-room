package com.example.smartmeetingroom.dto.booking;

import java.time.LocalDateTime;

public interface ExtensionReminderProjection {
    Long getBookingId();

    Long getCreatedBy();

    LocalDateTime getNextMeetingTime();
}
