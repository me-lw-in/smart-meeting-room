package com.example.smartmeetingroom.dto.procedure;

import com.example.smartmeetingroom.dto.asset.AssetDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProcedureDTO {
    private Long id;

    @NotBlank
    private String title;

    @NotBlank
    private String steps;

    public ProcedureDTO(Long id, String title, String steps) {
        this.id = id;
        this.title = title;
        this.steps = steps;
    }

    private List<AssetDTO> assets;
}
