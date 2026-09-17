package com.expensetracker.BuckSave.service;

import com.expensetracker.BuckSave.dto.CategoryRequest;
import com.expensetracker.BuckSave.dto.CategoryResponse;
import com.expensetracker.BuckSave.entity.Category;
import com.expensetracker.BuckSave.entity.User;
import com.expensetracker.BuckSave.repository.CategoryRepository;
import com.expensetracker.BuckSave.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(
            CategoryRepository categoryRepository,
            UserRepository userRepository) {

        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    private User getLoggedInUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }
    public CategoryResponse createCategory(CategoryRequest request) {

        User user = getLoggedInUser();
        if (categoryRepository.existsByNameIgnoreCaseAndUser(
                request.getName(),
                user)) {

            throw new IllegalArgumentException(
                    "Category already exists"

            );
        }

        Category category = new Category();

        category.setName(request.getName());
        category.setUser(user);

        Category savedCategory = categoryRepository.save(category);

        return new CategoryResponse(
                savedCategory.getId(),
                savedCategory.getName()
        );
    }

    public List<CategoryResponse> getAllCategories() {

        User user = getLoggedInUser();

        return categoryRepository.findByUser(user)
                .stream()
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getName()
                ))
                .toList();
    }
    public CategoryResponse getCategoryById(Long id) {

        User user = getLoggedInUser();

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Category not found"));

        if (!category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException(
                    "You are not allowed to access this category");
        }

        return new CategoryResponse(
                category.getId(),
                category.getName()
        );
    }

    public CategoryResponse updateCategory(
            Long id,
            CategoryRequest request) {

        User user = getLoggedInUser();

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Category not found"));

        if (!category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException(
                    "You are not allowed to update this category");
        }

        category.setName(request.getName());

        Category updatedCategory =
                categoryRepository.save(category);

        return new CategoryResponse(
                updatedCategory.getId(),
                updatedCategory.getName()
        );
    }
    public void deleteCategory(Long id) {

        User user = getLoggedInUser();

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Category not found"));

        if (!category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException(
                    "You are not allowed to delete this category");
        }

        categoryRepository.delete(category);
    }
    public void createDefaultCategories(User user)  {


        String[] defaultCategories = {
                "Food",
                "Transport",
                "Shopping",
                "Rent & Housing",
                "Bills & Utilities",
                "Entertainment",
                "Health",
                "Education",
                "Travel",
                "Other"
        };

        for (String categoryName : defaultCategories) {

            boolean alreadyExists = categoryRepository
                    .findByUser(user)
                    .stream()
                    .anyMatch(category ->
                            category.getName()
                                    .equalsIgnoreCase(categoryName)
                    );

            if (!alreadyExists) {

                Category category = new Category();

                category.setName(categoryName);
                category.setUser(user);

                categoryRepository.save(category);
            }
        }
    }


}
