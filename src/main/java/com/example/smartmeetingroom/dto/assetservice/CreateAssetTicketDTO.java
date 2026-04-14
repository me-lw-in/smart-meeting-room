package com.example.smartmeetingroom.dto.assetservice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAssetTicketDTO {

    @NotNull(message = "Asset id is required.")
    private Long assetId;

    @NotBlank(message = "Description is required.")
    private String description;
}
