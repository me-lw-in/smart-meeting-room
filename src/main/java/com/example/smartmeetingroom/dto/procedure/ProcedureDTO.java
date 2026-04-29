package com.example.smartmeetingroom.dto.procedure;

import com.example.smartmeetingroom.dto.asset.AssetDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
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
