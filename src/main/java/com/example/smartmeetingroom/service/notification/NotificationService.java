package com.example.smartmeetingroom.service.notification;

import com.example.smartmeetingroom.dto.notification.MarkNotificationReadRequest;
import com.example.smartmeetingroom.dto.notification.NotificationDTO;
import com.example.smartmeetingroom.enums.NotificationType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface NotificationService {

    public void sendMeetingCreatedNotifications(Set<Long> participantIds, Long loggedInUserId, LocalDateTime startTime, String meetingRoom, NotificationType type);

    public List<NotificationDTO> getUnreadNotifications ();

    public void markAsRead(MarkNotificationReadRequest notificationIds);

    public SseEmitter createEmitter();

    public void pushNotification(Long userId, NotificationDTO dto);


}
