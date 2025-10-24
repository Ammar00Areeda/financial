package com.financial.service;

import com.financial.entity.Category;
import com.financial.entity.User;
import com.financial.repository.CategoryRepository;
import com.financial.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for category-related business logic.
 * 
 * <p>This service manages both system-wide categories and user-specific custom categories.
 * System categories (user_id = NULL) are available to all users, while custom categories
 * are only visible to the user who created them.</p>
 * 
 * <p><b>Security:</b> All methods require authentication. Read operations return both
 * system categories and user's custom categories. Write operations (create, update, delete)
 * can only be performed on the authenticated user's custom categories. System categories
 * are read-only.</p>
 * 
 * @see Category
 * @see CategoryRepository
 * @see SecurityUtils
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    /**
     * Retrieves all categories available to the authenticated user with pagination.
     * 
     * <p>Returns both system categories (shared by all users) and custom categories
     * created by the authenticated user.</p>
     * 
     * <p><b>Security:</b> Requires authentication. Returns system categories and
     * categories belonging to the authenticated user only.</p>
     * 
     * <p><b>Example:</b></p>
     * <pre>{@code
     * Pageable pageable = PageRequest.of(0, 20);
     * Page<Category> categories = categoryService.getAllCategories(pageable);
     * // Returns paginated categories for the authenticated user
     * }</pre>
     *
     * @param pageable pagination information including page number, size, and sort order
     * @return page of categories available to the authenticated user
     * @throws org.springframework.security.authentication.AuthenticationCredentialsNotFoundException 
     *         if no authenticated user is found
     */
    @Transactional(readOnly = true)
    public Page<Category> getAllCategories(Pageable pageable) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return categoryRepository.findByUserOrSystem(currentUser, pageable);
    }
    
    /**
     * Get all categories for the authenticated user (including system categories).
     *
     * @return list of all categories
     */
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return categoryRepository.findByUserOrSystem(currentUser);
    }
    
    /**
     * Get all active categories for the authenticated user (including system categories).
     *
     * @return list of active categories
     */
    @Transactional(readOnly = true)
    public List<Category> getAllActiveCategories() {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return categoryRepository.findByUserOrSystemAndIsActive(currentUser, true);
    }
    
    /**
     * Get all active categories with pagination for the authenticated user (including system categories).
     *
     * @param pageable pagination information
     * @return page of active categories
     */
    @Transactional(readOnly = true)
    public Page<Category> getAllActiveCategories(Pageable pageable) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return categoryRepository.findByUserOrSystemAndIsActive(currentUser, Boolean.TRUE, pageable);
    }
    
    /**
     * Get categories by type for the authenticated user (including system categories).
     *
     * @param type the category type
     * @return list of categories with the specified type
     */
    @Transactional(readOnly = true)
    public List<Category> getCategoriesByType(Category.CategoryType type) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return categoryRepository.findByUserOrSystemAndType(currentUser, type);
    }
    
    /**
     * Get active categories by type for the authenticated user (including system categories).
     *
     * @param type the category type
     * @return list of active categories with the specified type
     */
    @Transactional(readOnly = true)
    public List<Category> getActiveCategoriesByType(Category.CategoryType type) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        List<Category> categories = categoryRepository.findByUserOrSystemAndType(currentUser, type);
        return categories.stream()
                .filter(c -> c.getIsActive() != null && c.getIsActive())
                .toList();
    }
    
    /**
     * Get category by ID for the authenticated user (including system categories).
     *
     * @param id the category ID
     * @return Optional containing the category if found
     */
    @Transactional(readOnly = true)
    public Optional<Category> getCategoryById(Long id) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return categoryRepository.findByIdAndUserOrSystem(id, currentUser);
    }
    
    /**
     * Get category by name.
     *
     * @param name the category name
     * @return Optional containing the category if found
     */
    @Transactional(readOnly = true)
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }
    
    /**
     * Search categories by name for the authenticated user (including system categories).
     *
     * @param name the name pattern to search for
     * @return list of categories matching the pattern
     */
    @Transactional(readOnly = true)
    public List<Category> searchCategoriesByName(String name) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        List<Category> allCategories = categoryRepository.findByUserOrSystem(currentUser);
        return allCategories.stream()
                .filter(c -> c.getName() != null && 
                            c.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }
    
    /**
     * Search active categories by name for the authenticated user (including system categories).
     *
     * @param name the name pattern to search for
     * @return list of active categories matching the pattern
     */
    @Transactional(readOnly = true)
    public List<Category> searchActiveCategoriesByName(String name) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        List<Category> categories = categoryRepository.findByUserOrSystemAndIsActive(currentUser, true);
        return categories.stream()
                .filter(c -> c.getName() != null && 
                            c.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }
    
    /**
     * Creates a new custom category for the authenticated user.
     * 
     * <p>The category is automatically associated with the authenticated user, making it
     * a custom user category (not a system category). Category names must be globally unique
     * across all users and system categories. Default value for isActive is set to true if
     * not provided.</p>
     * 
     * <p><b>Note:</b> Only users can create custom categories. System categories (user_id = NULL)
     * can only be created directly in the database.</p>
     * 
     * <p><b>Security:</b> Requires authentication. The category is associated with the
     * authenticated user and cannot be transferred to another user.</p>
     * 
     * <p><b>Example:</b></p>
     * <pre>{@code
     * Category category = Category.builder()
     *     .name("Personal Projects")
     *     .type(CategoryType.EXPENSE)
     *     .icon("🎯")
     *     .color("#FF5733")
     *     .isActive(true)
     *     .build();
     * 
     * Category created = categoryService.createCategory(category);
     * System.out.println("Category created with ID: " + created.getId());
     * }</pre>
     *
     * @param category the category to create (must not be null, name is required)
     * @return the persisted category with generated ID and default values applied
     * @throws IllegalArgumentException if a category with the same name already exists
     * @throws org.springframework.security.authentication.AuthenticationCredentialsNotFoundException 
     *         if no authenticated user is found
     */
    public Category createCategory(Category category) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        
        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("Category with name '" + category.getName() + "' already exists");
        }
        
        // Associate category with current user (making it a custom user category)
        category.setUser(currentUser);
        
        if (category.getIsActive() == null) {
            category.setIsActive(true);
        }
        
        return categoryRepository.save(category);
    }
    
    /**
     * Updates an existing custom category for the authenticated user.
     * 
     * <p>Verifies ownership and prevents modification of system categories. Users can only
     * update their own custom categories. Category names must remain globally unique.</p>
     * 
     * <p><b>Note:</b> Users can only update their own custom categories, not system categories.
     * System categories are read-only for all users.</p>
     * 
     * <p><b>Security:</b> Requires authentication. Only custom categories belonging to the
     * authenticated user can be updated. System categories cannot be modified. The user
     * association is immutable.</p>
     * 
     * <p><b>Example:</b></p>
     * <pre>{@code
     * Category category = categoryService.getCategoryById(123L)
     *     .orElseThrow(() -> new NotFoundException("Category not found"));
     * 
     * // This will fail if category is a system category
     * category.setName("Updated Project Name");
     * category.setColor("#00FF00");
     * 
     * Category updated = categoryService.updateCategory(category);
     * System.out.println("Category updated: " + updated.getName());
     * }</pre>
     *
     * @param category the category to update with modified fields
     * @return the updated and persisted category
     * @throws IllegalArgumentException if the category doesn't exist, is a system category,
     *         doesn't belong to the authenticated user, or if another category with the same
     *         name already exists
     * @throws org.springframework.security.authentication.AuthenticationCredentialsNotFoundException 
     *         if no authenticated user is found
     */
    public Category updateCategory(Category category) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        
        // Check if category exists and belongs to user
        Category existingCategory = categoryRepository.findById(category.getId())
                .orElseThrow(() -> new IllegalArgumentException("Category with ID " + category.getId() + " not found"));
        
        // Prevent modification of system categories
        if (existingCategory.getUser() == null) {
            throw new IllegalArgumentException("Cannot modify system category");
        }
        
        // Verify ownership
        if (!existingCategory.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Cannot modify category that doesn't belong to you");
        }
        
        if (categoryRepository.existsByNameAndIdNot(category.getName(), category.getId())) {
            throw new IllegalArgumentException("Category with name '" + category.getName() + "' already exists");
        }
        
        // Ensure user association is not changed
        category.setUser(currentUser);
        
        return categoryRepository.save(category);
    }
    
    /**
     * Deletes a custom category by ID for the authenticated user.
     * 
     * <p>Verifies ownership and prevents deletion of system categories. This operation is
     * permanent and cannot be undone. Consider the impact on related transactions before
     * deleting.</p>
     * 
     * <p><b>Note:</b> Users can only delete their own custom categories, not system categories.
     * System categories cannot be deleted.</p>
     * 
     * <p><b>Security:</b> Requires authentication. Only custom categories belonging to the
     * authenticated user can be deleted. System categories are protected from deletion.</p>
     * 
     * <p><b>Example:</b></p>
     * <pre>{@code
     * try {
     *     categoryService.deleteCategory(123L);
     *     System.out.println("Category deleted successfully");
     * } catch (IllegalArgumentException e) {
     *     // Will throw if category is a system category or not owned by user
     *     System.err.println("Cannot delete: " + e.getMessage());
     * }
     * }</pre>
     *
     * @param id the ID of the category to delete
     * @throws IllegalArgumentException if the category doesn't exist, is a system category,
     *         or doesn't belong to the authenticated user
     * @throws org.springframework.security.authentication.AuthenticationCredentialsNotFoundException 
     *         if no authenticated user is found
     */
    public void deleteCategory(Long id) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category with ID " + id + " not found"));
        
        // Prevent deletion of system categories
        if (category.getUser() == null) {
            throw new IllegalArgumentException("Cannot delete system category");
        }
        
        // Verify ownership
        if (!category.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Cannot delete category that doesn't belong to you");
        }
        
        categoryRepository.delete(category);
    }
    
    /**
     * Soft delete category by ID (set isActive to false) for the authenticated user.
     * Note: Users can only deactivate their own custom categories.
     *
     * @param id the category ID
     * @return the updated category
     * @throws IllegalArgumentException if category not found or attempting to modify system category
     */
    public Category deactivateCategory(Long id) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category with ID " + id + " not found"));
        
        // Prevent modification of system categories
        if (category.getUser() == null) {
            throw new IllegalArgumentException("Cannot modify system category");
        }
        
        // Verify ownership
        if (!category.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Cannot modify category that doesn't belong to you");
        }
        
        category.setIsActive(false);
        return categoryRepository.save(category);
    }
    
    /**
     * Activate category by ID (set isActive to true) for the authenticated user.
     * Note: Users can only activate their own custom categories.
     *
     * @param id the category ID
     * @return the updated category
     * @throws IllegalArgumentException if category not found or attempting to modify system category
     */
    public Category activateCategory(Long id) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category with ID " + id + " not found"));
        
        // Prevent modification of system categories
        if (category.getUser() == null) {
            throw new IllegalArgumentException("Cannot modify system category");
        }
        
        // Verify ownership
        if (!category.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Cannot modify category that doesn't belong to you");
        }
        
        category.setIsActive(true);
        return categoryRepository.save(category);
    }
    
    /**
     * Check if category exists by name.
     *
     * @param name the category name to check
     * @return true if category exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean categoryExistsByName(String name) {
        return categoryRepository.existsByName(name);
    }
    
    /**
     * Check if category exists by name excluding a specific ID.
     *
     * @param name the category name to check
     * @param id the ID to exclude
     * @return true if category exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean categoryExistsByNameAndIdNot(String name, Long id) {
        return categoryRepository.existsByNameAndIdNot(name, id);
    }
}
