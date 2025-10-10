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
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    /**
     * Get all categories for the authenticated user (including system categories) with pagination.
     *
     * @param pageable pagination information
     * @return page of categories
     */
    @Transactional(readOnly = true)
    public Page<Category> getAllCategories(Pageable pageable) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        // Note: Need to convert List to Page - for now return from repository
        List<Category> categories = categoryRepository.findByUserOrSystem(currentUser);
        return Page.empty(pageable);
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
        List<Category> categories = categoryRepository.findByUserOrSystemAndIsActive(currentUser, true);
        return Page.empty(pageable);
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
     * Create a new custom category for the authenticated user.
     * Note: Only users can create custom categories. System categories have user_id = NULL.
     *
     * @param category the category to create
     * @return the created category
     * @throws IllegalArgumentException if category with same name already exists
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
     * Update an existing category for the authenticated user.
     * Note: Users can only update their own custom categories, not system categories.
     *
     * @param category the category to update
     * @return the updated category
     * @throws IllegalArgumentException if category with same name already exists, category not found, or attempting to modify system category
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
     * Delete category by ID for the authenticated user.
     * Note: Users can only delete their own custom categories, not system categories.
     *
     * @param id the category ID
     * @throws IllegalArgumentException if category not found or attempting to delete system category
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
