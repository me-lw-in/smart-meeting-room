package com.example.smartmeetingroom.enums;

public enum AssetServiceStatus {
    OPEN,              // Complaint raised
    SCHEDULED,         // Service date assigned
    IN_PROGRESS,       // Work started
    REJECTED,          // Not genuine
    RESOLVED,          // Fixed
    CLOSED             // Final closure (optional)
}
