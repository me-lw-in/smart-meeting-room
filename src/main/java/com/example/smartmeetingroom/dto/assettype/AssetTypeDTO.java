package com.example.smartmeetingroom.dto.assettype;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetTypeDTO {
    @NotBlank(message = "Asset type is required")
    private String assetType;
}
