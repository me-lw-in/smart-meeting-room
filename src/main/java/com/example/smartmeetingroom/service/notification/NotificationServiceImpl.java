package com.example.smartmeetingroom.service.notification;

import com.example.smartmeetingroom.dto.notification.MarkNotificationReadRequest;
import com.example.smartmeetingroom.dto.notification.NotificationDTO;
import com.example.smartmeetingroom.entity.Notification;
import com.example.smartmeetingroom.enums.NotificationType;
import com.example.smartmeetingroom.repository.NotificationRepository;
import com.example.smartmeetingroom.repository.UserRepository;
import com.example.smartmeetingroom.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService{

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Async
    @Override
    public void sendMeetingCreatedNotifications(Set<Long> participantIds, Long loggedInUserId, LocalDateTime startTime, String meetingRoom, NotificationType type) {
        // Notification
        for (Long userid : participantIds) {
            var notification = new Notification();
            String message;
            if (userid.equals(loggedInUserId)) {
                message = "Your meeting has been scheduled at " + startTime + " in " +  meetingRoom + ".";
            }else {
                message = "You have been invited to a meeting at " + startTime + " in " +  meetingRoom + ".";
            }
            notification.setMessage(message);
            notification.setType(type);
            notification.setUser(userRepository.getReferenceById(userid));
            notificationRepository.save(notification);
            var dto = new NotificationDTO(
                    notification.getId(),
                    message,
                    notification.getType(),
                    notification.getIsRead(),
                    notification.getCreatedAt());
            pushNotification(userid, dto);
        }
    }

    @Override
    public List<NotificationDTO> getUnreadNotifications () {
        var loggedInUserId = SecurityUtil.getCurrentUserId();
        if (loggedInUserId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.");
        }
        var notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(loggedInUserId);
        return notifications.stream().map(n -> new NotificationDTO(
                    n.getId(),
                    n.getMessage(),
                    n.getType(),
                    n.getIsRead(),
                    n.getCreatedAt()
            )).toList();
    }

    @Override
    public void pushNotification(Long userId, NotificationDTO dto) {
        SseEmitter emitter = emitters.get(userId);

        if (emitter != null) {
            try {
                emitter.send(dto);
            } catch (Exception e) {
                emitter.complete();
                emitters.remove(userId);
            }
        }
    }

    @Override
    @Transactional
    public void markAsRead(MarkNotificationReadRequest notificationIds) {
        var loggedInUserId = SecurityUtil.getCurrentUserId();
        if (loggedInUserId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.");
        }
        notificationRepository.markAsRead(notificationIds.getNotificationIds(), loggedInUserId);
    }

    @Override
    public SseEmitter createEmitter() {

        Long userId = SecurityUtil.getCurrentUserId();

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        SseEmitter emitter = new SseEmitter(0L);

        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));


        return emitter;
    }

}
