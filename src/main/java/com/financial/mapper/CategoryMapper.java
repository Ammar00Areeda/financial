package com.financial.mapper;

import com.financial.dto.CategoryDto;
import com.financial.entity.Category;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Category entity and CategoryDto.
 */
@Component
public class CategoryMapper {
    
    public CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }
        
        return CategoryDto.builder()
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
    
    public Category toEntity(CategoryDto categoryDto) {
        if (categoryDto == null) {
            return null;
        }
        
        return Category.builder()
                .id(categoryDto.getId())
                .name(categoryDto.getName())
                .description(categoryDto.getDescription())
                .type(categoryDto.getType())
                .color(categoryDto.getColor())
                .icon(categoryDto.getIcon())
                .isActive(categoryDto.getIsActive())
                .createdAt(categoryDto.getCreatedAt())
                .updatedAt(categoryDto.getUpdatedAt())
                .build();
    }
    
    public void updateEntityFromDto(Category entity, CategoryDto dto) {
        if (entity == null || dto == null) {
            return;
        }
        
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setType(dto.getType());
        entity.setColor(dto.getColor());
        entity.setIcon(dto.getIcon());
        entity.setIsActive(dto.getIsActive());
    }
}


