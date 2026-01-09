package com.example.stok.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemDto {
    private Long id;
    private String name;
    private Double price;
    private Integer currentStock;
}
