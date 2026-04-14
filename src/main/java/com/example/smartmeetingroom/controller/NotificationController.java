package com.example.smartmeetingroom.controller;

import com.example.smartmeetingroom.dto.notification.MarkNotificationReadRequest;
import com.example.smartmeetingroom.dto.notification.NotificationDTO;
import com.example.smartmeetingroom.service.notification.NotificationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@AllArgsConstructor
@RequestMapping("/api/notifications")
class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/mark-read")
    public ResponseEntity<Void> markAsRead(@RequestBody @Valid MarkNotificationReadRequest notificationIds) {
        notificationService.markAsRead(notificationIds);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications() {
        var notifications =  notificationService.getUnreadNotifications();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/stream")
    public SseEmitter streamNotifications() {
        return notificationService.createEmitter();
    }

}
