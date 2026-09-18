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


    // ==========================================
    // GET LOGGED-IN USER
    // ==========================================

    private User getLoggedInUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        ));
    }


    // ==========================================
    // CREATE CUSTOM CATEGORY
    // ==========================================

    public CategoryResponse createCategory(
            CategoryRequest request) {

        User user = getLoggedInUser();

        if (categoryRepository
                .existsByNameIgnoreCaseAndUser(
                        request.getName(),
                        user)) {

            throw new IllegalArgumentException(
                    "Category already exists"
            );
        }


        Category category = new Category();

        category.setName(request.getName());

        // Custom category belongs to this user
        category.setUser(user);


        Category savedCategory =
                categoryRepository.save(category);


        return new CategoryResponse(
                savedCategory.getId(),
                savedCategory.getName()
        );
    }


    // ==========================================
    // GET ALL CATEGORIES
    // ==========================================

    public List<CategoryResponse> getAllCategories() {

        User user = getLoggedInUser();


        // Shared default categories
        List<Category> defaultCategories =
                categoryRepository.findByUserIsNull();


        // User's custom categories
        List<Category> userCategories =
                categoryRepository.findByUser(user);


        return java.util.stream.Stream
                .concat(
                        defaultCategories.stream(),
                        userCategories.stream()
                )
                .map(category ->
                        new CategoryResponse(
                                category.getId(),
                                category.getName()
                        )
                )
                .toList();
    }


    // ==========================================
    // GET CATEGORY BY ID
    // ==========================================

    public CategoryResponse getCategoryById(
            Long id) {

        User user = getLoggedInUser();


        Category category =
                categoryRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Category not found"
                                ));


        // Allow shared default categories
        // OR categories owned by current user
        if (category.getUser() != null
                && !category.getUser().getId()
                .equals(user.getId())) {

            throw new RuntimeException(
                    "You are not allowed to access this category"
            );
        }


        return new CategoryResponse(
                category.getId(),
                category.getName()
        );
    }


    // ==========================================
    // UPDATE CATEGORY
    // ==========================================

    public CategoryResponse updateCategory(
            Long id,
            CategoryRequest request) {

        User user = getLoggedInUser();


        Category category =
                categoryRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Category not found"
                                ));


        // Default categories cannot be modified
        if (category.getUser() == null) {

            throw new RuntimeException(
                    "Default categories cannot be updated"
            );
        }


        // Only owner can update
        if (!category.getUser().getId()
                .equals(user.getId())) {

            throw new RuntimeException(
                    "You are not allowed to update this category"
            );
        }


        category.setName(
                request.getName()
        );


        Category updatedCategory =
                categoryRepository.save(category);


        return new CategoryResponse(
                updatedCategory.getId(),
                updatedCategory.getName()
        );
    }


    // ==========================================
    // DELETE CATEGORY
    // ==========================================

    public void deleteCategory(Long id) {

        User user = getLoggedInUser();


        Category category =
                categoryRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Category not found"
                                ));


        // Default categories cannot be deleted
        if (category.getUser() == null) {

            throw new RuntimeException(
                    "Default categories cannot be deleted"
            );
        }


        // Only owner can delete
        if (!category.getUser().getId()
                .equals(user.getId())) {

            throw new RuntimeException(
                    "You are not allowed to delete this category"
            );
        }


        categoryRepository.delete(category);
    }


    // ==========================================
    // CREATE DEFAULT CATEGORIES
    // ==========================================

    public void createDefaultCategories() {

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


        for (String categoryName :
                defaultCategories) {


            boolean alreadyExists =
                    categoryRepository
                            .existsByNameIgnoreCaseAndUserIsNull(
                                    categoryName
                            );


            if (!alreadyExists) {

                Category category =
                        new Category();

                category.setName(
                        categoryName
                );

                // NULL = shared/default category
                category.setUser(null);


                categoryRepository.save(
                        category
                );
            }
        }
    }
}