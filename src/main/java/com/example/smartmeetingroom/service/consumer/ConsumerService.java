package com.example.smartmeetingroom.service.consumer;

import com.example.smartmeetingroom.dto.event.MeetingRoomBookedEvent;

public interface ConsumerService {
    public void consume(MeetingRoomBookedEvent event);
}
