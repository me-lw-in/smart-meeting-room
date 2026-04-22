package com.example.smartmeetingroom.dto.procedure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProcedureDTO {
    private Long id;
    private String title;
    private String steps;
}
