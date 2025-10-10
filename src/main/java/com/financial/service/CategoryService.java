package com.financial.service;

import com.financial.entity.Category;
import com.financial.repository.CategoryRepository;
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
     * Get all categories with pagination.
     *
     * @param pageable pagination information
     * @return page of categories
     */
    @Transactional(readOnly = true)
    public Page<Category> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }
    
    /**
     * Get all categories.
     *
     * @return list of all categories
     */
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    /**
     * Get all active categories.
     *
     * @return list of active categories
     */
    @Transactional(readOnly = true)
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findByIsActive(true);
    }
    
    /**
     * Get all active categories with pagination.
     *
     * @param pageable pagination information
     * @return page of active categories
     */
    @Transactional(readOnly = true)
    public Page<Category> getAllActiveCategories(Pageable pageable) {
        return categoryRepository.findByIsActive(true, pageable);
    }
    
    /**
     * Get categories by type.
     *
     * @param type the category type
     * @return list of categories with the specified type
     */
    @Transactional(readOnly = true)
    public List<Category> getCategoriesByType(Category.CategoryType type) {
        return categoryRepository.findByType(type);
    }
    
    /**
     * Get active categories by type.
     *
     * @param type the category type
     * @return list of active categories with the specified type
     */
    @Transactional(readOnly = true)
    public List<Category> getActiveCategoriesByType(Category.CategoryType type) {
        return categoryRepository.findByTypeAndIsActive(type, true);
    }
    
    /**
     * Get category by ID.
     *
     * @param id the category ID
     * @return Optional containing the category if found
     */
    @Transactional(readOnly = true)
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
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
     * Search categories by name.
     *
     * @param name the name pattern to search for
     * @return list of categories matching the pattern
     */
    @Transactional(readOnly = true)
    public List<Category> searchCategoriesByName(String name) {
        return categoryRepository.findByNameContainingIgnoreCase(name);
    }
    
    /**
     * Search active categories by name.
     *
     * @param name the name pattern to search for
     * @return list of active categories matching the pattern
     */
    @Transactional(readOnly = true)
    public List<Category> searchActiveCategoriesByName(String name) {
        return categoryRepository.findByNameContainingIgnoreCaseAndIsActive(name, true);
    }
    
    /**
     * Create a new category.
     *
     * @param category the category to create
     * @return the created category
     * @throws IllegalArgumentException if category with same name already exists
     */
    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("Category with name '" + category.getName() + "' already exists");
        }
        
        if (category.getIsActive() == null) {
            category.setIsActive(true);
        }
        
        return categoryRepository.save(category);
    }
    
    /**
     * Update an existing category.
     *
     * @param category the category to update
     * @return the updated category
     * @throws IllegalArgumentException if category with same name already exists (excluding current)
     */
    public Category updateCategory(Category category) {
        if (categoryRepository.existsByNameAndIdNot(category.getName(), category.getId())) {
            throw new IllegalArgumentException("Category with name '" + category.getName() + "' already exists");
        }
        
        return categoryRepository.save(category);
    }
    
    /**
     * Delete category by ID.
     *
     * @param id the category ID
     * @throws IllegalArgumentException if category not found
     */
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new IllegalArgumentException("Category with ID " + id + " not found");
        }
        
        categoryRepository.deleteById(id);
    }
    
    /**
     * Soft delete category by ID (set isActive to false).
     *
     * @param id the category ID
     * @return the updated category
     * @throws IllegalArgumentException if category not found
     */
    public Category deactivateCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category with ID " + id + " not found"));
        
        category.setIsActive(false);
        return categoryRepository.save(category);
    }
    
    /**
     * Activate category by ID (set isActive to true).
     *
     * @param id the category ID
     * @return the updated category
     * @throws IllegalArgumentException if category not found
     */
    public Category activateCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category with ID " + id + " not found"));
        
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
