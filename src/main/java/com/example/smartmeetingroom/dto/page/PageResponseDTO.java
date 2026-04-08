package com.example.smartmeetingroom.dto.page;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PageResponseDTO<T> {
    List<T> content;
    int page;
    int size;
    long totalElements;
    long totalPages;
}
