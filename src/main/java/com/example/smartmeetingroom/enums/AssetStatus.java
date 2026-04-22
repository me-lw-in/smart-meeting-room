package com.example.smartmeetingroom.enums;

public enum AssetStatus {
    ACTIVE, // (Keep as is - generic lifecycle flag, not tied to working condition)

    // Group: WORKING
    IN_USE, // Use case: Asset is currently being used in a room/session -
    AVAILABLE,  // Use case: Asset is working and free to be used - Group: WORKING

    // Group: NON-WORKING
    UNDER_MAINTENANCE,  // Use case: Asset is under repair/service after complaint approval
    DAMAGED,    // Use case: Asset is physically damaged and not usable (identified issue)
    OUT_OF_SERVICE, // Use case: Asset is completely unusable / not repairable
    RETIRED,    // Use case: Asset is permanently removed from system (end of life)
    LOST,   //Use case: Asset is missing / cannot be located
    INACTIVE,    // Use case: Asset temporarily disabled (not in use but not removed)

    // Group: TRANSITION
    PENDING_INSTALLATION  // Use case: Asset is purchased but not yet installed/ready
}
