package com.financial.dto;

import com.financial.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Category responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDto {
    
    private Long id;
    private String name;
    private String description;
    private Category.CategoryType type;
    private String color;
    private String icon;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Create response DTO from Category entity.
     */
    public static CategoryResponseDto fromEntity(Category category) {
        if (category == null) {
            return null;
        }
        return CategoryResponseDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .type(category.getType())
                .color(category.getColor())
                .icon(category.getIcon())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}



