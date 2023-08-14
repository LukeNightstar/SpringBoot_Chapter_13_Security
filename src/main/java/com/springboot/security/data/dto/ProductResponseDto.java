package com.springboot.security.data.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDto {
    private Long number;
    private String name;
    private int price;
    private int stock;

    // @NoArgsConstructo
    // public ProductResponseDto() {}

    // @AllArgsConstructor
    /*public ProductResponseDto(Long number, String name, int price, int stock) {
        this.number = number;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }*/

}
