package com.example.smartmeetingroom.service.producer;

import com.example.smartmeetingroom.dto.audit.AuditEventDTO;
import com.example.smartmeetingroom.dto.event.MeetingRoomBookedEvent;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class KafkaProducerService implements ProducerService{
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void sendMeetingBookedEvent(MeetingRoomBookedEvent event) {
        kafkaTemplate.send("meeting-room-booked", event);
    }

    @Override
    public void sendAuditEvent(AuditEventDTO event) {

        kafkaTemplate.send("audit-events", event);

        System.out.println("Audit event sent");
    }
}
