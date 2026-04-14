package com.example.smartmeetingroom.dto.notification;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MarkNotificationReadRequest {
    private List<Long> notificationIds;
}
