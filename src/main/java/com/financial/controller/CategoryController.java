package com.financial.controller;

import com.financial.entity.Category;
import com.financial.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for Category management operations.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management operations")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(
            summary = "Get all categories",
            description = "Retrieve a list of all categories with pagination support"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved categories"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<Page<Category>> getAllCategories(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort criteria (field,direction)") @RequestParam(defaultValue = "name,asc") String sort) {
        
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        Page<Category> categories = categoryService.getAllCategories(pageable);
        
        return ResponseEntity.ok(categories);
    }

    @Operation(
            summary = "Get all active categories",
            description = "Retrieve a list of all active categories"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved active categories"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/active")
    public ResponseEntity<List<Category>> getAllActiveCategories() {
        List<Category> categories = categoryService.getAllActiveCategories();
        return ResponseEntity.ok(categories);
    }

    @Operation(
            summary = "Get categories by type",
            description = "Retrieve categories filtered by type (INCOME, EXPENSE, TRANSFER)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved categories by type"),
            @ApiResponse(responseCode = "400", description = "Invalid category type"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Category>> getCategoriesByType(
            @Parameter(description = "Category type", required = true) @PathVariable String type) {
        
        try {
            Category.CategoryType categoryType = Category.CategoryType.valueOf(type.toUpperCase());
            List<Category> categories = categoryService.getCategoriesByType(categoryType);
            return ResponseEntity.ok(categories);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Get active categories by type",
            description = "Retrieve active categories filtered by type"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved active categories by type"),
            @ApiResponse(responseCode = "400", description = "Invalid category type"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/active/type/{type}")
    public ResponseEntity<List<Category>> getActiveCategoriesByType(
            @Parameter(description = "Category type", required = true) @PathVariable String type) {
        
        try {
            Category.CategoryType categoryType = Category.CategoryType.valueOf(type.toUpperCase());
            List<Category> categories = categoryService.getActiveCategoriesByType(categoryType);
            return ResponseEntity.ok(categories);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Get category by ID",
            description = "Retrieve a specific category by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(
            @Parameter(description = "Category ID", required = true) @PathVariable Long id) {
        
        Optional<Category> category = categoryService.getCategoryById(id);
        return category.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Search categories by name",
            description = "Search categories by name containing the provided text (case-insensitive)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved matching categories"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search")
    public ResponseEntity<List<Category>> searchCategoriesByName(
            @Parameter(description = "Name pattern to search for") @RequestParam String name) {
        
        List<Category> categories = categoryService.searchCategoriesByName(name);
        return ResponseEntity.ok(categories);
    }

    @Operation(
            summary = "Search active categories by name",
            description = "Search active categories by name containing the provided text"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved matching active categories"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/active/search")
    public ResponseEntity<List<Category>> searchActiveCategoriesByName(
            @Parameter(description = "Name pattern to search for") @RequestParam String name) {
        
        List<Category> categories = categoryService.searchActiveCategoriesByName(name);
        return ResponseEntity.ok(categories);
    }

    @Operation(
            summary = "Create a new category",
            description = "Create a new category in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - validation errors or duplicate name"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category) {
        try {
            Category createdCategory = categoryService.createCategory(category);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Update category",
            description = "Update an existing category"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - validation errors or duplicate name"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(
            @Parameter(description = "Category ID", required = true) @PathVariable Long id,
            @Valid @RequestBody Category category) {
        
        try {
            category.setId(id);
            Category updatedCategory = categoryService.updateCategory(category);
            return ResponseEntity.ok(updatedCategory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Delete category",
            description = "Delete a category from the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID", required = true) @PathVariable Long id) {
        
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Deactivate category",
            description = "Soft delete a category by setting it as inactive"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Category> deactivateCategory(
            @Parameter(description = "Category ID", required = true) @PathVariable Long id) {
        
        try {
            Category category = categoryService.deactivateCategory(id);
            return ResponseEntity.ok(category);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Activate category",
            description = "Activate a previously deactivated category"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category activated successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Category> activateCategory(
            @Parameter(description = "Category ID", required = true) @PathVariable Long id) {
        
        try {
            Category category = categoryService.activateCategory(id);
            return ResponseEntity.ok(category);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
