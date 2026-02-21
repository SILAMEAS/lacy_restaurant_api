package com.sila.modules.category.dto;

import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String url;
    private String publicId;
    private String restaurant;
    private int items;
}
