package com.example.smartmeetingroom.dto.page;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PageResponseDTO<T> {
    List<T> content;
    int page;
    int size;
    long totalElements;
    long totalPages;
}
