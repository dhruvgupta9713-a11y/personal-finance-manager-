package com.finance.manager.config;

import com.finance.manager.entity.Category;
import com.finance.manager.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// this class loads default categories when the app starts up
@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public DataSeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // only seed if there are no categories yet
        if (categoryRepository.count() == 0) {
            List<Category> defaultCategories = new ArrayList<>();

            // income category
            defaultCategories.add(Category.builder()
                .name("Salary")
                .type(Category.CategoryType.INCOME)
                .isCustom(false)
                .user(null)
                .build());

            // expense categories
            String[] expenseNames = {"Food", "Rent", "Transportation", "Entertainment", "Healthcare", "Utilities"};
            for (String name : expenseNames) {
                defaultCategories.add(Category.builder()
                    .name(name)
                    .type(Category.CategoryType.EXPENSE)
                    .isCustom(false)
                    .user(null)
                    .build());
            }

            categoryRepository.saveAll(defaultCategories);
            System.out.println("Default categories loaded!");
        } else {
            System.out.println("Categories already exist, skipping");
        }
    }
}
