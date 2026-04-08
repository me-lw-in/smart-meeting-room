package com.example.smartmeetingroom.dto.asset;

import com.example.smartmeetingroom.enums.AssetStatus;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AssetUpdateDTO {

    private String assetName;

    @Size(min = 3, max = 100, message = "Serial number must be between 3 and 100 characters")
    private String serialNumber;

    @PastOrPresent(message = "Purchase date cannot be in the future")
    private LocalDate purchaseDate;

    private LocalDate warrantyExpiry;

    @Positive(message = "Meeting room ID must be a positive number")
    private Long meetingRoomId;

    @Positive(message = "Asset type ID must be a positive number")
    private Short assetTypeId;

    private AssetStatus assetStatus;
}
