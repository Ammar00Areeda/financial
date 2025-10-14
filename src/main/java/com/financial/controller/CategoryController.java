package com.financial.controller;

import com.financial.dto.CategoryCreateRequestDto;
import com.financial.dto.CategoryResponseDto;
import com.financial.dto.CategoryUpdateRequestDto;
import com.financial.entity.Category;
import com.financial.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for Category management operations.
 * Implements CategoryApi interface which contains all OpenAPI documentation.
 */
@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController implements CategoryApi {

    private final CategoryService categoryService;

    @Override
    @GetMapping
    public ResponseEntity<Page<CategoryResponseDto>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name,asc") String sort) {
        
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        Page<Category> categories = categoryService.getAllCategories(pageable);
        Page<CategoryResponseDto> responseDtos = categories.map(CategoryResponseDto::fromEntity);
        
        return ResponseEntity.ok(responseDtos);
    }

    @Override
    @GetMapping("/active")
    public ResponseEntity<List<CategoryResponseDto>> getAllActiveCategories() {
        List<Category> categories = categoryService.getAllActiveCategories();
        List<CategoryResponseDto> responseDtos = categories.stream()
                .map(CategoryResponseDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    @Override
    @GetMapping("/type/{type}")
    public ResponseEntity<List<CategoryResponseDto>> getCategoriesByType(@PathVariable String type) {
        try {
            Category.CategoryType categoryType = Category.CategoryType.valueOf(type.toUpperCase());
            List<Category> categories = categoryService.getCategoriesByType(categoryType);
            List<CategoryResponseDto> responseDtos = categories.stream()
                    .map(CategoryResponseDto::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responseDtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    @GetMapping("/active/type/{type}")
    public ResponseEntity<List<CategoryResponseDto>> getActiveCategoriesByType(@PathVariable String type) {
        try {
            Category.CategoryType categoryType = Category.CategoryType.valueOf(type.toUpperCase());
            List<Category> categories = categoryService.getActiveCategoriesByType(categoryType);
            List<CategoryResponseDto> responseDtos = categories.stream()
                    .map(CategoryResponseDto::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responseDtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable Long id) {
        Optional<Category> category = categoryService.getCategoryById(id);
        return category.map(CategoryResponseDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    @GetMapping("/search")
    public ResponseEntity<List<CategoryResponseDto>> searchCategoriesByName(@RequestParam String name) {
        List<Category> categories = categoryService.searchCategoriesByName(name);
        List<CategoryResponseDto> responseDtos = categories.stream()
                .map(CategoryResponseDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    @Override
    @GetMapping("/active/search")
    public ResponseEntity<List<CategoryResponseDto>> searchActiveCategoriesByName(@RequestParam String name) {
        List<Category> categories = categoryService.searchActiveCategoriesByName(name);
        List<CategoryResponseDto> responseDtos = categories.stream()
                .map(CategoryResponseDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    @Override
    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(@Valid @RequestBody CategoryCreateRequestDto request) {
        try {
            Category category = Category.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .type(request.getType())
                    .color(request.getColor())
                    .icon(request.getIcon())
                    .isActive(true)
                    .build();
            
            Category createdCategory = categoryService.createCategory(category);
            CategoryResponseDto response = CategoryResponseDto.fromEntity(createdCategory);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Error creating category: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequestDto request) {
        
        try {
            Optional<Category> existingCategoryOpt = categoryService.getCategoryById(id);
            if (existingCategoryOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Category existingCategory = existingCategoryOpt.get();
            existingCategory.setName(request.getName());
            existingCategory.setDescription(request.getDescription());
            if (request.getType() != null) {
                existingCategory.setType(request.getType());
            }
            if (request.getColor() != null) {
                existingCategory.setColor(request.getColor());
            }
            if (request.getIcon() != null) {
                existingCategory.setIcon(request.getIcon());
            }
            if (request.getIsActive() != null) {
                existingCategory.setIsActive(request.getIsActive());
            }
            
            Category updatedCategory = categoryService.updateCategory(existingCategory);
            CategoryResponseDto response = CategoryResponseDto.fromEntity(updatedCategory);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error updating category: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<CategoryResponseDto> deactivateCategory(@PathVariable Long id) {
        try {
            Category category = categoryService.deactivateCategory(id);
            CategoryResponseDto response = CategoryResponseDto.fromEntity(category);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    @PatchMapping("/{id}/activate")
    public ResponseEntity<CategoryResponseDto> activateCategory(@PathVariable Long id) {
        try {
            Category category = categoryService.activateCategory(id);
            CategoryResponseDto response = CategoryResponseDto.fromEntity(category);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
