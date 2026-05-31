package com.example.smartmeetingroom.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventDTO {
    private String action;
    private String entityType;
    private Long entityId;
    private Long performedById;
    private Byte roleId;
    private String details;
    private LocalDateTime createdAt;
}
