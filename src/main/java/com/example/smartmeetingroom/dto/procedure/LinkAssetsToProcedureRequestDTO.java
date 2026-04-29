package com.example.smartmeetingroom.dto.procedure;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LinkAssetsToProcedureRequestDTO {
    @NotEmpty
    private List<Long> assetIds;
}
