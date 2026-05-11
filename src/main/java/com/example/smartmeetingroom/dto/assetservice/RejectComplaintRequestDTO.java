package com.example.smartmeetingroom.dto.assetservice;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectComplaintRequestDTO {

    @NotBlank(message = "Remarks are required")
    private String remarks;
}