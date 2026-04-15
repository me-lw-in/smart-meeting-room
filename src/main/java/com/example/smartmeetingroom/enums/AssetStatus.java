package com.example.smartmeetingroom.enums;

public enum AssetStatus {
    ACTIVE, // (Keep as is - generic lifecycle flag, not tied to working condition)

    IN_USE,
    // Use case: Asset is currently being used in a room/session
    // Group: WORKING

    AVAILABLE,
    // Use case: Asset is working and free to be used
    // Group: WORKING

    UNDER_MAINTENANCE,
    // Use case: Asset is under repair/service after complaint approval
    // Group: NON-WORKING

    DAMAGED,
    // Use case: Asset is physically damaged and not usable (identified issue)
    // Group: NON-WORKING

    OUT_OF_SERVICE,
    // Use case: Asset is completely unusable / not repairable
    // Group: NON-WORKING

    RETIRED,
    // Use case: Asset is permanently removed from system (end of life)
    // Group: NON-WORKING

    LOST,
    // Use case: Asset is missing / cannot be located
    // Group: NON-WORKING

    PENDING_INSTALLATION,
    // Use case: Asset is purchased but not yet installed/ready
    // Group: TRANSITION

    INACTIVE
    // Use case: Asset temporarily disabled (not in use but not removed)
    // Group: NON-WORKING
}
