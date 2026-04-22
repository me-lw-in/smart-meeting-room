package com.example.smartmeetingroom.enums;

public enum AssetServiceStatus {
    NEW,           // Complaint raised, waiting for admin review
    ASSIGNED,      // Admin marked as repairable and assigned technician
    IN_PROGRESS,   // Technician started working on the asset
    RESOLVED,      // Work completed (either fixed OR confirmed not repairable)
    REJECTED,       // Complaint is invalid / fake
    FAILED          // Service cannot be resolved
}
