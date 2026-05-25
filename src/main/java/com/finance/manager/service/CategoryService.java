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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final AuthService authService;

    public CategoryService(CategoryRepository categoryRepository,
                           TransactionRepository transactionRepository,
                           AuthService authService) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.authService = authService;
    }

    // get all categories - both default ones and user's custom ones
    public List<CategoryResponse> getAllCategories(HttpServletRequest request) {
        User currentUser = authService.getCurrentUser(request);

        List<Category> allCategories = categoryRepository.findByUserIsNullOrUser(currentUser);

        // map to response DTOs
        return allCategories.stream()
            .map(cat -> CategoryResponse.builder()
                .name(cat.getName())
                .type(cat.getType().name())
                .isCustom(cat.isCustom())
                .build())
            .collect(Collectors.toList());
    }

    // create a new custom category for the user
    public CategoryResponse createCategory(CategoryRequest request, HttpServletRequest httpRequest) {
        User currentUser = authService.getCurrentUser(httpRequest);

        // check if category with same name exists as default
        if (categoryRepository.existsByNameAndUserIsNull(request.getName())) {
            throw new DuplicateResourceException("Category '" + request.getName() + "' already exists as a default category");
        }

        // check if user already has a custom category with this name
        if (categoryRepository.existsByNameAndUser(request.getName(), currentUser)) {
            throw new DuplicateResourceException("You already have a category named '" + request.getName() + "'");
        }

        // create and save the category
        Category newCategory = Category.builder()
            .name(request.getName())
            .type(request.getType())
            .isCustom(true)
            .user(currentUser)
            .build();

        Category saved = categoryRepository.save(newCategory);

        return CategoryResponse.builder()
            .name(saved.getName())
            .type(saved.getType().name())
            .isCustom(saved.isCustom())
            .build();
    }

    // delete a custom category
    // NOTE: if IllegalStateException is thrown here, GlobalExceptionHandler should catch it and return 400
    public MessageResponse deleteCategory(String name, HttpServletRequest httpRequest) {
        User currentUser = authService.getCurrentUser(httpRequest);

        // first try to find it as user's custom category
        Category category = categoryRepository.findByNameAndUser(name, currentUser).orElse(null);

        if (category == null) {
            // check if its a default category - cant delete those
            if (categoryRepository.existsByNameAndUserIsNull(name)) {
                throw new AccessDeniedException("Cannot delete default category");
            }
            // not found at all
            throw new ResourceNotFoundException("Category not found: " + name);
        }

        // check if any transactions are using this category
        if (transactionRepository.existsByCategoryAndUser(category, currentUser)) {
            throw new IllegalStateException("Cannot delete category used by transactions");
        }

        categoryRepository.delete(category);
        return new MessageResponse("Category deleted successfully");
    }
}
