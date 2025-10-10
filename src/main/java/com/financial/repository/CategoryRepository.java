package com.financial.repository;

import com.financial.entity.Category;
import com.financial.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Category entity operations.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // User-based queries
    /**
     * Find category by ID and user (or system categories with null user).
     *
     * @param id the category ID
     * @param user the user
     * @return Optional containing the category if found
     */
    @Query("SELECT c FROM Category c WHERE c.id = :id AND (c.user = :user OR c.user IS NULL)")
    Optional<Category> findByIdAndUserOrSystem(@Param("id") Long id, @Param("user") User user);
    
    /**
     * Find all categories for user (including system categories).
     *
     * @param user the user
     * @return list of categories
     */
    @Query("SELECT c FROM Category c WHERE c.user = :user OR c.user IS NULL")
    List<Category> findByUserOrSystem(@Param("user") User user);
    
    /**
     * Find categories by user and type (including system categories).
     *
     * @param user the user
     * @param type the category type
     * @return list of categories
     */
    @Query("SELECT c FROM Category c WHERE (c.user = :user OR c.user IS NULL) AND c.type = :type")
    List<Category> findByUserOrSystemAndType(@Param("user") User user, @Param("type") Category.CategoryType type);
    
    /**
     * Find active categories by user (including system categories).
     *
     * @param user the user
     * @param isActive whether the category is active
     * @return list of active categories
     */
    @Query("SELECT c FROM Category c WHERE (c.user = :user OR c.user IS NULL) AND c.isActive = :isActive")
    List<Category> findByUserOrSystemAndIsActive(@Param("user") User user, @Param("isActive") Boolean isActive);
    
    /**
     * Find category by name.
     *
     * @param name the category name to search for
     * @return Optional containing the category if found
     */
    Optional<Category> findByName(String name);
    
    /**
     * Find categories by type.
     *
     * @param type the category type
     * @return list of categories with the specified type
     */
    List<Category> findByType(Category.CategoryType type);
    
    /**
     * Find active categories by type.
     *
     * @param type the category type
     * @param isActive whether the category is active
     * @return list of active categories with the specified type
     */
    List<Category> findByTypeAndIsActive(Category.CategoryType type, Boolean isActive);
    
    /**
     * Find all active categories.
     *
     * @param isActive whether the category is active
     * @return list of active categories
     */
    List<Category> findByIsActive(Boolean isActive);
    
    /**
     * Find all active categories with pagination.
     *
     * @param isActive whether the category is active
     * @param pageable pagination information
     * @return page of active categories
     */
    Page<Category> findByIsActive(Boolean isActive, Pageable pageable);
    
    /**
     * Check if category exists by name.
     *
     * @param name the category name to check
     * @return true if category exists, false otherwise
     */
    boolean existsByName(String name);
    
    /**
     * Check if category exists by name excluding a specific ID.
     *
     * @param name the category name to check
     * @param id the ID to exclude
     * @return true if category exists, false otherwise
     */
    boolean existsByNameAndIdNot(String name, Long id);
    
    /**
     * Find categories by name containing (case-insensitive).
     *
     * @param name the name pattern to search for
     * @return list of categories matching the pattern
     */
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Category> findByNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Find active categories by name containing (case-insensitive).
     *
     * @param name the name pattern to search for
     * @param isActive whether the category is active
     * @return list of active categories matching the pattern
     */
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) AND c.isActive = :isActive")
    List<Category> findByNameContainingIgnoreCaseAndIsActive(@Param("name") String name, @Param("isActive") Boolean isActive);
}
