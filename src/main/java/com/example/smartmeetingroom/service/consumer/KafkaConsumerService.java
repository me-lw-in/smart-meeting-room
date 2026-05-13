package com.example.smartmeetingroom.service.consumer;

import com.example.smartmeetingroom.dto.event.MeetingRoomBookedEvent;
import com.example.smartmeetingroom.service.email.EmailService;
import com.example.smartmeetingroom.service.notification.NotificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class KafkaConsumerService implements ConsumerService{

    private final NotificationService  notificationService;
    private final EmailService emailService;

    @Override
    @KafkaListener(topics = "meeting-room-booked", groupId = "meeting-room-group")
    public void consume(MeetingRoomBookedEvent event) {
        notificationService.sendMeetingCreatedNotifications(
                event.getParticipantIds(),
                event.getLoggedInUserId(),
                event.getStartTime(),
                event.getRoomName(),
                event.getType());
        emailService.sendMeetingEmails(event.getParticipantIds(), event.getLoggedInUserId(), event.getStartTime(), event.getRoomName());
        log.info("Received Event : {}", event);
    }
}
