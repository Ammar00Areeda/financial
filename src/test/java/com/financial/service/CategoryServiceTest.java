package com.financial.service;

import com.financial.entity.Category;
import com.financial.entity.User;
import com.financial.repository.CategoryRepository;
import com.financial.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CategoryService.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User testUser;
    private Category testCategory;
    private Category systemCategory;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .role(User.Role.USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .createdAt(LocalDateTime.now())
                .build();

        testCategory = Category.builder()
                .id(1L)
                .name("Custom Food")
                .description("My custom food category")
                .type(Category.CategoryType.EXPENSE)
                .color("#FF5733")
                .icon("food")
                .isActive(true)
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        systemCategory = Category.builder()
                .id(2L)
                .name("Groceries")
                .description("System category for groceries")
                .type(Category.CategoryType.EXPENSE)
                .color("#00FF00")
                .icon("shopping-cart")
                .isActive(true)
                .user(null) // System category has null user
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllCategories_ShouldReturnUserAndSystemCategories() {
        // Arrange
        List<Category> categories = Arrays.asList(testCategory, systemCategory);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(categoryRepository.findByUserOrSystem(testUser)).thenReturn(categories);

            // Act
            List<Category> result = categoryService.getAllCategories();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(categoryRepository).findByUserOrSystem(testUser);
        }
    }

    @Test
    void getAllActiveCategories_ShouldReturnOnlyActiveCategories() {
        // Arrange
        List<Category> categories = Arrays.asList(testCategory, systemCategory);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(categoryRepository.findByUserOrSystemAndIsActive(testUser, true)).thenReturn(categories);

            // Act
            List<Category> result = categoryService.getAllActiveCategories();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(c -> c.getIsActive()));
            verify(categoryRepository).findByUserOrSystemAndIsActive(testUser, true);
        }
    }

    @Test
    void getCategoriesByType_ShouldReturnCategoriesOfSpecifiedType() {
        // Arrange
        List<Category> categories = Arrays.asList(testCategory, systemCategory);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(categoryRepository.findByUserOrSystemAndType(testUser, Category.CategoryType.EXPENSE)).thenReturn(categories);

            // Act
            List<Category> result = categoryService.getCategoriesByType(Category.CategoryType.EXPENSE);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(c -> c.getType() == Category.CategoryType.EXPENSE));
            verify(categoryRepository).findByUserOrSystemAndType(testUser, Category.CategoryType.EXPENSE);
        }
    }

    @Test
    void getActiveCategoriesByType_ShouldReturnActiveTypedCategories() {
        // Arrange
        List<Category> categories = Arrays.asList(testCategory, systemCategory);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(categoryRepository.findByUserOrSystemAndType(testUser, Category.CategoryType.EXPENSE)).thenReturn(categories);

            // Act
            List<Category> result = categoryService.getActiveCategoriesByType(Category.CategoryType.EXPENSE);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(c -> c.getType() == Category.CategoryType.EXPENSE && c.getIsActive()));
            verify(categoryRepository).findByUserOrSystemAndType(testUser, Category.CategoryType.EXPENSE);
        }
    }

    @Test
    void getCategoryById_WhenCategoryExists_ShouldReturnCategory() {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(categoryRepository.findByIdAndUserOrSystem(1L, testUser)).thenReturn(Optional.of(testCategory));

            // Act
            Optional<Category> result = categoryService.getCategoryById(1L);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(testCategory.getId(), result.get().getId());
            verify(categoryRepository).findByIdAndUserOrSystem(1L, testUser);
        }
    }

    @Test
    void getCategoryById_WhenCategoryDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(categoryRepository.findByIdAndUserOrSystem(999L, testUser)).thenReturn(Optional.empty());

            // Act
            Optional<Category> result = categoryService.getCategoryById(999L);

            // Assert
            assertFalse(result.isPresent());
            verify(categoryRepository).findByIdAndUserOrSystem(999L, testUser);
        }
    }

    @Test
    void getCategoryByName_ShouldReturnCategoryIfFound() {
        // Arrange
        when(categoryRepository.findByName("Custom Food")).thenReturn(Optional.of(testCategory));

        // Act
        Optional<Category> result = categoryService.getCategoryByName("Custom Food");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Custom Food", result.get().getName());
        verify(categoryRepository).findByName("Custom Food");
    }

    @Test
    void searchCategoriesByName_ShouldReturnMatchingCategories() {
        // Arrange
        List<Category> categories = Arrays.asList(testCategory);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(categoryRepository.findByUserOrSystem(testUser)).thenReturn(categories);

            // Act
            List<Category> result = categoryService.searchCategoriesByName("Food");

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.get(0).getName().toLowerCase().contains("food"));
            verify(categoryRepository).findByUserOrSystem(testUser);
        }
    }

    @Test
    void searchActiveCategoriesByName_ShouldReturnActiveMatchingCategories() {
        // Arrange
        List<Category> categories = Arrays.asList(testCategory);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(categoryRepository.findByUserOrSystemAndIsActive(testUser, true)).thenReturn(categories);

            // Act
            List<Category> result = categoryService.searchActiveCategoriesByName("Food");

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.get(0).getName().toLowerCase().contains("food"));
            assertTrue(result.get(0).getIsActive());
            verify(categoryRepository).findByUserOrSystemAndIsActive(testUser, true);
        }
    }

    @Test
    void createCategory_WithValidData_ShouldCreateCategory() {
        // Arrange
        Category newCategory = Category.builder()
                .name("New Category")
                .description("A new custom category")
                .type(Category.CategoryType.INCOME)
                .color("#0000FF")
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(categoryRepository.existsByName("New Category")).thenReturn(false);
            when(categoryRepository.save(any(Category.class))).thenReturn(newCategory);

            // Act
            Category result = categoryService.createCategory(newCategory);

            // Assert
            assertNotNull(result);
            assertEquals("New Category", result.getName());
            assertEquals(testUser, newCategory.getUser());
            verify(categoryRepository).existsByName("New Category");
            verify(categoryRepository).save(newCategory);
        }
    }

    @Test
    void createCategory_WithDuplicateName_ShouldThrowException() {
        // Arrange
        Category newCategory = Category.builder()
                .name("Custom Food")
                .type(Category.CategoryType.EXPENSE)
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(categoryRepository.existsByName("Custom Food")).thenReturn(true);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.createCategory(newCategory)
            );

            assertTrue(exception.getMessage().contains("already exists"));
            verify(categoryRepository).existsByName("Custom Food");
            verify(categoryRepository, never()).save(any(Category.class));
        }
    }

    @Test
    void updateCategory_WithValidData_ShouldUpdateCategory() {
        // Arrange
        Category updatedCategory = Category.builder()
                .id(1L)
                .name("Updated Food")
                .description("Updated description")
                .type(Category.CategoryType.EXPENSE)
                .color("#FF0000")
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(categoryRepository.existsByNameAndIdNot("Updated Food", 1L)).thenReturn(false);
            when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

            // Act
            Category result = categoryService.updateCategory(updatedCategory);

            // Assert
            assertNotNull(result);
            assertEquals("Updated Food", result.getName());
            assertEquals(testUser, updatedCategory.getUser());
            verify(categoryRepository).findById(1L);
            verify(categoryRepository).save(updatedCategory);
        }
    }

    @Test
    void updateCategory_WhenCategoryNotFound_ShouldThrowException() {
        // Arrange
        Category updatedCategory = Category.builder()
                .id(999L)
                .name("Updated Food")
                .type(Category.CategoryType.EXPENSE)
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.updateCategory(updatedCategory)
            );

            assertTrue(exception.getMessage().contains("not found"));
            verify(categoryRepository).findById(999L);
            verify(categoryRepository, never()).save(any(Category.class));
        }
    }

    @Test
    void updateCategory_WhenSystemCategory_ShouldThrowException() {
        // Arrange
        Category updatedCategory = Category.builder()
                .id(2L)
                .name("Updated Groceries")
                .type(Category.CategoryType.EXPENSE)
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(systemCategory));

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.updateCategory(updatedCategory)
            );

            assertTrue(exception.getMessage().contains("Cannot modify system category"));
            verify(categoryRepository).findById(2L);
            verify(categoryRepository, never()).save(any(Category.class));
        }
    }

    @Test
    void updateCategory_WhenNotOwner_ShouldThrowException() {
        // Arrange
        User differentUser = User.builder()
                .id(2L)
                .username("differentuser")
                .email("different@example.com")
                .password("password")
                .role(User.Role.USER)
                .build();

        Category updatedCategory = Category.builder()
                .id(1L)
                .name("Updated Food")
                .type(Category.CategoryType.EXPENSE)
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(differentUser);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.updateCategory(updatedCategory)
            );

            assertTrue(exception.getMessage().contains("doesn't belong to you"));
            verify(categoryRepository).findById(1L);
            verify(categoryRepository, never()).save(any(Category.class));
        }
    }

    @Test
    void deleteCategory_WhenCategoryExists_ShouldDeleteCategory() {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            doNothing().when(categoryRepository).delete(testCategory);

            // Act
            categoryService.deleteCategory(1L);

            // Assert
            verify(categoryRepository).findById(1L);
            verify(categoryRepository).delete(testCategory);
        }
    }

    @Test
    void deleteCategory_WhenSystemCategory_ShouldThrowException() {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(systemCategory));

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.deleteCategory(2L)
            );

            assertTrue(exception.getMessage().contains("Cannot delete system category"));
            verify(categoryRepository).findById(2L);
            verify(categoryRepository, never()).delete(any(Category.class));
        }
    }

    @Test
    void deleteCategory_WhenNotOwner_ShouldThrowException() {
        // Arrange
        User differentUser = User.builder()
                .id(2L)
                .username("differentuser")
                .email("different@example.com")
                .password("password")
                .role(User.Role.USER)
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(differentUser);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.deleteCategory(1L)
            );

            assertTrue(exception.getMessage().contains("doesn't belong to you"));
            verify(categoryRepository).findById(1L);
            verify(categoryRepository, never()).delete(any(Category.class));
        }
    }

    @Test
    void deactivateCategory_ShouldSetIsActiveToFalse() {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Category result = categoryService.deactivateCategory(1L);

            // Assert
            assertNotNull(result);
            assertFalse(result.getIsActive());
            verify(categoryRepository).save(testCategory);
        }
    }

    @Test
    void deactivateCategory_WhenSystemCategory_ShouldThrowException() {
        // Arrange
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(systemCategory));

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.deactivateCategory(2L)
            );

            assertTrue(exception.getMessage().contains("Cannot modify system category"));
            verify(categoryRepository).findById(2L);
            verify(categoryRepository, never()).save(any(Category.class));
        }
    }

    @Test
    void activateCategory_ShouldSetIsActiveToTrue() {
        // Arrange
        testCategory.setIsActive(false);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Category result = categoryService.activateCategory(1L);

            // Assert
            assertNotNull(result);
            assertTrue(result.getIsActive());
            verify(categoryRepository).save(testCategory);
        }
    }

    @Test
    void categoryExistsByName_WhenExists_ShouldReturnTrue() {
        // Arrange
        when(categoryRepository.existsByName("Custom Food")).thenReturn(true);

        // Act
        boolean result = categoryService.categoryExistsByName("Custom Food");

        // Assert
        assertTrue(result);
        verify(categoryRepository).existsByName("Custom Food");
    }

    @Test
    void categoryExistsByName_WhenDoesNotExist_ShouldReturnFalse() {
        // Arrange
        when(categoryRepository.existsByName("Nonexistent")).thenReturn(false);

        // Act
        boolean result = categoryService.categoryExistsByName("Nonexistent");

        // Assert
        assertFalse(result);
        verify(categoryRepository).existsByName("Nonexistent");
    }

    @Test
    void categoryExistsByNameAndIdNot_ShouldReturnCorrectResult() {
        // Arrange
        when(categoryRepository.existsByNameAndIdNot("Custom Food", 2L)).thenReturn(true);

        // Act
        boolean result = categoryService.categoryExistsByNameAndIdNot("Custom Food", 2L);

        // Assert
        assertTrue(result);
        verify(categoryRepository).existsByNameAndIdNot("Custom Food", 2L);
    }
}

