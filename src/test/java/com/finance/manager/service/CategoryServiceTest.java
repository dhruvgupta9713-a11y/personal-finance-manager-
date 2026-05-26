package com.finance.manager.service;

import com.finance.manager.dto.request.CategoryRequest;
import com.finance.manager.dto.response.CategoryResponse;
import com.finance.manager.dto.response.MessageResponse;
import com.finance.manager.entity.Category;
import com.finance.manager.entity.User;
import com.finance.manager.exception.DuplicateResourceException;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.repository.CategoryRepository;
import com.finance.manager.repository.TransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// category service tests
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private CategoryService categoryService;

    private User testUser;
    private Category defaultCategory;
    private Category customCategory;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser@example.com");

        // a default category (no user attached)
        defaultCategory = Category.builder()
                .id(1L)
                .name("Salary")
                .type(Category.CategoryType.INCOME)
                .isCustom(false)
                .user(null)
                .build();

        // a custom category the user created
        customCategory = Category.builder()
                .id(2L)
                .name("Freelance")
                .type(Category.CategoryType.INCOME)
                .isCustom(true)
                .user(testUser)
                .build();
    }

    @Test
    void testGetAllCategories() {
        // should return both default and user categories
        when(authService.getCurrentUser(request)).thenReturn(testUser);
        when(categoryRepository.findByUserIsNullOrUser(testUser))
                .thenReturn(Arrays.asList(defaultCategory, customCategory));

        List<CategoryResponse> result = categoryService.getAllCategories(request);

        assertNotNull(result);
        assertEquals(2, result.size());
        // check the names are there
        assertEquals("Salary", result.get(0).getName());
        assertEquals("Freelance", result.get(1).getName());
    }

    @Test
    void testCreateCategorySuccess() {
        // creating a new custom category
        when(authService.getCurrentUser(request)).thenReturn(testUser);
        when(categoryRepository.existsByNameAndUserIsNull("MyCategory")).thenReturn(false);
        when(categoryRepository.existsByNameAndUser("MyCategory", testUser)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(customCategory);

        CategoryRequest categoryRequest = new CategoryRequest();
        categoryRequest.setName("MyCategory");
        categoryRequest.setType(Category.CategoryType.EXPENSE);

        CategoryResponse response = categoryService.createCategory(categoryRequest, request);

        assertNotNull(response);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void testCreateCategoryDuplicate() {
        // category with same name already exists as default
        when(authService.getCurrentUser(request)).thenReturn(testUser);
        when(categoryRepository.existsByNameAndUserIsNull("Salary")).thenReturn(true);

        CategoryRequest categoryRequest = new CategoryRequest();
        categoryRequest.setName("Salary");
        categoryRequest.setType(Category.CategoryType.INCOME);

        assertThrows(DuplicateResourceException.class, () -> {
            categoryService.createCategory(categoryRequest, request);
        });

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void testDeleteCustomCategorySuccess() {
        // deleting a custom category that belongs to the user
        when(authService.getCurrentUser(request)).thenReturn(testUser);
        when(categoryRepository.findByNameAndUser("Freelance", testUser))
                .thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategoryAndUser(customCategory, testUser))
                .thenReturn(false);

        MessageResponse response = categoryService.deleteCategory("Freelance", request);

        assertNotNull(response);
        assertEquals("Category deleted successfully", response.getMessage());
        verify(categoryRepository).delete(customCategory);
    }

    @Test
    void testDeleteDefaultCategory() {
        // trying to delete a default category should fail with AccessDeniedException
        when(authService.getCurrentUser(request)).thenReturn(testUser);
        when(categoryRepository.findByNameAndUser("Salary", testUser))
                .thenReturn(Optional.empty());
        when(categoryRepository.existsByNameAndUserIsNull("Salary"))
                .thenReturn(true);

        assertThrows(AccessDeniedException.class, () -> {
            categoryService.deleteCategory("Salary", request);
        });
    }

    @Test
    void testDeleteCategoryNotFound() {
        // category doesnt exist at all
        when(authService.getCurrentUser(request)).thenReturn(testUser);
        when(categoryRepository.findByNameAndUser("NonExistent", testUser))
                .thenReturn(Optional.empty());
        when(categoryRepository.existsByNameAndUserIsNull("NonExistent"))
                .thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.deleteCategory("NonExistent", request);
        });
    }

    @Test
    void testDeleteCategoryUsedByTransactions() {
        // category is used by transactions so it should fail
        when(authService.getCurrentUser(request)).thenReturn(testUser);
        when(categoryRepository.findByNameAndUser("Freelance", testUser))
                .thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategoryAndUser(customCategory, testUser))
                .thenReturn(true);

        assertThrows(IllegalStateException.class, () -> {
            categoryService.deleteCategory("Freelance", request);
        });

        // make sure we didnt delete it
        verify(categoryRepository, never()).delete(any(Category.class));
    }
}
