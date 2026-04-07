package com.example.smartmeetingroom.dto.asset;

import com.example.smartmeetingroom.enums.AssetStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetDTO {
    @NotBlank(message = "Asset name is required")
    @Size(min = 3, max = 100, message = "Asset name must be between 3 and 100 characters")
    private String assetName;

    @NotBlank(message = "Serial number is required")
    @Size(min = 3, max = 100, message = "Serial number must be between 3 and 100 characters")
    private String serialNumber;

    @NotNull(message = "Purchase date is required")
    @PastOrPresent(message = "Purchase date cannot be in the future")
    private LocalDate purchaseDate;

    @NotNull(message = "Warranty expiry is required")
    @Future(message = "Warranty expiry must be a future date")
    private LocalDate warrantyExpiry;

    @NotNull(message = "Meeting room is required")
    @Positive(message = "Meeting room ID must be a positive number")
    private Long meetingRoomId;

    @NotNull(message = "Device type is required")
    @Positive(message = "Device type ID must be a positive number")
    private Short deviceTypeId;

    @NotNull(message = "Status is required")
    private AssetStatus assetStatus;

    Long assetId;
    String meetingRoomName;
    String assetTypeName;

}
