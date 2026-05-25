package com.finance.manager.controller;

import com.finance.manager.dto.request.CategoryRequest;
import com.finance.manager.dto.response.CategoryResponse;
import com.finance.manager.dto.response.MessageResponse;
import com.finance.manager.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// controller for managing categories (income, expense types etc)
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // get all categories for the logged in user
    @GetMapping
    public ResponseEntity<Map<String, List<CategoryResponse>>> getAllCategories(HttpServletRequest request) {
        List<CategoryResponse> allCategories = categoryService.getAllCategories(request);

        // wrap the list in a map so the json looks cleaner
        Map<String, List<CategoryResponse>> result = new HashMap<>();
        result.put("categories", allCategories);

        return ResponseEntity.ok(result);
    }

    // create a new category
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody @Valid CategoryRequest categoryRequest,
                                                            HttpServletRequest request) {
        CategoryResponse createdCategory = categoryService.createCategory(categoryRequest, request);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    // delete a category by its name
    @DeleteMapping("/{name}")
    public ResponseEntity<MessageResponse> deleteCategory(@PathVariable String name,
                                                          HttpServletRequest request) {
        MessageResponse response = categoryService.deleteCategory(name, request);
        return ResponseEntity.ok(response);
    }
}
