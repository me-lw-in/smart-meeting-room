package com.example.smartmeetingroom.service.producer;

import com.example.smartmeetingroom.dto.event.MeetingRoomBookedEvent;

public interface ProducerService {

    void sendMeetingBookedEvent(MeetingRoomBookedEvent event);
}
