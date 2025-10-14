package com.financial.controller;

import com.financial.dto.CategoryCreateRequestDto;
import com.financial.dto.CategoryResponseDto;
import com.financial.dto.CategoryUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * API interface for Category management operations.
 * Contains all OpenAPI documentation for category endpoints.
 */
@Tag(name = "Categories", description = "Category management operations")
@SecurityRequirement(name = "Bearer Authentication")
public interface CategoryApi {

    @Operation(
            summary = "Get all categories",
            description = "Retrieve a list of all categories with pagination support"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved categories"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<Page<CategoryResponseDto>> getAllCategories(
            @Parameter(description = "Page number (0-based)") int page,
            @Parameter(description = "Number of items per page") int size,
            @Parameter(description = "Sort criteria (field,direction)") String sort);

    @Operation(
            summary = "Get all active categories",
            description = "Retrieve a list of all active categories"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved active categories"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<CategoryResponseDto>> getAllActiveCategories();

    @Operation(
            summary = "Get categories by type",
            description = "Retrieve categories filtered by type (INCOME, EXPENSE, TRANSFER)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved categories by type"),
            @ApiResponse(responseCode = "400", description = "Invalid category type"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<CategoryResponseDto>> getCategoriesByType(
            @Parameter(description = "Category type", required = true) String type);

    @Operation(
            summary = "Get active categories by type",
            description = "Retrieve active categories filtered by type"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved active categories by type"),
            @ApiResponse(responseCode = "400", description = "Invalid category type"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<CategoryResponseDto>> getActiveCategoriesByType(
            @Parameter(description = "Category type", required = true) String type);

    @Operation(
            summary = "Get category by ID",
            description = "Retrieve a specific category by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<CategoryResponseDto> getCategoryById(
            @Parameter(description = "Category ID", required = true) Long id);

    @Operation(
            summary = "Search categories by name",
            description = "Search categories by name containing the provided text (case-insensitive)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved matching categories"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<CategoryResponseDto>> searchCategoriesByName(
            @Parameter(description = "Name pattern to search for") String name);

    @Operation(
            summary = "Search active categories by name",
            description = "Search active categories by name containing the provided text"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved matching active categories"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<CategoryResponseDto>> searchActiveCategoriesByName(
            @Parameter(description = "Name pattern to search for") String name);

    @Operation(
            summary = "Create a new category",
            description = "Create a new category in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - validation errors or duplicate name"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<CategoryResponseDto> createCategory(CategoryCreateRequestDto request);

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
    ResponseEntity<CategoryResponseDto> updateCategory(
            @Parameter(description = "Category ID", required = true) Long id,
            CategoryUpdateRequestDto request);

    @Operation(
            summary = "Delete category",
            description = "Delete a category from the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID", required = true) Long id);

    @Operation(
            summary = "Deactivate category",
            description = "Soft delete a category by setting it as inactive"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<CategoryResponseDto> deactivateCategory(
            @Parameter(description = "Category ID", required = true) Long id);

    @Operation(
            summary = "Activate category",
            description = "Activate a previously deactivated category"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category activated successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<CategoryResponseDto> activateCategory(
            @Parameter(description = "Category ID", required = true) Long id);
}

